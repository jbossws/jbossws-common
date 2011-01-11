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
package org.jboss.wsf.framework.deployment;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint.EndpointState;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * A basic lifecycle handler
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public class DefaultLifecycleHandler implements LifecycleHandler
{
   // provide logging
   protected final Logger log = Logger.getLogger(getClass());

   public void start(Endpoint ep)
   {
      if (log.isTraceEnabled())
         log.trace("Start: " + ep.getName());

      EndpointState state = ep.getState();
      if (state != EndpointState.UNDEFINED)
      {
         log.error("Cannot start endpoint in state: " + state);
      }
      else
      {
         if (ep.getEndpointMetrics() != null)
            ep.getEndpointMetrics().start();

         InvocationHandler invHandler = ep.getInvocationHandler();
         if (invHandler == null)
            throw new IllegalStateException("Invocation handler not available");
         invHandler.init(ep);

         ep.setState(EndpointState.STARTED);
      }
   }

   public void stop(Endpoint ep)
   {
      if (log.isTraceEnabled())
         log.trace("Stop: " + ep.getName());

      EndpointState state = ep.getState();
      if (state != EndpointState.STARTED)
      {
         log.error("Cannot stop endpoint in state: " + state);
      }
      else
      {
         if (ep.getEndpointMetrics() != null)
            ep.getEndpointMetrics().stop();

         ep.setState(EndpointState.STOPPED);
      }
   }

}
