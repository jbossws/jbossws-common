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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.serviceref.ServiceRefElement;
import org.w3c.dom.Element;

/** The metdata data from service-ref/port-component-ref element in web.xml, ejb-jar.xml, and application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class UnifiedPortComponentRefMetaData extends ServiceRefElement
{
   // The parent service-ref
   private UnifiedServiceRefMetaData serviceRefMetaData;

   // The required <service-endpoint-interface> element
   private String serviceEndpointInterface;
   // The optional <enable-mtom> element
   private Boolean enableMTOM = Boolean.FALSE;
   // The optional <port-component-link> element
   private String portComponentLink;
   // The optional <port-qname> element
   private QName portQName;
   // Arbitrary proxy properties given by <call-property>
   private List<UnifiedCallPropertyMetaData> callProperties = new ArrayList<UnifiedCallPropertyMetaData>();
   // Arbitrary proxy properties given by <stub-property>
   private List<UnifiedStubPropertyMetaData> stubProperties = new ArrayList<UnifiedStubPropertyMetaData>();
   // The optional JBossWS config-name
   private String configName;
   // The optional JBossWS config-file
   private String configFile;

   public UnifiedPortComponentRefMetaData(UnifiedServiceRefMetaData serviceRefMetaData)
   {
      this.serviceRefMetaData = serviceRefMetaData;
   }

   public void merge(UnifiedPortComponentRefMetaData pcref)
   {
      portQName = pcref.portQName;
      configName = pcref.configName;
      configFile = pcref.configFile;
      callProperties = pcref.callProperties;
      stubProperties = pcref.stubProperties;
   }

   public UnifiedServiceRefMetaData getServiceRefMetaData()
   {
      return serviceRefMetaData;
   }

   public Boolean getEnableMTOM()
   {
      return enableMTOM;
   }

   public void setEnableMTOM(Boolean enableMTOM)
   {
      this.enableMTOM = enableMTOM;
   }

   /** 
    * The port-component-link element links a port-component-ref
    * to a specific port-component required to be made available
    * by a service reference.
    * 
    * The value of a port-component-link must be the
    * port-component-name of a port-component in the same module
    * or another module in the same application unit. The syntax
    * for specification follows the syntax defined for ejb-link
    * in the EJB 2.0 specification.
    */
   public String getPortComponentLink()
   {
      return portComponentLink;
   }

   public void setPortComponentLink(String portComponentLink)
   {
      this.portComponentLink = portComponentLink;
   }

   public String getServiceEndpointInterface()
   {
      return serviceEndpointInterface;
   }

   public void setServiceEndpointInterface(String serviceEndpointInterface)
   {
      this.serviceEndpointInterface = serviceEndpointInterface;
   }

   public QName getPortQName()
   {
      return portQName;
   }

   public void setPortQName(QName portQName)
   {
      this.portQName = portQName;
   }

   public List<UnifiedCallPropertyMetaData> getCallProperties()
   {
      return callProperties;
   }

   public void setCallProperties(List<UnifiedCallPropertyMetaData> callProps)
   {
      callProperties = callProps;
   }

   public void addCallProperty(UnifiedCallPropertyMetaData callProp)
   {
      callProperties.add(callProp);
   }

   public List<UnifiedStubPropertyMetaData> getStubProperties()
   {
      return stubProperties;
   }

   public void setStubProperties(List<UnifiedStubPropertyMetaData> stubProps)
   {
      stubProperties = stubProps;
   }

   public void addStubProperty(UnifiedStubPropertyMetaData stubProp)
   {
      stubProperties.add(stubProp);
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public void importStandardXml(Element root)
   {
      SPIProvider provider = SPIProviderResolver.getInstance().getProvider();
      ServiceRefMetaDataParserFactory factory = provider.getSPI(ServiceRefMetaDataParserFactory.class);
      factory.getServiceRefMetaDataParser().importStandardXml(root, this);
   }

   public void importJBossXml(Element root)
   {
      SPIProvider provider = SPIProviderResolver.getInstance().getProvider();
      ServiceRefMetaDataParserFactory factory = provider.getSPI(ServiceRefMetaDataParserFactory.class);
      factory.getServiceRefMetaDataParser().importJBossXml(root, this);
   }

   public boolean matches(String seiName, QName portName)
   {
      if (seiName == null && portName == null)
         throw new IllegalArgumentException("Cannot match against seiName=null && portName=null.");

      boolean match = false;

      // match against portName first
      if (portName != null)
         match = portName.equals(getPortQName());

      // if it fails try seiName
      if (match == false)
         match = seiName.equals(getServiceEndpointInterface());

      return match;
   }

   public String toString()
   {
      StringBuilder str = new StringBuilder();
      str.append("\nUnifiedPortComponentRef");
      str.append("\n serviceEndpointInterface=" + serviceEndpointInterface);
      str.append("\n portQName=" + portQName);
      str.append("\n enableMTOM=" + enableMTOM);
      str.append("\n portComponentLink=" + portComponentLink);
      str.append("\n callProperties=" + callProperties);
      str.append("\n stubProperties=" + stubProperties);
      str.append("\n configName=" + configName);
      str.append("\n configFile=" + configFile);
      return str.toString();
   }
}
