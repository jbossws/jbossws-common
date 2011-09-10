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
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.LifecycleHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.InvocationHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationType;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.invocation.RequestHandlerFactory;

/**
 * An aspect that assigns the handlers to the Endpoint. 
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class EndpointHandlerDeploymentAspect extends AbstractDeploymentAspect
{
   private SPIProvider spiProvider;

   public EndpointHandlerDeploymentAspect()
   {
      spiProvider = SPIProviderResolver.getInstance().getProvider();
   }

   @Override
   public void start(final Deployment dep)
   {
      for (final Endpoint ep : dep.getService().getEndpoints())
      {
         ep.setRequestHandler(getRequestHandler(dep));
         ep.setLifecycleHandler(getLifecycleHandler(dep));
         ep.setInvocationHandler(getInvocationHandler(ep));
      }
   }

   private RequestHandler getRequestHandler(final Deployment dep)
   {
      return spiProvider.getSPI(RequestHandlerFactory.class).newRequestHandler();
   }

   private LifecycleHandler getLifecycleHandler(final Deployment dep)
   {
      return spiProvider.getSPI(LifecycleHandlerFactory.class).newLifecycleHandler();
   }

   private InvocationHandler getInvocationHandler(final Endpoint ep)
   {
      final InvocationType invocationType = InvocationType.valueOf(ep.getType().toString());
      return spiProvider.getSPI(InvocationHandlerFactory.class).newInvocationHandler(invocationType);
   }

}
