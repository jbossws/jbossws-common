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

import org.jboss.ws.common.Messages;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.Service;

/**
 * A deployer that that calls the endpoint lifecycle handler 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class EndpointLifecycleDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         getLifecycleHandler(ep, true).start(ep);
      }
   }

   @Override
   public void stop(Deployment dep)
   {
      Service service = dep.getService();
      if (service != null)
      {
         for (Endpoint ep : service.getEndpoints())
         {
            LifecycleHandler lifecycleHandler = getLifecycleHandler(ep, false);
            if (lifecycleHandler != null)
               lifecycleHandler.stop(ep);
         }
      }
   }

   protected LifecycleHandler getLifecycleHandler(Endpoint ep, boolean assertHandler)
   {
      LifecycleHandler lifecycleHandler = ep.getLifecycleHandler();
      if (lifecycleHandler == null && assertHandler)
         throw Messages.MESSAGES.lifecycleHandlerNotInitialized(ep.getName());
      
      return lifecycleHandler;
   }

}
