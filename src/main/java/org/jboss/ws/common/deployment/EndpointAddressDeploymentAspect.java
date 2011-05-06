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
package org.jboss.ws.common.deployment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBSecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData.JSEResourceCollection;

/**
 * A deployer that assigns the endpoint address. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2007
 */
public class EndpointAddressDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null)
         throw new IllegalStateException("Cannot obtain context root");
      
      // TODO: remove this hack - review API
      String protocol = (String)dep.getService().getProperty("protocol");
      String host = (String)dep.getService().getProperty("host");
      Integer port = (Integer)dep.getService().getProperty("port");
      Integer securePort = null;
      
      if (protocol == null)
      {
         SPIProvider provider = SPIProviderResolver.getInstance().getProvider();
         ServerConfigFactory spi = provider.getSPI(ServerConfigFactory.class);
         ServerConfig serverConfig = spi.getServerConfig();

         host = serverConfig.getWebServiceHost();
         port = serverConfig.getWebServicePort();
         securePort = serverConfig.getWebServiceSecurePort();
      }
      Map<String, Endpoint> endpointsMap = new HashMap<String, Endpoint>();
      List<Endpoint> deleteList = new LinkedList<Endpoint>();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         boolean confidential = isConfidentialTransportGuarantee(dep, ep);
         int currentPort = confidential ? securePort : port;
         String hostAndPort = host + (currentPort > 0 ? ":" + currentPort : ""); 
         
         HttpEndpoint httpEp = (HttpEndpoint)ep;
         String urlPattern = httpEp.getURLPattern();
         if (urlPattern == null)
            throw new IllegalStateException("Cannot obtain url pattern");

         if (urlPattern.endsWith("/*"))
            urlPattern = urlPattern.substring(0, urlPattern.length() - 2);

         protocol = confidential ? "https://" : "http://";
         String address = protocol + hostAndPort + contextRoot + urlPattern;
         httpEp.setAddress(address);
         //JBWS-2957: EJB3 binds the same endpoint class to multiple beans at multiple JNDI locations;
         //generally speaking we can't have multiple endpoints published at the same address and we
         //can't ensure that completely in AS integration, since the publish address is final just here
         if (!endpointsMap.containsKey(address))
         {
            endpointsMap.put(address, httpEp);
         }
         else
         {
            deleteList.add(httpEp);
         }
      }
      //Remove endpoints with duplicated address
      for (Endpoint ep : deleteList)
      {
         dep.getService().getEndpoints().remove(ep);
      }
   }
   
   protected boolean isConfidentialTransportGuarantee(Deployment dep, Endpoint ep)
   {
      String transportGuarantee = null;
      if (DeploymentType.JAXWS_JSE == dep.getType())
      {
         JSEArchiveMetaData webMetaData = dep.getAttachment(JSEArchiveMetaData.class);
         if (webMetaData != null)
         {
            String servletLink = ep.getShortName();
            Map<String, String> servletMappings = webMetaData.getServletMappings();
            String urlPattern = servletMappings.get(servletLink);
   
            if (urlPattern == null)
               throw new RuntimeException("Cannot find <url-pattern> for servlet-name: " + servletLink);
   
            List<JSESecurityMetaData> securityList = webMetaData.getSecurityMetaData();
            for (JSESecurityMetaData currentSecurity : securityList)
            {
               if (currentSecurity.getTransportGuarantee() != null && currentSecurity.getTransportGuarantee().length() > 0)
               {
                  for (JSEResourceCollection currentCollection : currentSecurity.getWebResources())
                  {
                     for (String currentUrlPattern : currentCollection.getUrlPatterns())
                     {
                        if (urlPattern.equals(currentUrlPattern) || (urlPattern + "/*").equals(currentUrlPattern) || "/*".equals(currentUrlPattern))
                        {
                           transportGuarantee = currentSecurity.getTransportGuarantee();
                        }
                     }
                  }
               }
            }
         }
      }
      else if (DeploymentType.JAXWS_EJB3 == dep.getType())
      {
         //TODO Unify annotation scans
         Class implClass = ep.getTargetBeanClass();
         WebContext anWebContext = (WebContext)implClass.getAnnotation(WebContext.class);
         if (anWebContext != null)
         {
            transportGuarantee = anWebContext.transportGuarantee();
         }
         if (anWebContext == null || transportGuarantee == null || transportGuarantee.length() == 0)
         {
            String ejbName = ep.getShortName();
            EJBArchiveMetaData ejbArchiveMD = dep.getAttachment(EJBArchiveMetaData.class);
            EJBMetaData ejbMD = ejbArchiveMD != null ? ejbArchiveMD.getBeanByEjbName(ejbName) : null;
            EJBSecurityMetaData ejbSecurityMD = ejbMD != null ? ejbMD.getSecurityMetaData() : null;
            
            if (ejbSecurityMD != null)
            {
               transportGuarantee = ejbSecurityMD.getTransportGuarantee();
            }
         }
      }
      return "CONFIDENTIAL".equals(transportGuarantee);
   }
   
}
