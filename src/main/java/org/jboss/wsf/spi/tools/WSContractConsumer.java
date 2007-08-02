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
package org.jboss.wsf.spi.tools;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.wsf.spi.util.ServiceLoader;

/**
 * WSContractConsumer is responsible for generating JAX-WS client and server
 * artifacts from the specified WSDL file. To implement a client, one would use
 * the generated ___Service.java file. For a server, one only needs to provide
 * an implementation class that implements the generated service endpoint
 * interface.
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public abstract class WSContractConsumer
{
   private static String DEFAULT_PROVIDER = "org.jboss.ws.tools.jaxws.impl.SunRIConsumerFactoryImpl";
   public static final String PROVIDER_PROPERTY = "org.jboss.wsf.spi.tools.ConsumerFactoryImpl";

   /**
    * Obtain a new instance of a WSContractProvider. This will use the current
    * thread's context class loader to locate the WSContractProviderFactory
    * implementation.
    *
    * @return a new WSContractProvider
    */
   public static WSContractConsumer newInstance()
   {
      return newInstance(Thread.currentThread().getContextClassLoader());
   }

   /**
    * Obtain a new instance of a WSContractConsumer. The specified ClassLoader will be used to
    * locate the WebServiceImporterProvide implementation
    *
    * @param loader the ClassLoader to use
    * @return a new WSContractConsumer
    */
   public static WSContractConsumer newInstance(ClassLoader loader)
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(loader);
         WSContractConsumerFactory factory = (WSContractConsumerFactory) ServiceLoader.loadService(PROVIDER_PROPERTY, DEFAULT_PROVIDER);
         return factory.createConsumer();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   /**
    * Specifies the JAX-WS and JAXB binding files to use on import operations.
    *
    * @param bindingFiles list of JAX-WS or JAXB binding files
    */
   public abstract void setBindingFiles(List<File> bindingFiles);

   /**
    * Sets the OASIS XML Catalog file to use for entity resolution.
    *
    * @param catalog the OASIS XML Catalog file
    */
   public abstract void setCatalog(File catalog);

   /**
    * Sets the main output directory. If the directory does not exist, it will be created.
    *
    * @param directory the root directory for generated files
    */
   public abstract void setOutputDirectory(File directory);

   /**
    * Sets the source directory. This directory will contain any generated Java source.
    * If the directory does not exist, it will be created. If not specified,
    * the output directory will be used instead.
    *
    * @param directory the root directory for generated source code
    */
   public abstract void setSourceDirectory(File directory);

   /**
    * Enables/Disables Java source generation.
    *
    * @param generateSource whether or not to generate Java source.
    */
   public abstract void setGenerateSource(boolean generateSource);

   /**
    * Sets the target package for generated source. If not specified the default
    * is based off of the XML namespace.
    *
    * @param targetPackage the target package for generated source
    */
   public abstract void setTargetPackage(String targetPackage);

   /**
    * Sets the @@WebService.wsdlLocation and @@WebServiceClient.wsdlLocation attributes to a custom value.
    *
    * @param wsdlLocation the custom WSDL location to use in generated source
    */
   public abstract void setWsdlLocation(String wsdlLocation);

   /**
    * Sets the PrintStream to use for status feedback. The simplest example
    * would be to use System.out.
    *
    * @param messageStream  the stream to use for status messages:
    */
   public abstract void setMessageStream(PrintStream messageStream);

   /**
    * Sets the additional classpath to use if/when invoking the Java compiler.
    * Typically an implementation will use the system <code>java.class.path</code>
    * property. So for most normal applications this method is not needed. However,
    * if this API is being used from an isolated classloader, then it needs to
    * be called in order to reference all jars that are required by the
    * implementation.
    *
    * @param classPath a list of strings where each entry references a
    *                  single jar or directory
    */
   public abstract void setAdditionalCompilerClassPath(List<String> classPath);

   /**
    * Set the target JAX-WS specification target. Defaults to <code>2.0</code>
    * @param target  the JAX-WS specification version. Allowed values are 2.0, 2.1
    */
   public abstract void setTarget(String target);

   /**
    * Generate the required artifacts using the specified WSDL URL. This method
    * may be called more than once, although this is probably not desireable
    * 
    * @param wsdl the URL of the WSDL
    */
   public abstract void consume(URL wsdl);

   /**
    * Generate the required artifacts using the specified WSDL. This method
    * may be called more than once, although this is probably not desireable.
    * The passed string is expect to either be a valid URL, or a local file path.
    *
    * @param wsdl a URL or local file path
    * @throws MalformedURLException if wsdl is not a legal URL or local file
    */
   public void consume(String wsdl) throws MalformedURLException
   {
      URL url = null;
      try
      {
         url = new URL(wsdl);
      }
      catch (MalformedURLException e)
      {
         File file = new File(wsdl);
         url = file.toURL();
      }

      consume(url);
   }
}
