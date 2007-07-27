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
package org.jboss.wsf.spi.metadata.j2ee;

//$Id$

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The container independent representation of the web.xml and jboss-web.xml deployment descriptors 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class JSEArchiveMetaData
{
   // The war context root as specified at the jboss-web.xml descriptor level.
   private String contextRoot;
   // The servlet-mapping <servlet-name, url-pattern> 
   private Map<String, String> servletMappings = new HashMap<String, String>();
   // The servlet <servlet-name, servlet-class> 
   private Map<String, String> servletClassNames = new HashMap<String, String>();
   // The optional JBossWS config-name 
   private String configName;
   // The optional JBossWS config-file 
   private String configFile;
   // The security-domain value assigned to the application 
   private String securityDomain;
   // A HashMap<String, String> for webservice description publish locations 
   private PublishLocationAdapter publishLocationAdapter;
   // web.xml security-constraints 
   private List<JSESecurityMetaData> securityMetaData;

   public String getContextRoot()
   {
      return contextRoot;
   }

   public void setContextRoot(String contextRoot)
   {
      this.contextRoot = contextRoot;
   }

   public Map<String, String> getServletMappings()
   {
      return servletMappings;
   }

   public void setServletMappings(Map<String, String> servletMappings)
   {
      this.servletMappings = servletMappings;
   }

   public Map<String, String> getServletClassNames()
   {
      return servletClassNames;
   }

   public void setServletClassNames(Map<String, String> servletClassNames)
   {
      this.servletClassNames = servletClassNames;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public List<JSESecurityMetaData> getSecurityMetaData()
   {
      return securityMetaData;
   }

   public void setSecurityMetaData(List<JSESecurityMetaData> securityMetaData)
   {
      this.securityMetaData = securityMetaData;
   }

   public void setPublishLocationAdapter(PublishLocationAdapter publishLocationAdapter)
   {
      this.publishLocationAdapter = publishLocationAdapter;
   }

   public String getWsdlPublishLocationByName(String name)
   {
      String publishLocation = null;
      if (publishLocationAdapter != null)
         publishLocation = publishLocationAdapter.getWsdlPublishLocationByName(name);
      return publishLocation;

   }

   public interface PublishLocationAdapter
   {
      String getWsdlPublishLocationByName(String name);
   }
}
