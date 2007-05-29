/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ws.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;


// $Id$

/**
 * Load a factory using the factory lookup procedure 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 14-Dec-2006
 */
public class FactoryLoader
{
   private FactoryLoader()
   {
   }

   /**
    * Load a factory using this ordered lookup procedure
    *
    * <ol>
    *  <li>Use the system property
    *  <li>Use the properties file "lib/jaxm.properties" in the JRE directory
    *  <li>Use the Services API (as detailed in the JAR specification), if available, to determine the classname
    *  <li>Use the default factory implementation class
    * </ol>
    *  
    *  @return the factory impl, or null 
    */
   public static Object loadFactory(String propertyName, String defaultFactory) 
   {
      Object factory = null;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      // Use the system property
      PrivilegedAction action = new PropertyAccessAction(propertyName);
      String factoryName = (String)AccessController.doPrivileged(action);
      if (factoryName != null)
      {
         try
         {
            //if(log.isDebugEnabled()) log.debug("Load from system property: " + factoryName);
            Class factoryClass = loader.loadClass(factoryName);
            factory = factoryClass.newInstance();
         }
         catch (Throwable t)
         {
            throw new IllegalStateException("Failed to load " + propertyName + ": " + factoryName, t);
         }
      }

      // Use the properties file "lib/jaxm.properties" in the JRE directory.
      // This configuration file is in standard java.util.Properties format and contains the fully qualified name of the implementation class with the key being the system property defined above.
      if (factory == null)
      {
         action = new PropertyAccessAction("java.home");
         String javaHome = (String)AccessController.doPrivileged(action);
         File jaxmFile = new File(javaHome + "/lib/jaxm.properties");
         if (jaxmFile.exists())
         {
            try
            {
               action = new PropertyFileAccessAction(jaxmFile.getCanonicalPath());
               Properties jaxmProperties = (Properties)AccessController.doPrivileged(action);
               factoryName = jaxmProperties.getProperty(propertyName);
               if (factoryName != null)
               {
                  //if(log.isDebugEnabled()) log.debug("Load from " + jaxmFile + ": " + factoryName);
                  Class factoryClass = loader.loadClass(factoryName);
                  factory = factoryClass.newInstance();
               }
            }
            catch (Throwable t)
            {
               throw new IllegalStateException("Failed to load " + propertyName + ": " + factoryName, t);
            }
         }
      }

      // Use the Services API (as detailed in the JAR specification), if available, to determine the classname.
      if (factory == null)
      {
         String filename = "META-INF/services/" + propertyName;
         InputStream inStream = loader.getResourceAsStream(filename);
         if (inStream != null)
         {
            try
            {
               BufferedReader br = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
               factoryName = br.readLine();
               br.close();
               if (factoryName != null)
               {
                  //if(log.isTraceEnabled()) log.trace("Load from Service API " + filename + ": " + factoryName);
                  Class factoryClass = loader.loadClass(factoryName);
                  factory = factoryClass.newInstance();
               }
            }
            catch (Throwable t)
            {
               throw new IllegalStateException("Failed to load " + propertyName + ": " + factoryName, t);
            }
         }
      }

      // Use the default factory implementation class.
      if (factory == null && defaultFactory != null)
      {
         try
         {
            factoryName = defaultFactory;
            //if(log.isDebugEnabled()) log.debug("Load from default: " + factoryName);
            Class factoryClass = loader.loadClass(factoryName);
            factory = factoryClass.newInstance();
         }
         catch (Throwable t)
         {
            throw new IllegalStateException("Failed to load " + propertyName + ": " + factoryName, t);
         }
      }

      return factory;
   }

   private static class PropertyAccessAction implements PrivilegedAction
   {
      private String name;

      PropertyAccessAction(String name)
      {
         this.name = name;
      }

      public Object run()
      {
         return System.getProperty(name);
      }
   }

   private static class PropertyFileAccessAction implements PrivilegedAction
   {
      private String filename;

      PropertyFileAccessAction(String filename)
      {
         this.filename = filename;
      }

      public Object run()
      {
         try
         {
            InputStream inStream = new FileInputStream(filename);
            Properties props = new Properties();
            props.load(inStream);
            return props;
         }
         catch (IOException ex)
         {
            throw new SecurityException("Cannot load properties: " + filename, ex);
         }
      }
   }
}
