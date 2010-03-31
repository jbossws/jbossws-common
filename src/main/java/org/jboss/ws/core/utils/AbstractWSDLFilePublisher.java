/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.factory.WSDLFactory;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.IOUtils;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.w3c.dom.Element;

/**
 * Abstract WSDL file publisher
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 *
 */
public abstract class AbstractWSDLFilePublisher
{
   private static final Logger log = Logger.getLogger(AbstractWSDLFilePublisher.class);
   
   // The deployment info for the web service archive
   protected ArchiveDeployment dep;
   // The expected wsdl location in the deployment
   protected String expLocation;
   // The server config
   protected ServerConfig serverConfig;
   
   public AbstractWSDLFilePublisher(ArchiveDeployment dep)
   {
      this.dep = dep;
      
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      serverConfig = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
      
      if (dep.getType().toString().endsWith("JSE"))
      {
         expLocation = "WEB-INF/wsdl/";
      }
      else
      {
         expLocation = "META-INF/wsdl/";
      }
   }
   
   /** Publish the wsdl imports for a given wsdl definition
    */
   @SuppressWarnings("unchecked")
   protected void publishWsdlImports(URL parentURL, Definition parentDefinition, List<String> published) throws Exception
   {
      String baseURI = parentURL.toExternalForm();

      Iterator it = parentDefinition.getImports().values().iterator();
      while (it.hasNext())
      {
         for (Import wsdlImport : (List<Import>)it.next())
         {
            String locationURI = wsdlImport.getLocationURI();
            Definition subdef = wsdlImport.getDefinition();

            // its an external import, don't publish locally
            if (locationURI.startsWith("http://") == false)
            {
               // infinity loops prevention
               if (published.contains(locationURI))
               {
                  continue;
               }
               else
               {
                  published.add(locationURI);
               }
               
               URL targetURL = new URL(baseURI.substring(0, baseURI.lastIndexOf("/") + 1) + locationURI);
               File targetFile = new File(targetURL.getPath());
               targetFile.getParentFile().mkdirs();

               WSDLFactory wsdlFactory = WSDLFactory.newInstance();
               javax.wsdl.xml.WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
               FileWriter fw = new FileWriter(targetFile);
               wsdlWriter.writeWSDL(subdef, fw);
               fw.close();

               log.debug("WSDL import published to: " + targetURL);

               // recursively publish imports
               publishWsdlImports(targetURL, subdef, published);

               // Publish XMLSchema imports
               Element subdoc = DOMUtils.parse(targetURL.openStream());
               publishSchemaImports(targetURL, subdoc, published);
            }
         }
      }
   }

   /** Publish the schema imports for a given wsdl definition
    */
   protected void publishSchemaImports(URL parentURL, Element element, List<String> published) throws Exception
   {
      String baseURI = parentURL.toExternalForm();

      Iterator<Element> it = DOMUtils.getChildElements(element);
      while (it.hasNext())
      {
         Element childElement = (Element)it.next();
         if ("import".equals(childElement.getLocalName()) || "include".equals(childElement.getLocalName()))
         {
            String schemaLocation = childElement.getAttribute("schemaLocation");
            if (schemaLocation.length() > 0)
            {
               if (schemaLocation.startsWith("http://") == false)
               {
                  // infinity loops prevention
                  if (published.contains(schemaLocation))
                  {
                     continue;
                  }
                  else
                  {
                     published.add(schemaLocation);
                  }
                  
                  URL xsdURL = new URL(baseURI.substring(0, baseURI.lastIndexOf("/") + 1) + schemaLocation);
                  File targetFile = new File(xsdURL.getPath());
                  targetFile.getParentFile().mkdirs();

                  String deploymentName = dep.getCanonicalName();

                  // get the resource path including the separator
                  int index = baseURI.indexOf(deploymentName) + 1;
                  String resourcePath = baseURI.substring(index + deploymentName.length());
                  //check for sub-directories
                  resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf("/") + 1);

                  resourcePath = expLocation + resourcePath + schemaLocation;
                  while (resourcePath.indexOf("//") != -1)
                  {
                     resourcePath = resourcePath.replace("//", "/");
                  }
                  URL resourceURL = dep.getResourceResolver().resolve(resourcePath);
//                  URL resourceURL = dep.getMetaDataFileURL(resourcePath);
                  InputStream is = new ResourceURL(resourceURL).openStream();
                  if (is == null)
                     throw new IllegalArgumentException("Cannot find schema import in deployment: " + resourcePath);

                  FileOutputStream fos = null;
                  try
                  {
                     fos = new FileOutputStream(targetFile);
                     IOUtils.copyStream(fos, is);
                  }
                  finally
                  {
                     if (fos != null) fos.close();
                  }

                  log.debug("XMLSchema import published to: " + xsdURL);

                  // recursively publish imports
                  Element subdoc = DOMUtils.parse(xsdURL.openStream());
                  publishSchemaImports(xsdURL, subdoc, published);
               }
            }
         }
         else
         {
            publishSchemaImports(parentURL, childElement, published);
         }
      }
   }
   
   /**
    * Delete the published wsdl
    */
   public void unpublishWsdlFiles() throws IOException
   {
      String deploymentDir = (dep.getParent() != null ? dep.getParent().getSimpleName() : dep.getSimpleName());

      File serviceDir = new File(serverConfig.getServerDataDir().getCanonicalPath() + "/wsdl/" + deploymentDir);
      deleteWsdlPublishDirectory(serviceDir);
   }

   /**
    * Delete the published wsdl document, traversing down the dir structure
    */
   protected void deleteWsdlPublishDirectory(File dir) throws IOException
   {
      String[] files = dir.list();
      for (int i = 0; files != null && i < files.length; i++)
      {
         String fileName = files[i];
         File file = new File(dir + "/" + fileName);
         if (file.isDirectory())
         {
            deleteWsdlPublishDirectory(file);
         }
         else
         {
            if (file.delete() == false)
               log.warn("Cannot delete published wsdl document: " + file.toURL());
         }
      }

      // delete the directory as well
      dir.delete();
   }
}
