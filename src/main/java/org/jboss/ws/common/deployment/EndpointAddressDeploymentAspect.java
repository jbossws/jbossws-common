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

import static org.jboss.ws.common.integration.WSHelper.isJaxwsEjbEndpoint;
import static org.jboss.ws.common.integration.WSHelper.isJaxwsJseEndpoint;

import java.security.AccessController;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.common.Loggers;
import org.jboss.ws.common.Messages;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBSecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData.JSEResourceCollection;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;

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
      // prepare the WSDL soap:address metadata and attach it to the deployment for later usage
      final SOAPAddressRewriteMetadata sarm = new SOAPAddressRewriteMetadata(getServerConfig(),
            dep.getAttachment(JBossWebservicesMetaData.class));
      dep.addAttachment(SOAPAddressRewriteMetadata.class, sarm);

      final Service service = dep.getService();
      String contextRoot = service.getContextRoot();
      if (contextRoot == null)
         throw Messages.MESSAGES.cannotObtainContextRoot(dep.getSimpleName());

      PortValue port = new PortValue((Integer)service.getProperty("port"), null);
      port.setSOAPAddressRewriteMetadata(sarm);
      String host = sarm.getWebServiceHost();
      Map<String, Endpoint> endpointsMap = new HashMap<String, Endpoint>();
      List<Endpoint> deleteList = new LinkedList<Endpoint>();
      for (Endpoint ep : service.getEndpoints())
      {
         if (ep instanceof HttpEndpoint)
         {
            boolean confidential = isConfidentialTransportGuarantee(dep, ep);
            int currentPort = port.getValue(confidential);
            String hostAndPort = host + (currentPort > 0 ? ":" + currentPort : "");
            if (ep.getService().getVirtualHost() != null)
            {
               String hostName = getServerConfig().getHostAlias(ep.getService().getVirtualHost());
               if (hostName == null) {
                  Loggers.DEPLOYMENT_LOGGER.cannotObtainHost(ep.getService().getVirtualHost());
                  hostName = host;
               }
               Integer hostPort = getServerConfig().getVirtualHostPort(ep.getService().getVirtualHost(), confidential);
               if (hostPort == null) {
                  Loggers.DEPLOYMENT_LOGGER.cannotObtainPort(ep.getService().getVirtualHost());
                  hostPort = currentPort;
               }
               hostAndPort = hostName + ":" + hostPort;
            }
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
         service.removeEndpoint(ep);
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
      // Fix for JBWS-4196/WFLY-12135. We pick up the attached boolean. If it's true then isConfidentialTransportGuarantee is true
      if (dep.getProperty("isHttpsOnly") != null && (boolean)dep.getProperty("isHttpsOnly")) {
         return true;
      }
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

         String ejbName = ep.getShortName();
         EJBArchiveMetaData ejbArchiveMD = dep.getAttachment(EJBArchiveMetaData.class);
         EJBMetaData ejbMD = ejbArchiveMD != null ? ejbArchiveMD.getBeanByEjbName(ejbName) : null;
         EJBSecurityMetaData ejbSecurityMD = ejbMD != null ? ejbMD.getSecurityMetaData() : null;

         if (ejbSecurityMD != null )
         {
           transportGuarantee = ejbSecurityMD.getTransportGuarantee();
           if(transportGuarantee == null || transportGuarantee.length() == 0)
           {
              return "CONFIDENTIAL".equals(transportGuarantee);
           }
         }
         else if (anWebContext != null)
         {
           transportGuarantee = anWebContext.transportGuarantee();
         }
      }
      return "CONFIDENTIAL".equals(transportGuarantee);
   }

   private static class PortValue {
      private SOAPAddressRewriteMetadata sarm;
      private Integer port;
      private Integer securePort;

      public PortValue(Integer port, Integer securePort) {
         this.port = port;
         this.securePort = securePort;
      }

      public void setSOAPAddressRewriteMetadata(SOAPAddressRewriteMetadata sarm)
      {
         this.port = null;
         this.securePort = null;
         this.sarm = sarm;
      }

      public Integer getValue(boolean confidential) {
         return confidential ? getSecurePortValue() : getPortValue();
      }

      public Integer getPortValue()
      {
         if (this.port == null && this.sarm != null)
         {
            this.port = this.sarm.getWebServicePort();
         }
         return this.port;
      }

      public Integer getSecurePortValue()
      {
         if (this.securePort == null && this.sarm != null)
         {
            this.securePort = this.sarm.getWebServiceSecurePort();
         }
         return this.securePort;
      }
   }
}
