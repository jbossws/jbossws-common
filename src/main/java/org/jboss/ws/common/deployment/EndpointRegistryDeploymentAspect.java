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

import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;

/**
 * A deployer that registers the endpoints 
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class EndpointRegistryDeploymentAspect extends AbstractDeploymentAspect
{
   private EndpointRegistryFactory factory;
   
   public void start(Deployment dep)
   {
      if (factory == null) {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         factory = spiProvider.getSPI(EndpointRegistryFactory.class);
      }
      EndpointRegistry registry = factory.getEndpointRegistry();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         registry.register(ep);
      }
   }

   public void stop(Deployment dep)
   {
      if (factory == null) {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         factory = spiProvider.getSPI(EndpointRegistryFactory.class);
      }
      EndpointRegistry registry = factory.getEndpointRegistry();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         registry.unregister(ep);
      }
   }
   
}
