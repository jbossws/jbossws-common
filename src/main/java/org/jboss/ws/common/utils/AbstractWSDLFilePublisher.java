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
package org.jboss.ws.common.utils;

import static org.jboss.ws.common.Loggers.DEPLOYMENT_LOGGER;
import static org.jboss.ws.common.Messages.MESSAGES;
import static org.jboss.ws.common.integration.WSHelper.isJseDeployment;
import static org.jboss.ws.common.integration.WSHelper.isWarArchive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.ws.common.Constants;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.IOUtils;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.management.ServerConfig;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract WSDL file publisher
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 *
 */
public abstract class AbstractWSDLFilePublisher
{
   // The deployment info for the web service archive
   protected final ArchiveDeployment dep;
   // The expected wsdl location in the deployment
   protected final String expLocation;
   // The server config
   protected final ServerConfig serverConfig;
   
   private static DocumentBuilder builder;
   
   public AbstractWSDLFilePublisher(ArchiveDeployment dep)
   {
      this.dep = dep;
      
      ServerConfig sc = dep.getAttachment(ServerConfig.class);
      serverConfig = sc != null ? sc : getServerConfig();
      
      if (isJseDeployment(dep) || isWarArchive(dep))
      {
         expLocation = "WEB-INF/wsdl/";
      }
      else
      {
         expLocation = "META-INF/wsdl/";
      }
   }
   
   private static ServerConfig getServerConfig() {
      if(System.getSecurityManager() == null) {
         return AbstractServerConfig.getServerIntegrationServerConfig();
      }
      return AccessController.doPrivileged(AbstractServerConfig.GET_SERVER_INTEGRATION_SERVER_CONFIG);
   }
   
   private static synchronized DocumentBuilder getDocumentBuilder()
   {
      if (builder == null)
      {
         final ClassLoader classLoader = SecurityActions.getContextClassLoader();
         SecurityActions.setContextClassLoader(AbstractWSDLFilePublisher.class.getClassLoader());
         try
         {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            try
            {
               factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (ParserConfigurationException pce)
            {
               DEPLOYMENT_LOGGER.error(pce);
            }
            builder = DOMUtils.newDocumentBuilder(factory);
         }
         finally
         {
            SecurityActions.setContextClassLoader(classLoader);
         }
      }
      return builder;
   }
   
   protected void publishWsdlImports(URL parentURL, Definition parentDefinition, List<String> published) throws Exception
   {
      this.publishWsdlImports(parentURL, parentDefinition, published, expLocation);
   }
   
   /** Publish the wsdl imports for a given wsdl definition
    */
   @SuppressWarnings("unchecked")
   protected void publishWsdlImports(URL parentURL, Definition parentDefinition, List<String> published, String expLocation) throws Exception
   {
      @SuppressWarnings("rawtypes")
      Iterator it = parentDefinition.getImports().values().iterator();
      while (it.hasNext())
      {
         for (Import wsdlImport : (List<Import>)it.next())
         {
            String locationURI = wsdlImport.getLocationURI();
            // its an external import, don't publish locally
            if (locationURI.startsWith("http://") == false && locationURI.startsWith("https://") == false)
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
               
               String baseURI = parentURL.toExternalForm();
               URL targetURL = new URL(baseURI.substring(0, baseURI.lastIndexOf("/") + 1) + locationURI);
               File targetFile = new File(targetURL.getFile()); //JBWS-3488
               createParentDir(targetFile);

               Definition subdef = wsdlImport.getDefinition();
               WSDLFactory wsdlFactory = WSDLFactory.newInstance();
               javax.wsdl.xml.WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
               FileWriter fw = new FileWriter(targetFile);
               wsdlWriter.writeWSDL(subdef, fw);
               fw.close();

               DEPLOYMENT_LOGGER.wsdlImportPublishedTo(targetURL);

               // recursively publish imports
               publishWsdlImports(targetURL, subdef, published, expLocation);

               // Publish XMLSchema imports
               Element subdoc = DOMUtils.parse(targetURL.openStream(), getDocumentBuilder());
               publishSchemaImports(targetURL, subdoc, published, expLocation);
            }
         }
      }
   }
   
   protected void createParentDir(File targetFile)
   {
      File parentFile = targetFile.getParentFile();
      if (parentFile != null) {
         if (!parentFile.mkdirs()) {
            ; // exception will be thrown later in this code
         }
      }
   }
   
   protected void publishSchemaImports(URL parentURL, Element element, List<String> published) throws Exception
   {
      this.publishSchemaImports(parentURL, element, published, expLocation);
   }

   /** Publish the schema imports for a given wsdl definition
    */
   protected void publishSchemaImports(URL parentURL, Element element, List<String> published, String expLocation) throws Exception
   {
      Element childElement = getFirstChildElement(element);
      while (childElement != null) {
         //first check on namespace only to avoid doing anything on any other wsdl/schema elements
         final String ns = childElement.getNamespaceURI();
         if (Constants.NS_SCHEMA_XSD.equals(ns)) {
            final String ln = childElement.getLocalName();
            if ("import".equals(ln) || "include".equals(ln)) {
               String schemaLocation = childElement.getAttribute("schemaLocation");
               if (schemaLocation.length() > 0 && schemaLocation.startsWith("http://") == false)
               {
                  // infinity loops prevention
                  if (!published.contains(schemaLocation))
                  {
                     published.add(schemaLocation);
                     String baseURI = parentURL.toExternalForm();
                     URL xsdURL = new URL(baseURI.substring(0, baseURI.lastIndexOf("/") + 1) + schemaLocation);
                     File targetFile = new File(xsdURL.getFile()); //JBWS-3488
                     createParentDir(targetFile);

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
                     InputStream is = new ResourceURL(resourceURL).openStream();
                     if (is == null)
                        throw MESSAGES.cannotFindSchemaImportInDeployment(resourcePath, deploymentName);

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

                     DEPLOYMENT_LOGGER.xmlSchemaImportPublishedTo(xsdURL);

                     // recursively publish imports
                     Element subdoc = DOMUtils.parse(xsdURL.openStream(), getDocumentBuilder());
                     publishSchemaImports(xsdURL, subdoc, published, expLocation);
                  }
               }
            } else if ("schema".equals(ln)) {
               //recurse, as xsd:schema might contain an import
               publishSchemaImports(parentURL, childElement, published, expLocation);
            }
         } else if (Constants.NS_WSDL11.equals(ns) && "types".equals(childElement.getLocalName())) {
            //recurse as wsdl:types might contain a schema
            publishSchemaImports(parentURL, childElement, published, expLocation);
         }
         childElement = getNextSiblingElement(childElement);
      }
   }
   
   private static Element getFirstChildElement(Node node) {
      Node fc = node.getFirstChild();
      Element e = null;
      if (fc != null) {
         if (fc.getNodeType() == Node.ELEMENT_NODE) {
            e = (Element)fc;
         } else{
            e = getNextSiblingElement(fc);
         }
      }
      return e;
   }
   
   private static Element getNextSiblingElement(Node node) {
      Element e = null;
      Node nextSibling = node.getNextSibling();
      while (e == null && nextSibling != null) {
         if (nextSibling.getNodeType() == Node.ELEMENT_NODE) {
            e = (Element)nextSibling;
         }
         nextSibling = nextSibling.getNextSibling();
      }
      return e;
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
               DEPLOYMENT_LOGGER.cannotDeletePublishedWsdlDoc(file.toURI().toURL());
         }
      }

      // delete the directory as well
      if (dir.delete() == false) {
         DEPLOYMENT_LOGGER.cannotDeletePublishedWsdlDoc(dir.toURI().toURL());
      }
   }
}
