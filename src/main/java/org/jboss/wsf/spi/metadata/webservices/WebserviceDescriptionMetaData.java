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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;

/**
 * XML Binding element for <code>webservices/webservice-description</code>
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision$
 * @since 15-April-2004
 */
public class WebserviceDescriptionMetaData
{
   // provide logging
   private static final Logger log = Logger.getLogger(WebserviceDescriptionMetaData.class);

   // The parent <webservices> element
   private WebservicesMetaData webservices;

   // The required <webservice-description-name> element
   private String webserviceDescriptionName;
   // The required <wsdl-file> element
   private String wsdlFile;
   // The required <jaxrpc-mapping-file> element
   private String jaxrpcMappingFile;
   // The required <port-component> elements
   private ArrayList<PortComponentMetaData> portComponents = new ArrayList<PortComponentMetaData>();

   public WebserviceDescriptionMetaData(WebservicesMetaData webservices)
   {
      this.webservices = webservices;
   }

   public WebservicesMetaData getWebservices()
   {
      return webservices;
   }

   public void addPortComponent(PortComponentMetaData portComponent)
   {
      portComponents.add(portComponent);
   }

   public PortComponentMetaData[] getPortComponents()
   {
      PortComponentMetaData[] array = new PortComponentMetaData[portComponents.size()];
      portComponents.toArray(array);
      return array;
   }

   /**
    * Get the QNames of the port components to be declared
    * in the namespaces
    * 
    * @return
    */
   public Collection<QName> getPortComponentQNames()
   {
      //TODO:Check if there is just one QName that drives all portcomponents
      //or each port component can have a distinct QName (namespace/prefix)
      //Maintain uniqueness of the QName
      Map<String, QName> map = new HashMap<String, QName>();
      for (PortComponentMetaData pcm : portComponents)
      {
         QName qname = pcm.getWsdlPort();
         map.put(qname.getPrefix(), qname);
      }
      return map.values();
   }

   /**
    * Lookup a PortComponentMetaData by wsdl-port local part
    *
    * @param name - the wsdl-port local part
    * @return PortComponentMetaData if found, null otherwise
    */
   public PortComponentMetaData getPortComponentByWsdlPort(String name)
   {
      ArrayList<String> pcNames = new ArrayList<String>();
      for (PortComponentMetaData pc : portComponents)
      {
         String wsdlPortName = pc.getWsdlPort().getLocalPart();
         if (wsdlPortName.equals(name))
            return pc;

         pcNames.add(wsdlPortName);
      }

      log.error("Cannot get port component name '" + name + "', we have: " + pcNames);
      return null;
   }

   public String getWebserviceDescriptionName()
   {
      return webserviceDescriptionName;
   }

   public void setWebserviceDescriptionName(String webserviceDescriptionName)
   {
      this.webserviceDescriptionName = webserviceDescriptionName;
   }

   public String getWsdlFile()
   {
      return wsdlFile;
   }

   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }

   public String getJaxrpcMappingFile()
   {
      return jaxrpcMappingFile;
   }

   public void setJaxrpcMappingFile(String jaxrpcMappingFile)
   {
      this.jaxrpcMappingFile = jaxrpcMappingFile;
   }

   /**
    * Serialize as a String
    * 
    * @return
    */
   public String serialize()
   {
      StringBuilder buffer = new StringBuilder("<webservice-description>");
      buffer.append("<webservice-description-name>").append(webserviceDescriptionName).append("</webservice-description-name>");
      buffer.append("<wsdl-file>").append(wsdlFile).append("</wsdl-file>");
      buffer.append("<jaxrpc-mapping-file>").append(jaxrpcMappingFile).append("</jaxrpc-mapping-file>");
      for (PortComponentMetaData pm : portComponents)
         buffer.append(pm.serialize());
      buffer.append("</webservice-description>");
      return buffer.toString();
   }
}
