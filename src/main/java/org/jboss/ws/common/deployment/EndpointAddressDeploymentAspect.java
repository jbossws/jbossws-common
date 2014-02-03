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

import static org.jboss.ws.common.integration.WSHelper.isJaxrpcDeployment;
import static org.jboss.ws.common.integration.WSHelper.isJaxwsEjbEndpoint;
import static org.jboss.ws.common.integration.WSHelper.isJaxwsJseEndpoint;

import java.security.AccessController;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.common.Messages;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.management.ServerConfig;
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
 * @author alessio.soldano@jboss.com
 * @since 19-May-2007
 */
public class EndpointAddressDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      final Service service = dep.getService();
      String contextRoot = service.getContextRoot();
      if (contextRoot == null)
         throw Messages.MESSAGES.cannotObtainContextRoot(dep.getSimpleName());
      
      PortValue port = new PortValue((Integer)service.getProperty("port"), null);
      ServerConfig serverConfig = getServerConfig();
      port.setServerConfig(serverConfig);
      String host = serverConfig.getWebServiceHost();
      Map<String, Endpoint> endpointsMap = new HashMap<String, Endpoint>();
      List<Endpoint> deleteList = new LinkedList<Endpoint>();
      for (Endpoint ep : service.getEndpoints())
      {
         if (ep instanceof HttpEndpoint)
         {
            boolean confidential = isConfidentialTransportGuarantee(dep, ep);
            int currentPort = port.getValue(confidential);
            String hostAndPort = host + (currentPort > 0 ? ":" + currentPort : ""); 
            
            HttpEndpoint httpEp = (HttpEndpoint)ep;
            String urlPattern = httpEp.getURLPattern();
            if (urlPattern == null)
               throw Messages.MESSAGES.cannotObtainUrlPattern(ep.getName());
   
            if (urlPattern.endsWith("/*"))
               urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
   
            String protocol = confidential ? "https://" : "http://";
            String address = protocol + hostAndPort + (contextRoot.equals("/") && urlPattern.startsWith("/") ? "" : contextRoot) + urlPattern;
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
      }
      //Remove endpoints with duplicated address
      for (Endpoint ep : deleteList)
      {
         service.getEndpoints().remove(ep);
      }
   }
   
   private static ServerConfig getServerConfig() {
      if(System.getSecurityManager() == null) {
         return AbstractServerConfig.getServerIntegrationServerConfig();
      }
      return AccessController.doPrivileged(AbstractServerConfig.GET_SERVER_INTEGRATION_SERVER_CONFIG);
   }
   
   protected boolean isConfidentialTransportGuarantee(final Deployment dep, final Endpoint ep)
   {
      if (isJaxrpcDeployment(dep)) return false;

      String transportGuarantee = null;
      if (isJaxwsJseEndpoint(ep))
      {
         JSEArchiveMetaData webMetaData = dep.getAttachment(JSEArchiveMetaData.class);
         if (webMetaData != null)
         {
            String servletLink = ep.getShortName();
            Map<String, String> servletMappings = webMetaData.getServletMappings();
            String urlPattern = servletMappings.get(servletLink);
   
            if (urlPattern == null)
               throw Messages.MESSAGES.cannotFindUrlPatternForServletName(servletLink);
   
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
      else if (isJaxwsEjbEndpoint(ep))
      {
         //TODO Unify annotation scans
         Class<?> implClass = ep.getTargetBeanClass();
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
   
   private static class PortValue {
      private ServerConfig config;
      private Integer port;
      private Integer securePort;
      
      public PortValue(Integer port, Integer securePort) {
         this.port = port;
         this.securePort = securePort;
      }
      
      public void setServerConfig(ServerConfig config)
      {
         this.port = null;
         this.securePort = null;
         this.config = config;
      }
      
      public Integer getValue(boolean confidential) {
         return confidential ? getSecurePortValue() : getPortValue();
      }
      
      public Integer getPortValue()
      {
         if (this.port == null && this.config != null)
         {
            this.port = this.config.getWebServicePort();
         }
         return this.port;
      }
      
      public Integer getSecurePortValue()
      {
         if (this.securePort == null && this.config != null)
         {
            this.securePort = this.config.getWebServiceSecurePort();
         }
         return this.securePort;
      }
   }
}
