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
import org.jboss.wsf.spi.deployment.Endpoint.EndpointType;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.LifecycleHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.InvocationHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationType;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.invocation.RequestHandlerFactory;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.MDBMetaData;

/**
 * A deployer that assigns the handlers to the Endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class EndpointHandlerDeploymentAspect extends AbstractDeploymentAspect
{
   private SPIProvider spiProvider;

   public EndpointHandlerDeploymentAspect()
   {
      super();
      spiProvider = SPIProviderResolver.getInstance().getProvider();
   }

   @Override
   public void start(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         // Associate a request handler
         ep.setRequestHandler(getRequestHandler(dep));

         // Associate a lifecycle handler
         ep.setLifecycleHandler(getLifecycleHandler(dep));

         // Associate an invocation handler
         // Invocation handlers are assigned per container or per stack
         InvocationHandler invocationHandler = getInvocationHandler(ep);
         if (invocationHandler != null)
            ep.setInvocationHandler(invocationHandler);
         
      }
   }

   private RequestHandler getRequestHandler(Deployment dep)
   {
      return spiProvider.getSPI(RequestHandlerFactory.class).newRequestHandler();
   }

   private LifecycleHandler getLifecycleHandler(Deployment dep)
   {
      return spiProvider.getSPI(LifecycleHandlerFactory.class).newLifecycleHandler();
   }

   private InvocationHandler getInvocationHandler(Endpoint ep)
   {
      
      String key = ep.getType().toString();

      // Use a special key for MDB endpoints
      EJBArchiveMetaData uapp = ep.getService().getDeployment().getAttachment(EJBArchiveMetaData.class);
      if (uapp != null)
      {
         EJBMetaData bmd = uapp.getBeanByEjbName(ep.getShortName());
         if (ep.getType() == EndpointType.JAXRPC_EJB21 && bmd instanceof MDBMetaData)
         {
            key = InvocationType.JAXRPC_MDB21.toString();
         }
         else if (ep.getType() == EndpointType.JAXWS_EJB3 && bmd instanceof MDBMetaData)
         {
            key = InvocationType.JAXWS_MDB3.toString();
         }
      }

      InvocationType type = InvocationType.valueOf(key);
      InvocationHandler invocationHandler = spiProvider.getSPI(InvocationHandlerFactory.class).newInvocationHandler(type);
      return invocationHandler;
   }
}
