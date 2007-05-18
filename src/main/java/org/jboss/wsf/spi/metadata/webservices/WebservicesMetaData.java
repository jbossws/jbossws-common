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
// $Id$
package org.jboss.wsf.spi.metadata.webservices;

import java.net.URL;
import java.util.ArrayList;

// $Id$

/**
 * XML Binding root element for <code>webservices.xml</code>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class WebservicesMetaData
{
   // The required <webservice-description> elements
   private ArrayList<WebserviceDescriptionMetaData> webserviceDescriptions = new ArrayList<WebserviceDescriptionMetaData>();

   // The URL to the webservices.xml descriptor
   private URL descriptorURL;

   public WebservicesMetaData()
   {
   }

   public WebservicesMetaData(URL descriptorURL)
   {
      this.descriptorURL = descriptorURL;
   }

   public URL getDescriptorURL()
   {
      return descriptorURL;
   }

   public void addWebserviceDescription(WebserviceDescriptionMetaData webserviceDescription)
   {
      webserviceDescriptions.add(webserviceDescription);
   }

   public WebserviceDescriptionMetaData[] getWebserviceDescriptions()
   {
      WebserviceDescriptionMetaData[] array = new WebserviceDescriptionMetaData[webserviceDescriptions.size()];
      webserviceDescriptions.toArray(array);
      return array;
   }

   //Serialize as a String
   public String serialize()
   {
      //Construct the webservices.xml definitions
      StringBuilder buffer = new StringBuilder();

      // header: opening webservices tag
      createHeader(buffer);

      // webservice-description subelements
      for (WebserviceDescriptionMetaData wm : webserviceDescriptions)
         buffer.append(wm.serialize());

      // closing webservices tag
      buffer.append("</webservices>");
      return buffer.toString();
   }

   private void createHeader(StringBuilder buf)
   {
      buf.append("<webservices xmlns='http://java.sun.com/xml/ns/j2ee'");
      buf.append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'");
      buf.append(" xsi:schemaLocation='http://java.sun.com/xml/ns/j2ee http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd'");
      buf.append(" version='1.1'>");
   }
}
