/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.util.xml.JBossEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** 
 * Dynamically register the JBossWS entities.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Aug-2006
 */
public class JBossWSEntityResolver extends JBossEntityResolver
{
   // provide logging
   private static final Logger log = Logger.getLogger(JBossWSEntityResolver.class);

   public JBossWSEntityResolver()
   {
      registerEntity("urn:jboss:jaxrpc-config:2.0", "schema/jaxrpc-config_2_0.xsd");
      registerEntity("urn:jboss:jaxws-config:2.0", "schema/jaxws-config_2_0.xsd");
      registerEntity("http://java.sun.com/xml/ns/javaee", "schema/javaee_web_services_1_2.xsd");
      registerEntity("http://www.w3.org/2005/08/addressing", "schema/ws-addr.xsd");
      registerEntity("http://schemas.xmlsoap.org/ws/2004/08/eventing", "eventing.xsd");
      registerEntity("http://www.w3.org/2002/06/soap-encoding", "soap-encoding_200206.xsd");
      registerEntity("http://schemas.xmlsoap.org/soap/encoding/", "soap-encoding_1_1.xsd");
      registerEntity("http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd", "j2ee_web_services_client_1_1.xsd");
      registerEntity("http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd", "j2ee_web_services_1_1.xsd");
      registerEntity("http://www.ibm.com/webservices/xsd/j2ee_jaxrpc_mapping_1_1.xsd", "j2ee_jaxrpc_mapping_1_1.xsd");
      registerEntity("http://ws-i.org/profiles/basic/1.1/swaref.xsd", "schema/swaref.xsd");
   }

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      if(log.isDebugEnabled()) log.debug("resolveEntity: [pub=" + publicId + ",sysid=" + systemId + "]");
      InputSource inputSource = super.resolveEntity(publicId, systemId);

      if (inputSource == null)
         inputSource = resolveSystemIDAsURL(systemId, log.isTraceEnabled());

      if (inputSource == null)
         log.debug("Cannot resolve entity: [pub=" + publicId + ",sysid=" + systemId + "]");
      
      return inputSource;
   }

   /** Use a ResourceURL to access the resource.
    *  This method should be protected in the super class. */
   protected InputSource resolveSystemIDAsURL(String id, boolean trace)
   {
      if (id == null)
         return null;

      if (trace)
         log.trace("resolveIDAsResourceURL, id=" + id);

      InputSource inputSource = null;

      // Try to use the systemId as a URL to the schema
      try
      {
         if (trace)
            log.trace("Trying to resolve id as a URL");

         URL url = new URL(id);
         if (url.getProtocol().equalsIgnoreCase("file") == false)
            log.warn("Trying to resolve id as a non-file URL: " + id);

         InputStream ins = new ResourceURL(url).openStream();
         if (ins != null)
         {
            inputSource = new InputSource(ins);
            inputSource.setSystemId(id);
         }
         else
         {
            log.warn("Cannot load id as URL: " + id);
         }

         if (trace)
            log.trace("Resolved id as a URL");
      }
      catch (MalformedURLException ignored)
      {
         if (trace)
            log.trace("id is not a url: " + id, ignored);
      }
      catch (IOException e)
      {
         if (trace)
            log.trace("Failed to obtain URL.InputStream from id: " + id, e);
      }
      return inputSource;
   }
}
