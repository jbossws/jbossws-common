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
public class LifecycleHandlerImpl implements LifecycleHandler
{
   // provide logging
   protected final Logger log = Logger.getLogger(getClass());

   public void create(Endpoint ep)
   {
      log.debug("Create: " + ep.getName());

      InvocationHandler invHandler = ep.getInvocationHandler();
      if (invHandler == null)
         throw new IllegalStateException("Invocation handler not available");

      invHandler.init(ep);
      
      ep.setState(EndpointState.CREATED);
   }

   public void start(Endpoint ep)
   {
      log.debug("Start: " + ep.getName());

      EndpointState state = ep.getState();
      if (state == EndpointState.UNDEFINED || state == EndpointState.DESTROYED)
         throw new IllegalStateException("Cannot start endpoint in state: " + state);

      if (ep.getEndpointMetrics() != null)
         ep.getEndpointMetrics().start();

      ep.setState(EndpointState.STARTED);
   }

   public void stop(Endpoint ep)
   {
      log.debug("Stop: " + ep.getName());

      EndpointState state = ep.getState();
      if (state != EndpointState.STARTED)
         throw new IllegalStateException("Cannot stop endpoint in state: " + state);

      if (ep.getEndpointMetrics() != null)
         ep.getEndpointMetrics().stop();

      ep.setState(EndpointState.STOPPED);
   }

   public void destroy(Endpoint ep)
   {
      log.debug("Destroy: " + ep.getName());

      ep.setState(EndpointState.DESTROYED);
   }
}
