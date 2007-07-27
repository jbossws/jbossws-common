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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The container independent top level meta data from the jboss.xml and ejb-jar.xml descriptor. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class EJBArchiveMetaData
{
   /** ArrayList<BeanMetaData> for the ejbs */
   private List<EJBMetaData> beans = new ArrayList<EJBMetaData>();
   /** The optional JBossWS config-name */
   private String configName;
   /** The optional JBossWS config-file */
   private String configFile;
   /** The web context root to use for web services */
   private String webServiceContextRoot;
   /** The security-domain value assigned to the application */
   private String securityDomain;
   /** A HashMap<String, String> for webservice description publish locations */
   private PublishLocationAdapter publishLocationAdapter;

   public EJBMetaData getBeanByEjbName(String ejbName)
   {
      for (EJBMetaData beanMetaData : beans)
      {
         if (beanMetaData.getEjbName().equals(ejbName))
         {
            return beanMetaData;
         }
      }
      return null;
   }

   public Iterator<EJBMetaData> getEnterpriseBeans()
   {
      return beans.iterator();
   }

   public void setEnterpriseBeans(List<EJBMetaData> beans)
   {
      this.beans = beans;
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

   public String getWebServiceContextRoot()
   {
      return webServiceContextRoot;
   }

   public void setWebServiceContextRoot(String contextRoot)
   {
      this.webServiceContextRoot = contextRoot;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public void setPublishLocationAdapter(PublishLocationAdapter publishLocationAdapter)
   {
      this.publishLocationAdapter = publishLocationAdapter;
   }

   public String getWsdlPublishLocationByName(String name)
   {
      String publishLocation = (publishLocationAdapter != null ? publishLocationAdapter.getWsdlPublishLocationByName(name) : null);
      return publishLocation;

   }

   public interface PublishLocationAdapter
   {
      String getWsdlPublishLocationByName(String name);
   }
}
