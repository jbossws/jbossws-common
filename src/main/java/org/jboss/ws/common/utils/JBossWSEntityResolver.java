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
package org.jboss.ws.common.utils;

import static org.jboss.ws.common.Loggers.ROOT_LOGGER;
import static org.jboss.ws.common.Messages.MESSAGES;

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
   
   private ClassLoader additionalClassLoader;

   public JBossWSEntityResolver()
   {
      this(ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
   }
   
   public JBossWSEntityResolver(ClassLoader loader)
   {
      this("META-INF/jbossws-entities.properties", loader);
   }
   
   public JBossWSEntityResolver(final String entitiesResource)
   {
      this(entitiesResource, ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
   }
   
   public JBossWSEntityResolver(final String entitiesResource, final ClassLoader loader)
   {
      super();
      
      this.additionalClassLoader = loader;
      Properties props = null;
      ClassLoader tccl = SecurityActions.getContextClassLoader();
      Map<String, Properties> map = propertiesMap.get(tccl);
      if (map != null && map.containsKey(entitiesResource))
      {
         props = map.get(entitiesResource);
      }
      else
      {
         if (map == null)
         {
            map = new ConcurrentHashMap<String, Properties>();
            propertiesMap.put(tccl, map);
         }
         // load entities
         props = loadEntitiesMappingFromClasspath(entitiesResource, tccl, this.additionalClassLoader);
         if (props.size() == 0)
            throw MESSAGES.entityResolutionNoEntityMapppingDefined(entitiesResource);
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
   
   private Properties loadEntitiesMappingFromClasspath(final String entitiesResource, final ClassLoader classLoader, final ClassLoader additionalClassLoader)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Properties>()
      {
         public Properties run()
         {
            //use a delegate classloader: first try lookup using the provided (tccl) classloader,
            //otherwise use the constructor provided classloader if any (that defaults to the
            //server integration classloader which should have the default configuration)
            InputStream is = new DelegateClassLoader(additionalClassLoader, classLoader).getResourceAsStream(entitiesResource);
            // get stream
            if (is == null)
               throw MESSAGES.entityResolutionResourceNotFound(entitiesResource);

            // load props
            Properties props = new Properties();
            try
            {
               props.load(is);
            }
            catch (IOException ioe)
            {
               ROOT_LOGGER.cannotReadResource(entitiesResource, ioe);
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
      if(ROOT_LOGGER.isTraceEnabled()) ROOT_LOGGER.trace(this.getClass().getName(), "resolveEntity: [pub=" + publicId + ",sysid=" + systemId + "]", null);
      InputSource inputSource = super.resolveEntity(publicId, systemId);

      if (inputSource == null)
         inputSource = resolveSystemIDAsURL(systemId, ROOT_LOGGER.isTraceEnabled());

      if (inputSource == null)
      {
         if (ROOT_LOGGER.isDebugEnabled())
            ROOT_LOGGER.debug(this.getClass().getName(), "Cannot resolve entity: [pub=" + publicId + ",sysid=" + systemId + "]", null);
      }
      
      return inputSource;
   }
   
   protected InputStream loadClasspathResource(String resource, boolean trace)
   {
       InputStream is = super.loadClasspathResource(resource, trace);
       if (is == null)
       {
           final ClassLoader origLoader = SecurityActions.getContextClassLoader();
           try
           {
               SecurityActions.setContextClassLoader(this.additionalClassLoader);
               is = super.loadClasspathResource(resource, trace);
           }
           finally
           {
               SecurityActions.setContextClassLoader(origLoader);
           }
       }
       
       return is;
   }

   /** Use a ResourceURL to access the resource.
    *  This method should be protected in the super class. */
   protected InputSource resolveSystemIDAsURL(String id, boolean trace)
   {
      if (id == null)
         return null;

      if (trace)
         ROOT_LOGGER.trace(this.getClass().getName(), "resolveIDAsResourceURL, id=" + id, null);

      InputSource inputSource = null;

      // Try to use the systemId as a URL to the schema
      try
      {
         if (trace)
            ROOT_LOGGER.trace(this.getClass().getName(), "Trying to resolve id as a URL", null);

         URL url = new URL(id);
         InputStream ins = new ResourceURL(url).openStream();
         if (ins != null)
         {
            inputSource = new InputSource(ins);
            inputSource.setSystemId(id);
         }
         else
         {
            ROOT_LOGGER.cannotLoadIDAsURL(id, url.getProtocol());
         }

         if (trace)
            ROOT_LOGGER.trace(this.getClass().getName(), "Resolved id as a URL", null);
      }
      catch (MalformedURLException ignored)
      {
         if (trace)
            ROOT_LOGGER.trace(this.getClass().getName(), "id is not a url: " + id, ignored);
      }
      catch (IOException e)
      {
         if (trace)
            ROOT_LOGGER.trace(this.getClass().getName(), "Failed to obtain URL.InputStream from id: " + id, e);
      }
      return inputSource;
   }
}
