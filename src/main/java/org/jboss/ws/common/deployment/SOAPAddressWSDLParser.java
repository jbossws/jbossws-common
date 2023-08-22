/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.common.deployment;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.jboss.ws.common.Messages.MESSAGES;
import static org.jboss.wsf.spi.util.StAXUtils.attributeAsQName;
import static org.jboss.wsf.spi.util.StAXUtils.match;
import static org.jboss.wsf.spi.util.StAXUtils.nextElement;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.wsf.spi.util.StAXUtils;

/**
 * A partial StAX-based WSDL parser for retrieving soap:address elements
 * 
 * @author alessio.soldano@jboss.com
 */
public final class SOAPAddressWSDLParser
{
   public static final String SOAP_OVER_JMS_NS = "http://www.w3.org/2010/soapjms/";
   public static final String SOAP_HTTP_NS = "http://schemas.xmlsoap.org/soap/http";
   private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
   private static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
   private static final String SOAP12_NS = "http://schemas.xmlsoap.org/wsdl/soap12/";
   private static final String DEFINITIONS = "definitions";
   private static final String SERVICE = "service";
   private static final String PORT = "port";
   private static final String BINDING = "binding";
   private static final String IMPORT = "import";
   private static final String TRANSPORT = "transport";
   private static final String ADDRESS = "address";
   private static final String LOCATION = "location";
   private static final String NAME = "name";
   private static final String TARGET_NAMESPACE = "targetNamespace";
   
   private WSDLMetaData metadata;
   
   public SOAPAddressWSDLParser(URL wsdlUrl)
   {
      this.metadata = new WSDLMetaData();
      parse(this.metadata, wsdlUrl);
      Map<String, Boolean> map = this.metadata.getImports();
      while (!map.isEmpty() && map.containsValue(false)) {
         Set<String> imports = new HashSet<String>(map.keySet());
         for (String i : imports) {
            if (!map.get(i)) {
               parse(this.metadata, i);
               map.put(i, true);
            }
         }
      }
   }
   
   public String filterSoapAddress(QName serviceName, QName portName, String[] transportNamespaces)
   {
      WSDLServiceMetaData smd = metadata.getServices().get(serviceName);
      if (smd != null)
      {
         WSDLPortMetaData pmd = smd.getPorts().get(portName);
         if (pmd != null)
         {
            WSDLBindingMetaData bmd = metadata.getBindings().get(pmd.getBindingName());
            if (bmd != null)
            {
               for (String txNs : transportNamespaces)
               {
                  if (txNs.equals(bmd.getSoapTransport()))
                  {
                     return pmd.getSoapAddress();
                  }
               }
            }
         }
      }
      return null;
   }
   
   public String filterSoapAddress(QName serviceName, QName portName, String transportNamespace)
   {
      return filterSoapAddress(serviceName, portName, new String[]{transportNamespace});
   }
   
   protected static void parse(WSDLMetaData metadata, String wsdlUrl)
   {
      try
      {
         parse(metadata, new URL(wsdlUrl));
      }
      catch (MalformedURLException e)
      {
         throw MESSAGES.failedToRead(wsdlUrl, e.getMessage(), e);
      }
   }
   
   protected static void parse(WSDLMetaData metadata, URL wsdlUrl)
   {
      InputStream is = null;
      try
      {
         is = wsdlUrl.openStream();
         XMLStreamReader xmlr = StAXUtils.createXMLStreamReader(is);
         parse(metadata, xmlr, wsdlUrl);
      }
      catch (Exception e)
      {
         throw MESSAGES.failedToRead(wsdlUrl.toExternalForm(), e.getMessage(), e);
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
   
   private static void parse(WSDLMetaData metadata, XMLStreamReader reader, URL wsdlUrl) throws XMLStreamException
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
               parseDefinitions(reader, metadata, targetNS, wsdlUrl);
            }
            else
            {
               throw MESSAGES.unexpectedElement(wsdlUrl.toExternalForm(), reader.getLocalName());
            }
         }
      }
   }
   
   private static void parseDefinitions(XMLStreamReader reader, WSDLMetaData metadata, String targetNS, URL wsdlUrl) throws XMLStreamException
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
                  WSDLServiceMetaData smd = parseService(reader, targetNS, wsdlUrl);
                  smd.setName(name);
                  metadata.getServices().put(smd.getName(), smd);
               }
               else if (match(reader, WSDL_NS, BINDING)) {
                  QName name = attributeAsQName(reader, null, NAME, targetNS);
                  WSDLBindingMetaData bmd = parseBinding(reader, wsdlUrl);
                  bmd.setName(name);
                  metadata.getBindings().put(bmd.getName(), bmd);
               }
               else if (match(reader, WSDL_NS, IMPORT)) {
                  final String location = reader.getAttributeValue(null, LOCATION);
                  try {
                     final String newUrl = new URL(wsdlUrl, location).toExternalForm();
                     if (!metadata.getImports().containsKey(newUrl)) {
                        metadata.getImports().put(newUrl, false);
                     }
                  } catch (MalformedURLException e) {
                     throw MESSAGES.failedToRead(wsdlUrl.toExternalForm(), e.getMessage(), e);
                  }
               }
               continue;
            }
         }
      }
      throw MESSAGES.reachedEndOfXMLDocUnexpectedly(wsdlUrl.toExternalForm());
   }
   
   private static WSDLServiceMetaData parseService(XMLStreamReader reader, String targetNS, URL wsdlUrl) throws XMLStreamException
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
                  WSDLPortMetaData pmd = parsePort(reader, wsdlUrl);
                  pmd.setName(name);
                  pmd.setBindingName(binding);
                  smd.getPorts().put(pmd.getName(), pmd);
               }
               continue;
            }
         }
      }
      throw MESSAGES.reachedEndOfXMLDocUnexpectedly(wsdlUrl.toExternalForm());
   }
   
   private static WSDLPortMetaData parsePort(XMLStreamReader reader, URL wsdlUrl) throws XMLStreamException
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
      throw MESSAGES.reachedEndOfXMLDocUnexpectedly(wsdlUrl.toExternalForm());
   }
   
   private static WSDLBindingMetaData parseBinding(XMLStreamReader reader, URL wsdlUrl) throws XMLStreamException
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
      throw MESSAGES.reachedEndOfXMLDocUnexpectedly(wsdlUrl.toExternalForm());
   }
   
   private static class WSDLMetaData
   {
      private Map<QName, WSDLServiceMetaData> services = new HashMap<QName, SOAPAddressWSDLParser.WSDLServiceMetaData>();
      private Map<QName, WSDLBindingMetaData> bindings = new HashMap<QName, SOAPAddressWSDLParser.WSDLBindingMetaData>();
      private Map<String, Boolean> imports = new HashMap<String, Boolean>(); //<url, processed>
      
      public Map<QName, WSDLServiceMetaData> getServices()
      {
         return services;
      }
      public Map<QName, WSDLBindingMetaData> getBindings()
      {
         return bindings;
      }
      
      public Map<String, Boolean> getImports()
      {
         return imports;
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
