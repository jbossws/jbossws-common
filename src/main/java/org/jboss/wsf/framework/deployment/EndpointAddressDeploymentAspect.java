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
package org.jboss.wsf.framework.deployment;

//$Id$

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;

/**
 * A deployer that assigns the endpoint address. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2007
 */
public class EndpointAddressDeploymentAspect extends DeploymentAspect
{
   @Override
   public void create(Deployment dep)
   {
      String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null)
         throw new IllegalStateException("Cannot obtain context root");
      
      SPIProvider provider = SPIProviderResolver.getInstance().getProvider();
      ServerConfigFactory spi = provider.getSPI(ServerConfigFactory.class);
      ServerConfig serverConfig = spi.getServerConfig();
      
      String host = serverConfig.getWebServiceHost();
      int port = serverConfig.getWebServicePort();
      String hostAndPort = host + (port > 0 ? ":" + port : ""); 

      for (Endpoint ep : dep.getService().getEndpoints())
      {
         String urlPattern = ep.getURLPattern();
         if (urlPattern == null)
            throw new IllegalStateException("Cannot obtain url pattern");

         if (urlPattern.endsWith("/*"))
            urlPattern = urlPattern.substring(0, urlPattern.length() - 2);

         ep.setAddress("http://" + hostAndPort + contextRoot + urlPattern);
      }
   }
}