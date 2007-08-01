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
package org.jboss.wsf.spi.metadata.webservices;

// $Id$

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedInitParamMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * A JBossXB factory for {@link WebservicesMetaData}
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Apr-2004
 */
public class WebservicesFactory implements ObjectModelFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(WebservicesFactory.class);

   // The URL to the webservices.xml descriptor
   private URL descriptorURL;

   public WebservicesFactory(URL descriptorURL)
   {
      this.descriptorURL = descriptorURL;
   }

   /**
    * Load webservices.xml from <code>META-INF/webservices.xml</code>
    * or <code>WEB-INF/webservices.xml</code>.
    *
    * @param root virtual file root
    * @return WebservicesMetaData or <code>null</code> if it cannot be found
    */
   public static WebservicesMetaData loadFromVFSRoot(UnifiedVirtualFile root)
   {
      WebservicesMetaData webservices = null;

      UnifiedVirtualFile wsdd = null;
      try
      {
         wsdd = root.findChild("META-INF/webservices.xml");
      }
      catch (IOException e)
      {
         //
      }

      // Maybe a web application deployment?
      if (null == wsdd)
      {
         try
         {
            wsdd = root.findChild("WEB-INF/webservices.xml");
         }
         catch (IOException e)
         {
            //
         }
      }

      // the descriptor is optional
      if (wsdd != null)
      {

         URL wsddUrl = wsdd.toURL();
         try
         {
            InputStream is = wsddUrl.openStream();
            Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
            ObjectModelFactory factory = new WebservicesFactory(wsddUrl);
            webservices = (WebservicesMetaData)unmarshaller.unmarshal(is, factory, null);
            is.close();
         }
         catch (Exception e)
         {
            throw new WebServiceException("Failed to unmarshall webservices.xml:" + e.getMessage());
         }
      }

      return webservices;
   }

   /**
    * This method is called on the factory by the object model builder when the parsing starts.
    *
    * @return the root of the object model.
    */
   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      WebservicesMetaData webservicesMetaData = new WebservicesMetaData(descriptorURL);
      return webservicesMetaData;
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(WebservicesMetaData webservices, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("webservice-description".equals(localName))
         return new WebserviceDescriptionMetaData(webservices);
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(WebservicesMetaData webservices, WebserviceDescriptionMetaData webserviceDescription, UnmarshallingContext navigator, String namespaceURI,
         String localName)
   {
      webservices.addWebserviceDescription(webserviceDescription);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(WebserviceDescriptionMetaData webserviceDescription, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("port-component".equals(localName))
         return new PortComponentMetaData(webserviceDescription);
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(WebserviceDescriptionMetaData webserviceDescription, PortComponentMetaData portComponent, UnmarshallingContext navigator, String namespaceURI,
         String localName)
   {
      webserviceDescription.addPortComponent(portComponent);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(PortComponentMetaData portComponent, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler".equals(localName))
         return new UnifiedHandlerMetaData(null);
      else if ("handler-chains".equals(localName))
         return new UnifiedHandlerChainsMetaData();
      else return null;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerChainsMetaData handlerChains, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler-chain".equals(localName))
         return new UnifiedHandlerChainMetaData();
      else return null;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerChainMetaData handlerChains, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler".equals(localName))
         return new UnifiedHandlerMetaData();
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(PortComponentMetaData portComponent, UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      portComponent.addHandler(handler);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(PortComponentMetaData portComponent, UnifiedHandlerChainsMetaData handlerChains, UnmarshallingContext navigator, String namespaceURI,
         String localName)
   {
      portComponent.setHandlerChains(handlerChains);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerChainsMetaData chains, UnifiedHandlerChainMetaData handlerChain, UnmarshallingContext navigator, String namespaceURI,
         String localName)
   {
      chains.addHandlerChain(handlerChain);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerChainMetaData chain, UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      chain.addHandler(handler);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("init-param".equals(localName))
         return new UnifiedInitParamMetaData();
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerMetaData handler, UnifiedInitParamMetaData param, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      handler.addInitParam(param);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WebserviceDescriptionMetaData webserviceDescription, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("WebserviceDescriptionMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("webservice-description-name"))
         webserviceDescription.setWebserviceDescriptionName(value);
      else if (localName.equals("wsdl-file"))
         webserviceDescription.setWsdlFile(value);
      else if (localName.equals("jaxrpc-mapping-file"))
         webserviceDescription.setJaxrpcMappingFile(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(PortComponentMetaData portComponent, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("PortComponentMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("port-component-name"))
         portComponent.setPortComponentName(value);
      else if (localName.equals("wsdl-port"))
         portComponent.setWsdlPort(navigator.resolveQName(value));
      else if (localName.equals("service-endpoint-interface"))
         portComponent.setServiceEndpointInterface(value);
      else if (localName.equals("ejb-link"))
         portComponent.setEjbLink(value);
      else if (localName.equals("servlet-link"))
         portComponent.setServletLink(value);
      else if (localName.equals("wsdl-service"))
         portComponent.setWsdlService(navigator.resolveQName(value));
      else if (localName.equals("protocol-binding"))
         portComponent.setProtocolBinding(value);
      else if (localName.equals("enable-mtom"))
         portComponent.setEnableMtom(Boolean.valueOf(value));

   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedHandlerMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("handler-name"))
         handler.setHandlerName(value);
      else if (localName.equals("handler-class"))
         handler.setHandlerClass(value);
      else if (localName.equals("soap-header"))
         handler.addSoapHeader(navigator.resolveQName(value));
      else if (localName.equals("soap-role"))
         handler.addSoapRole(value);
      else if (localName.equals("port-name"))
         handler.addPortName(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(UnifiedInitParamMetaData param, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedInitParamMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("param-name"))
         param.setParamName(value);
      else if (localName.equals("param-value"))
         param.setParamValue(value);
   }
}
