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

import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.SPIProvider;
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
      spiProvider = SPIProvider.getInstance();
   }

   @Override
   public void start(final Deployment dep)
   {
      final RequestHandler reqHandler = getRequestHandler();
      final LifecycleHandler lcHandler = getLifecycleHandler();
      for (final Endpoint ep : dep.getService().getEndpoints())
      {
         ep.setRequestHandler(reqHandler);
         ep.setLifecycleHandler(lcHandler);
         ep.setInvocationHandler(getInvocationHandler(ep));
      }
   }

   private RequestHandler getRequestHandler()
   {
      return spiProvider.getSPI(RequestHandlerFactory.class).getRequestHandler();
   }

   private LifecycleHandler getLifecycleHandler()
   {
      return spiProvider.getSPI(LifecycleHandlerFactory.class).getLifecycleHandler();
   }

   private InvocationHandler getInvocationHandler(final Endpoint ep)
   {
      final InvocationType invocationType = InvocationType.valueOf(ep.getType().toString());
      return spiProvider.getSPI(InvocationHandlerFactory.class).newInvocationHandler(invocationType);
   }

}
