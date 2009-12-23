/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.jboss.logging.Logger;

/**
 * A thread-safe {@link DocumentBuilderFactory} that also adds a caching system
 * for preventing useless access to the filesystem due to the Service API when
 * the same context classloader is in place.
 * 
 * @author alessio.soldano@jboss.com
 * @since 22-Dec-2009
 *
 */
public class JBossWSDocumentBuilderFactory extends DocumentBuilderFactory
{
   private static Logger log = Logger.getLogger(JBossWSDocumentBuilderFactory.class);
   private static final String PROPERTY_NAME = "javax.xml.parsers.DocumentBuilderFactory";
   private static final boolean useJaxpProperty;
   /**
    * A weak hash map that keeps DocumentBuilderFactory instances for each classloader.
    * Weak keys are used to remove entries when classloaders are garbage collected.
    * 
    * No need for a synchronized map as this accessed from the
    * static synchronized newInstance newInstance method.
    */
   private static Map<ClassLoader, JBossWSDocumentBuilderFactory> factoryMap = new WeakHashMap<ClassLoader, JBossWSDocumentBuilderFactory>();
   
   private final DocumentBuilderFactory delegate;
   
   //ThreadLocal attributes and features maps required to achieve thread safety
   private ThreadLocal<DocumentBuilderFactoryFields> fields = new ThreadLocal<DocumentBuilderFactoryFields>() {
      @Override
      protected DocumentBuilderFactoryFields initialValue()
      {
         return new DocumentBuilderFactoryFields();
      }
   };
   
   static
   {
      // Use the properties file "lib/jaxp.properties" in the JRE directory.
      // This configuration file is in standard java.util.Properties format and contains the fully
      // qualified name of the implementation class with the key being the system property defined above.
      PrivilegedAction<Object> action = new PropertyAccessAction("java.home");
      String javaHome = (String)AccessController.doPrivileged(action);
      File jaxmFile = new File(javaHome + "/lib/jaxp.properties");
      if ((Boolean)AccessController.doPrivileged(new PropertyFileExistAction(jaxmFile)))
      {
         String factoryName = null;
         boolean error = false;
         try
         {
            action = new PropertyFileAccessAction(jaxmFile.getCanonicalPath());
            Properties jaxmProperties = (Properties)AccessController.doPrivileged(action);
            factoryName = jaxmProperties.getProperty(PROPERTY_NAME);
         }
         catch (IOException e)
         {
            log.warn("Can't read " + jaxmFile);
            error = true;
         }
         finally
         {
            useJaxpProperty = (error || (factoryName != null));
         }
      }
      else
      {
         useJaxpProperty = false;
      }
   }
   
   private JBossWSDocumentBuilderFactory(DocumentBuilderFactory delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public Object getAttribute(String name) throws IllegalArgumentException
   {
      return fields.get().getAttribute(name);
   }

   @Override
   public boolean getFeature(String name) throws ParserConfigurationException
   {
      return fields.get().getFeature(name);
   }

   /**
    * The creation method for the document builder; it's synchronized to allow us configuring the underlying
    * DocumentBuilderFactory and delegate to it in a thread safe way.
    * 
    */
   @Override
   public synchronized DocumentBuilder newDocumentBuilder() throws ParserConfigurationException
   {
      DocumentBuilderFactoryFields currentFields = fields.get();
      currentFields.copyTo(delegate);
      return delegate.newDocumentBuilder();
   }

   @Override
   public void setAttribute(String name, Object value) throws IllegalArgumentException
   {
      fields.get().setAttribute(name, value);
   }

   @Override
   public void setFeature(String name, boolean value) throws ParserConfigurationException
   {
      fields.get().setFeature(name, value);
   }
   
   @Override
   public boolean isCoalescing()
   {
      return fields.get().isCoalescing();
   }

   @Override
   public void setCoalescing(boolean coalescing)
   {
      fields.get().setCoalescing(coalescing);
   }

   @Override
   public boolean isExpandEntityReferences()
   {
      return fields.get().isExpandEntityReferences();
   }

   @Override
   public void setExpandEntityReferences(boolean expandEntityReferences)
   {
      fields.get().setExpandEntityReferences(expandEntityReferences);
   }

   @Override
   public boolean isIgnoringComments()
   {
      return fields.get().isIgnoringComments();
   }

   @Override
   public void setIgnoringComments(boolean ignoringComments)
   {
      fields.get().setIgnoringComments(ignoringComments);
   }

   @Override
   public boolean isIgnoringElementContentWhitespace()
   {
      return fields.get().isIgnoringElementContentWhitespace();
   }

   @Override
   public void setIgnoringElementContentWhitespace(boolean ignoringElementContentWhitespace)
   {
      fields.get().setIgnoringElementContentWhitespace(ignoringElementContentWhitespace);
   }

   @Override
   public boolean isNamespaceAware()
   {
      return fields.get().isNamespaceAware();
   }

   @Override
   public void setNamespaceAware(boolean namespaceAware)
   {
      fields.get().setNamespaceAware(namespaceAware);
   }

   @Override
   public Schema getSchema()
   {
      return fields.get().getSchema();
   }

   @Override
   public void setSchema(Schema schema)
   {
      fields.get().setSchema(schema);
   }

   @Override
   public boolean isValidating()
   {
      return fields.get().isValidating();
   }

   @Override
   public void setValidating(boolean validating)
   {
      fields.get().setValidating(validating);
   }

   @Override
   public boolean isXIncludeAware()
   {
      return fields.get().isXIncludeAware();
   }

   @Override
   public void setXIncludeAware(boolean includeAware)
   {
      fields.get().setXIncludeAware(includeAware);
   }
   
   /**
    * The {@link DocumentBuilderFactory#newInstance()} documentation defines the retrieval algorithm:
    * 
    * 1) Use the javax.xml.parsers.DocumentBuilderFactory system property.
    * 2) Use the properties file "lib/jaxp.properties" in the JRE directory. This configuration file is in standard java.util.Properties format
    *    and contains the fully qualified name of the implementation class with the key being the system property defined above. The jaxp.properties
    *    file is read only once by the JAXP implementation and it's values are then cached for future use. If the file does not exist when the first
    *    attempt is made to read from it, no further attempts are made to check for its existence. It is not possible to change the value of any
    *    property in jaxp.properties after it has been read for the first time.
    * 3) Use the Services API (as detailed in the JAR specification), if available, to determine the classname. The Services API will look for a
    *    classname in the file META-INF/services/javax.xml.parsers.DocumentBuilderFactory in jars available to the runtime.
    * 4) Platform default DocumentBuilderFactory instance.
    * 
    * So we basically check if 1) or 2) applies: if yes, we simply delegate to the DocumentBuilderFactory, otherwise we first try using our classloader
    * cache and delegate to the DocumentBuilderFactory only in case of a miss in the cache. Then we wrap up the result into a JBossWSDocumentBuilderFactory
    * instance.
    * 
    * @return a DocumentBuilderFactoryInstance
    */
   public static synchronized JBossWSDocumentBuilderFactory newInstance()
   {
      if (useJaxpProperty || getFactoryNameFromSystemProperty() != null)
      {
         return new JBossWSDocumentBuilderFactory(DocumentBuilderFactory.newInstance());
      }
      ClassLoader classLoader = SecurityActions.getContextClassLoader();
      JBossWSDocumentBuilderFactory factory = factoryMap.get(classLoader);
      if (factory == null)
      {
         factory = new JBossWSDocumentBuilderFactory(DocumentBuilderFactory.newInstance());
         factoryMap.put(classLoader, factory);
      }
      return factory;
   }
   
   private static String getFactoryNameFromSystemProperty()
   {
      PrivilegedAction<Object> action = new PropertyAccessAction(PROPERTY_NAME);
      return (String)AccessController.doPrivileged(action);
   }
   
   
   //--------------------------------- Utility privileged actions
   
   private static class PropertyAccessAction implements PrivilegedAction<Object>
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

   private static class PropertyFileAccessAction implements PrivilegedAction<Object>
   {
      private String filename;

      PropertyFileAccessAction(String filename)
      {
         this.filename = filename;
      }

      public Object run()
      {
         InputStream inStream = null;
         try
         {
            inStream = new FileInputStream(filename);
            Properties props = new Properties();
            props.load(inStream);
            return props;
         }
         catch (IOException ex)
         {
            throw new SecurityException("Cannot load properties: " + filename, ex);
         }
         finally
         {
            try
            {
               inStream.close();
            }
            catch (Exception e) {} //ignore
         }
      }
   }
   
   private static class PropertyFileExistAction implements PrivilegedAction<Object>
   {
      private File file;

      PropertyFileExistAction(File file)
      {
         this.file = file;
      }

      public Object run()
      {
         return file.exists();
      }
   }
   
   /**
    * A utility class for storing the document builder factory fields in the ThreadLocal
    */
   private static class DocumentBuilderFactoryFields {
      private boolean coalescing = false;
      private boolean expandEntityReferences = true;
      private boolean ignoringComments = false;
      private boolean ignoringElementContentWhitespace = false;
      private boolean namespaceAware = false;
      private Schema schema = null;
      private boolean validating = false;
      private boolean XIncludeAware = false;
      private Map<String, Object> attributes = new HashMap<String, Object>();
      private Map<String, Boolean> features = new HashMap<String, Boolean>();
      
      public void copyTo(DocumentBuilderFactory target) throws ParserConfigurationException
      {
         target.setCoalescing(coalescing);
         target.setExpandEntityReferences(expandEntityReferences);
         target.setIgnoringComments(ignoringComments);
         target.setIgnoringElementContentWhitespace(ignoringElementContentWhitespace);
         target.setNamespaceAware(namespaceAware);
         target.setSchema(schema);
         target.setValidating(validating);
         target.setXIncludeAware(XIncludeAware);
         for (String key : attributes.keySet())
         {
            target.setAttribute(key, attributes.get(key));
         }
         for (String key : features.keySet())
         {
            target.setFeature(key, features.get(key));
         }
      }
      
      public Object getAttribute(String name)
      {
         return attributes.get(name);
      }
      
      public Boolean getFeature(String name)
      {
         return features.get(name);
      }
      
      public void setAttribute(String key, Object value)
      {
         this.attributes.put(key, value);
      }
      
      public void setFeature(String key, Boolean value)
      {
         this.features.put(key, value);
      }

      public boolean isCoalescing()
      {
         return coalescing;
      }

      public void setCoalescing(boolean coalescing)
      {
         this.coalescing = coalescing;
      }

      public boolean isExpandEntityReferences()
      {
         return expandEntityReferences;
      }

      public void setExpandEntityReferences(boolean expandEntityReferences)
      {
         this.expandEntityReferences = expandEntityReferences;
      }

      public boolean isIgnoringComments()
      {
         return ignoringComments;
      }

      public void setIgnoringComments(boolean ignoringComments)
      {
         this.ignoringComments = ignoringComments;
      }

      public boolean isIgnoringElementContentWhitespace()
      {
         return ignoringElementContentWhitespace;
      }

      public void setIgnoringElementContentWhitespace(boolean ignoringElementContentWhitespace)
      {
         this.ignoringElementContentWhitespace = ignoringElementContentWhitespace;
      }

      public boolean isNamespaceAware()
      {
         return namespaceAware;
      }

      public void setNamespaceAware(boolean namespaceAware)
      {
         this.namespaceAware = namespaceAware;
      }

      public Schema getSchema()
      {
         return schema;
      }

      public void setSchema(Schema schema)
      {
         this.schema = schema;
      }

      public boolean isValidating()
      {
         return validating;
      }

      public void setValidating(boolean validating)
      {
         this.validating = validating;
      }

      public boolean isXIncludeAware()
      {
         return XIncludeAware;
      }

      public void setXIncludeAware(boolean includeAware)
      {
         XIncludeAware = includeAware;
      }
   }

}
