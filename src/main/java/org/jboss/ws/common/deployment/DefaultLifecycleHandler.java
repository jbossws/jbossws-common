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

import org.jboss.ws.common.Loggers;
import org.jboss.ws.common.Messages;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointState;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * A basic lifecycle handler
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public class DefaultLifecycleHandler implements LifecycleHandler
{
   private static DefaultLifecycleHandler me;
   
   public static DefaultLifecycleHandler getInstance()
   {
      if (me == null)
      {
         me = new DefaultLifecycleHandler();
      }
      return me;
   }
   
   public void start(Endpoint ep)
   {
      EndpointState state = ep.getState();
      if (state != EndpointState.UNDEFINED)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotStartEndpoint(state, ep.getName());
      }
      else
      {
         if (ep.getEndpointMetrics() != null)
            ep.getEndpointMetrics().start();

         InvocationHandler invHandler = ep.getInvocationHandler();
         if (invHandler == null)
            throw Messages.MESSAGES.invocationHandlerNotAvailable(ep.getName());
         invHandler.init(ep);

         ep.setState(EndpointState.STARTED);
      }
   }

   public void stop(Endpoint ep)
   {
      EndpointState state = ep.getState();
      if (state == EndpointState.STOPPED) {
    	  //if the endpoint is stopped in EndpointServiceDA
    	  return;
      }
      if (state != EndpointState.STARTED)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotStopEndpoint(state, ep.getName());
      }
      else
      {
         if (ep.getEndpointMetrics() != null)
            ep.getEndpointMetrics().stop();

         ep.setState(EndpointState.STOPPED);
      }
   }

}
