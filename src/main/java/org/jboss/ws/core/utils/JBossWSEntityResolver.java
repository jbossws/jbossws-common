/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ws.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** 
 * Dynamically register the JBossWS entities.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Richard.Opalka@jboss.org
 */
public class JBossWSEntityResolver extends JBossEntityResolver
{
   /**
    * A synchronized weak hash map that keeps entities' properties for each classloader.
    * Weak keys are used to remove entries when classloaders are garbage collected; values are filenames -> properties.
    */
   private static Map<ClassLoader, Map<String, Properties>> propertiesMap = Collections.synchronizedMap(new WeakHashMap<ClassLoader, Map<String, Properties>>());
   
   // provide logging
   private static final Logger log = Logger.getLogger(JBossWSEntityResolver.class);

   public JBossWSEntityResolver()
   {
	  this("META-INF/jbossws-entities.properties");
   }
   
   public JBossWSEntityResolver(final String entitiesResource)
   {
      super();
      
      Properties props = null;
      ClassLoader loader = SecurityActions.getContextClassLoader();
      Map<String, Properties> map = propertiesMap.get(loader);
      if (map != null && map.containsKey(entitiesResource))
      {
         props = map.get(entitiesResource);
      }
      else
      {
         if (map == null)
         {
            map = new ConcurrentHashMap<String, Properties>();
            propertiesMap.put(loader, map);
         }
         // load entities
         props = loadEntitiesMappingFromClasspath(entitiesResource, loader);
         if (props.size() == 0)
            throw new IllegalArgumentException("No entities mapping defined in resource file: " + entitiesResource);
         map.put(entitiesResource, props);
      }
      
	   // register entities
	   String key = null, val = null;
	   for (Enumeration<Object> keys = props.keys(); keys.hasMoreElements();)
	   {
		   key = (String)keys.nextElement();
		   val = props.getProperty(key);
		   
		   registerEntity(key, val);
	   }
   }
   
   private Properties loadEntitiesMappingFromClasspath(final String entitiesResource, final ClassLoader classLoader)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Properties>()
      {
         public Properties run()
         {
            //use a delegate classloader: first try lookup using the provided classloader,
            //otherwise use server integration classloader which has the default configuration
            final ClassLoader intCl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
            InputStream is = new DelegateClassLoader(intCl, classLoader).getResourceAsStream(entitiesResource);
            // get stream
            if (is == null)
               throw new IllegalArgumentException("Resource " + entitiesResource + " not found");

            // load props
            Properties props = new Properties();
            try
            {
               props.load(is);
            }
            catch (IOException ioe)
            {
               log.error("Cannot read resource: " + entitiesResource, ioe);
            }
            finally
            {
               try { is.close(); } catch (IOException ioe) {} // ignore
            }

            return props;
         }
      });
   }

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      if(log.isTraceEnabled()) log.trace("resolveEntity: [pub=" + publicId + ",sysid=" + systemId + "]");
      InputSource inputSource = super.resolveEntity(publicId, systemId);

      if (inputSource == null)
         inputSource = resolveSystemIDAsURL(systemId, log.isTraceEnabled());

      if (inputSource == null)
      {
         if (log.isDebugEnabled())
            log.debug("Cannot resolve entity: [pub=" + publicId + ",sysid=" + systemId + "]");
      }
      
      return inputSource;
   }

   /** Use a ResourceURL to access the resource.
    *  This method should be protected in the super class. */
   protected InputSource resolveSystemIDAsURL(String id, boolean trace)
   {
      if (id == null)
         return null;

      if (trace)
         log.trace("resolveIDAsResourceURL, id=" + id);

      InputSource inputSource = null;

      // Try to use the systemId as a URL to the schema
      try
      {
         if (trace)
            log.trace("Trying to resolve id as a URL");

         URL url = new URL(id);
         if (url.getProtocol().equalsIgnoreCase("file") == false)
            log.warn("Trying to resolve id as a non-file URL: " + id);

         InputStream ins = new ResourceURL(url).openStream();
         if (ins != null)
         {
            inputSource = new InputSource(ins);
            inputSource.setSystemId(id);
         }
         else
         {
            log.warn("Cannot load id as URL: " + id);
         }

         if (trace)
            log.trace("Resolved id as a URL");
      }
      catch (MalformedURLException ignored)
      {
         if (trace)
            log.trace("id is not a url: " + id, ignored);
      }
      catch (IOException e)
      {
         if (trace)
            log.trace("Failed to obtain URL.InputStream from id: " + id, e);
      }
      return inputSource;
   }
}
