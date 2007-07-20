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
package org.jboss.wsf.spi.metadata.j2ee.serviceref;

// $Id$

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.xb.QNameBuilder;
import org.w3c.dom.Element;

/**
 * The metdata data from service-ref element in web.xml, ejb-jar.xml, and
 * application-client.xml.
 * 
 * @author Thomas.Diesler@jboss.org
 */
public class ServiceRefMetaDataParser
{
   private static final Logger log = Logger.getLogger(ServiceRefMetaDataParser.class);

   public void importStandardXml(Element root, UnifiedServiceRefMetaData sref)
   {
      sref.setServiceRefName(getElementContent(root, "service-ref-name"));
      sref.setServiceInterface(getOptionalElementContent(root, "service-interface"));
      sref.setWsdlFile(getOptionalElementContent(root, "wsdl-file"));
      sref.setMappingFile(getOptionalElementContent(root, "jaxrpc-mapping-file"));

      Element child = DOMUtils.getFirstChildElement(root, "service-qname");
      if (child != null)
         sref.setServiceQName(QNameBuilder.buildQName(child, getTextContent(child)));

      // Parse the port-component-ref elements
      Iterator iterator = DOMUtils.getChildElements(root, "port-component-ref");
      while (iterator.hasNext())
      {
         Element pcrefElement = (Element)iterator.next();
         UnifiedPortComponentRefMetaData pcrefMetaData = new UnifiedPortComponentRefMetaData(sref);
         pcrefMetaData.importStandardXml(pcrefElement);
         sref.addPortComponentRef(pcrefMetaData);
      }

      // Parse the handler elements
      iterator = DOMUtils.getChildElements(root, "handler");
      while (iterator.hasNext())
      {
         Element handlerElement = (Element)iterator.next();
         UnifiedHandlerMetaData handlerMetaData = new UnifiedHandlerMetaData();
         handlerMetaData.importStandardXml(handlerElement);
         sref.addHandler(handlerMetaData);
      }
   }

   public void importJBossXml(Element root, UnifiedServiceRefMetaData sref)
   {
      sref.setConfigName(getOptionalElementContent(root, "config-name"));
      sref.setConfigFile(getOptionalElementContent(root, "config-file"));
      sref.setWsdlOverride(getOptionalElementContent(root, "wsdl-override"));

      // Parse the port-component-ref elements
      Iterator iterator = DOMUtils.getChildElements(root, "port-component-ref");
      while (iterator.hasNext())
      {
         Element pcrefElement = (Element)iterator.next();
         String seiName = getOptionalElementContent(pcrefElement, "service-endpoint-interface");
         QName portName = getOptionalElementContentAsQName(pcrefElement, "port-qname");

         UnifiedPortComponentRefMetaData pcref = sref.getPortComponentRef(seiName, portName);
         if (pcref == null && seiName != null)
         {
            // Its ok to only have the <port-component-ref> in jboss.xml and not in ejb-jar.xml
            // if it has at least a SEI declared
            pcref = new UnifiedPortComponentRefMetaData(sref);
            pcref.importStandardXml(pcrefElement);
            sref.addPortComponentRef(pcref);
         }

         if (pcref != null)
            pcref.importJBossXml(pcrefElement);
      }

      // Parse the call-property elements
      iterator = DOMUtils.getChildElements(root, "call-property");
      while (iterator.hasNext())
      {
         Element propElement = (Element)iterator.next();
         String name = getElementContent(propElement, "prop-name");
         String value = getElementContent(propElement, "prop-value");
         sref.addCallProperty(new UnifiedCallPropertyMetaData(name, value));
      }
   }

   public void importStandardXml(Element root, UnifiedPortComponentRefMetaData pcref)
   {
      pcref.setServiceEndpointInterface(getOptionalElementContent(root, "service-endpoint-interface"));
      pcref.setPortComponentLink(getOptionalElementContent(root, "port-component-link"));
   }

   public void importJBossXml(Element root, UnifiedPortComponentRefMetaData pcref)
   {
      // Look for call-property elements
      Iterator iterator = DOMUtils.getChildElements(root, "call-property");
      while (iterator.hasNext())
      {
         Element propElement = (Element)iterator.next();
         String name = getElementContent(propElement, "prop-name");
         String value = getElementContent(propElement, "prop-value");
         pcref.addCallProperty(new UnifiedCallPropertyMetaData(name, value));
      }

      // Look for stub-property elements
      iterator = DOMUtils.getChildElements(root, "stub-property");
      while (iterator.hasNext())
      {
         Element propElement = (Element)iterator.next();
         String name = getElementContent(propElement, "prop-name");
         String value = getElementContent(propElement, "prop-value");
         UnifiedStubPropertyMetaData propMetaData = new UnifiedStubPropertyMetaData();
         propMetaData.setPropName(name);
         propMetaData.setPropValue(value);
         pcref.addStubProperty(propMetaData);
      }

      // portQName
      QName portQName = getOptionalElementContentAsQName(root, "port-qname");
      if (portQName != null)
         pcref.setPortQName(portQName);

      // config
      Element configName = DOMUtils.getFirstChildElement(root, "config-name");
      if (configName != null)
         pcref.setConfigName(getTextContent(configName));

      Element configFile = DOMUtils.getFirstChildElement(root, "config-file");
      if (configFile != null)
         pcref.setConfigFile(getTextContent(configFile));

      // service-endpoint-interface
      Element sei = DOMUtils.getFirstChildElement(root, "service-endpoint-interface");
      if (sei != null)
         pcref.setServiceEndpointInterface(getTextContent(sei));

   }

   public void importStandardXml(Element root, UnifiedHandlerMetaData href)
   {
      href.setHandlerName(getElementContent(root, "handler-name"));
      href.setHandlerClass(getElementContent(root, "handler-class"));

      // Parse the init-param elements
      Iterator iterator = DOMUtils.getChildElements(root, "init-param");
      while (iterator.hasNext())
      {
         Element paramElement = (Element)iterator.next();
         UnifiedInitParamMetaData param = new UnifiedInitParamMetaData();
         param.setParamName(getElementContent(paramElement, "param-name"));
         param.setParamValue(getElementContent(paramElement, "param-value"));
         href.addInitParam(param);
      }

      // Parse the soap-header elements
      iterator = DOMUtils.getChildElements(root, "soap-header");
      while (iterator.hasNext())
      {
         Element headerElement = (Element)iterator.next();
         String content = getTextContent(headerElement);
         QName qname = DOMUtils.resolveQName(headerElement, content);
         href.addSoapHeader(qname);
      }

      // Parse the soap-role elements
      iterator = DOMUtils.getChildElements(root, "soap-role");
      while (iterator.hasNext())
      {
         Element roleElement = (Element)iterator.next();
         String content = getTextContent(roleElement);
         href.addSoapRole(content);
      }

      // Parse the port-name elements
      iterator = DOMUtils.getChildElements(root, "port-name");
      while (iterator.hasNext())
      {
         Element portElement = (Element)iterator.next();
         String content = getTextContent(portElement);
         href.addPortName(content);
      }
   }

   private String getElementContent(Element element, String childName)
   {
      String childValue = getOptionalElementContent(element, childName);
      if (childValue == null || childValue.length() == 0)
         throw new IllegalStateException("Invalid null element content: " + childName);

      return childValue;
   }

   private String getOptionalElementContent(Element element, String childName)
   {
      return getTextContent(DOMUtils.getFirstChildElement(element, childName));
   }

   private QName getOptionalElementContentAsQName(Element element, String childName)
   {
      QName qname = null;
      String value = getOptionalElementContent(element, childName);
      if (value != null)
      {
         qname = (value.startsWith("{") ? QName.valueOf(value) : DOMUtils.resolveQName(element, value));
      }
      return qname;
   }

   private String getTextContent(Element element)
   {
      String content = null;
      if (element != null)
      {
         content = DOMUtils.getTextContent(element);
         if (content != null)
            content = content.trim();
      }
      return content;
   }
}
