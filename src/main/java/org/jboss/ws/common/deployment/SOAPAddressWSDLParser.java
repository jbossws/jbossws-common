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
package org.jboss.ws.common.deployment;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.jboss.wsf.spi.util.StAXUtils.attributeAsQName;
import static org.jboss.wsf.spi.util.StAXUtils.match;
import static org.jboss.wsf.spi.util.StAXUtils.nextElement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import org.jboss.wsf.spi.util.StAXUtils;

/**
 * A partial StAX-based WSDL parser for retrieving soap:address elements
 * 
 * @author alessio.soldano@jboss.com
 */
public final class SOAPAddressWSDLParser
{
   public static final String SOAP_OVER_JMS_NS = "http://www.w3.org/2010/soapjms/";
   private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
   private static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
   private static final String SOAP12_NS = "http://schemas.xmlsoap.org/wsdl/soap12/";
   private static final String DEFINITIONS = "definitions";
   private static final String SERVICE = "service";
   private static final String PORT = "port";
   private static final String BINDING = "binding";
   private static final String TRANSPORT = "transport";
   private static final String ADDRESS = "address";
   private static final String LOCATION = "location";
   private static final String NAME = "name";
   private static final String TARGET_NAMESPACE = "targetNamespace";
   
   private WSDLMetaData metadata;
   
   public SOAPAddressWSDLParser(URL wsdlUrl)
   {
      this.metadata = getMetaData(wsdlUrl);
   }
   
   public String filterSoapAddress(QName serviceName, QName portName, String transportNamespace)
   {
      //get the soap:address of the required service/port if the corresponding binding uses SOAP over JMS transport
      WSDLServiceMetaData smd = metadata.getServices().get(serviceName);
      if (smd != null)
      {
         WSDLPortMetaData pmd = smd.getPorts().get(portName);
         if (pmd != null)
         {
            WSDLBindingMetaData bmd = metadata.getBindings().get(pmd.getBindingName());
            if (bmd != null && transportNamespace.equals(bmd.getSoapTransport()))
            {
               return pmd.getSoapAddress();
            }
         }
      }
      return null;
   }
   
   protected static WSDLMetaData getMetaData(URL wsdlUrl)
   {
      InputStream is = null;
      try
      {
         is = wsdlUrl.openStream();
         XMLStreamReader xmlr = StAXUtils.createXMLStreamReader(is);
         return getMetaData(xmlr);
      }
      catch (Exception e)
      {
         throw new WebServiceException("Failed to read " + wsdlUrl + ":" + e.getMessage(), e);
      }
      finally
      {
         try
         {
            if (is != null) is.close();
         }
         catch (IOException e) {} //ignore
      }
   }
   
   private static WSDLMetaData getMetaData(XMLStreamReader reader) throws XMLStreamException
   {
      int iterate;
      try
      {
         iterate = reader.nextTag();
      }
      catch (XMLStreamException e)
      {
         // skip non-tag elements
         iterate = reader.nextTag();
      }
      WSDLMetaData metadata = new WSDLMetaData();
      switch (iterate)
      {
         case END_ELEMENT : {
            // we're done
            break;
         }
         case START_ELEMENT : {

            if (match(reader, WSDL_NS, DEFINITIONS))
            {
               String targetNS = reader.getAttributeValue(null, TARGET_NAMESPACE);
               parseDefinitions(reader, metadata, targetNS);
            }
            else
            {
               throw new IllegalStateException("Unexpected element: " + reader.getLocalName());
            }
         }
      }
      return metadata;
   }
   
   private static void parseDefinitions(XMLStreamReader reader, WSDLMetaData metadata, String targetNS) throws XMLStreamException
   {
      while (reader.hasNext())
      {
         switch (nextElement(reader))
         {
            case XMLStreamConstants.END_ELEMENT : {
               if (match(reader, WSDL_NS, DEFINITIONS))
               {
                  return;
               }
               continue;
            }
            case XMLStreamConstants.START_ELEMENT : {
               if (match(reader, WSDL_NS, SERVICE)) {
                  QName name = attributeAsQName(reader, null, NAME, targetNS);
                  WSDLServiceMetaData smd = parseService(reader, targetNS);
                  smd.setName(name);
                  metadata.getServices().put(smd.getName(), smd);
               }
               else if (match(reader, WSDL_NS, BINDING)) {
                  QName name = attributeAsQName(reader, null, NAME, targetNS);
                  WSDLBindingMetaData bmd = parseBinding(reader);
                  bmd.setName(name);
                  metadata.getBindings().put(bmd.getName(), bmd);
               }
               continue;
            }
         }
      }
      throw new IllegalStateException("Reached end of xml document unexpectedly");
   }
   
   private static WSDLServiceMetaData parseService(XMLStreamReader reader, String targetNS) throws XMLStreamException
   {
      WSDLServiceMetaData smd = new WSDLServiceMetaData();
      while (reader.hasNext())
      {
         switch (nextElement(reader))
         {
            case XMLStreamConstants.END_ELEMENT : {
               if (match(reader, WSDL_NS, SERVICE))
               {
                  return smd;
               }
               continue;
            }
            case XMLStreamConstants.START_ELEMENT : {
               if (match(reader, WSDL_NS, PORT)) {
                  QName name = attributeAsQName(reader, null, NAME, targetNS);
                  QName binding = attributeAsQName(reader, null, BINDING, targetNS);
                  WSDLPortMetaData pmd = parsePort(reader);
                  pmd.setName(name);
                  pmd.setBindingName(binding);
                  smd.getPorts().put(pmd.getName(), pmd);
               }
               continue;
            }
         }
      }
      throw new IllegalStateException("Reached end of xml document unexpectedly");
   }
   
   private static WSDLPortMetaData parsePort(XMLStreamReader reader) throws XMLStreamException
   {
      WSDLPortMetaData pmd = new WSDLPortMetaData();
      while (reader.hasNext())
      {
         switch (nextElement(reader))
         {
            case XMLStreamConstants.END_ELEMENT : {
               if (match(reader, WSDL_NS, PORT))
               {
                  return pmd;
               }
               continue;
            }
            case XMLStreamConstants.START_ELEMENT : {
               if (match(reader, SOAP_NS, ADDRESS) || match(reader, SOAP12_NS, ADDRESS)) {
                  String location = reader.getAttributeValue(null, LOCATION);
                  pmd.setSoapAddress(location);
                  reader.nextTag();
               }
               continue;
            }
         }
      }
      throw new IllegalStateException("Reached end of xml document unexpectedly");
   }
   
   private static WSDLBindingMetaData parseBinding(XMLStreamReader reader) throws XMLStreamException
   {
      WSDLBindingMetaData bmd = new WSDLBindingMetaData();
      while (reader.hasNext())
      {
         switch (nextElement(reader))
         {
            case XMLStreamConstants.END_ELEMENT : {
               if (match(reader, WSDL_NS, BINDING))
               {
                  return bmd;
               }
               continue;
            }
            case XMLStreamConstants.START_ELEMENT : {
               if (match(reader, SOAP_NS, BINDING) || match(reader, SOAP12_NS, BINDING)) {
                  String transport = reader.getAttributeValue(null, TRANSPORT);
                  bmd.setSoapTransport(transport);
                  reader.nextTag();
               }
               continue;
            }
         }
      }
      throw new IllegalStateException("Reached end of xml document unexpectedly");
   }
   
   private static class WSDLMetaData
   {
      private Map<QName, WSDLServiceMetaData> services = new HashMap<QName, SOAPAddressWSDLParser.WSDLServiceMetaData>();
      private Map<QName, WSDLBindingMetaData> bindings = new HashMap<QName, SOAPAddressWSDLParser.WSDLBindingMetaData>();
      
      public Map<QName, WSDLServiceMetaData> getServices()
      {
         return services;
      }
      public Map<QName, WSDLBindingMetaData> getBindings()
      {
         return bindings;
      }
   }
   
   private static class WSDLServiceMetaData
   {
      private QName name;
      private Map<QName, WSDLPortMetaData> ports = new HashMap<QName, WSDLPortMetaData>();
      
      public QName getName()
      {
         return name;
      }
      public void setName(QName name)
      {
         this.name = name;
      }
      public Map<QName, WSDLPortMetaData> getPorts()
      {
         return ports;
      }
   }
   
   private static class WSDLPortMetaData
   {
      private String soapAddress;
      private QName name;
      private QName bindingName;
      
      public String getSoapAddress()
      {
         return soapAddress;
      }
      public void setSoapAddress(String soapAddress)
      {
         this.soapAddress = soapAddress;
      }
      public QName getName()
      {
         return name;
      }
      public void setName(QName name)
      {
         this.name = name;
      }
      public QName getBindingName()
      {
         return bindingName;
      }
      public void setBindingName(QName bindingName)
      {
         this.bindingName = bindingName;
      }
   }
   
   private static class WSDLBindingMetaData
   {
      private String soapTransport;
      private QName name;
      
      public String getSoapTransport()
      {
         return soapTransport;
      }
      public void setSoapTransport(String soapTransport)
      {
         this.soapTransport = soapTransport;
      }
      public QName getName()
      {
         return name;
      }
      public void setName(QName name)
      {
         this.name = name;
      }
   }
   
}
