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
package org.jboss.ws.integration;

//$Id$

import org.jboss.logging.Logger;
import org.jboss.ws.integration.Endpoint.EndpointState;
import org.jboss.ws.integration.invocation.InvocationHandler;

/**
 * A basic lifecycle handler
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public class BasicLifecycleHandler implements LifecycleHandler
{
   // provide logging
   protected final Logger log = Logger.getLogger(getClass());

   public void create(Endpoint endpoint)
   {
      log.debug("Create: " + endpoint.getName());

      // Initialize the invoker
      InvocationHandler invoker = endpoint.getInvocationHandler();
      invoker.create(endpoint);

      endpoint.setState(EndpointState.CREATED);
   }

   public void start(Endpoint endpoint)
   {
      log.debug("Start: " + endpoint.getName());

      EndpointState state = endpoint.getState();
      if (state == EndpointState.UNDEFINED || state == EndpointState.DESTROYED)
         throw new IllegalStateException("Cannot start endpoint in state: " + state);

      endpoint.setState(EndpointState.STARTED);
   }

   public void stop(Endpoint endpoint)
   {
      log.debug("Stop: " + endpoint.getName());

      EndpointState state = endpoint.getState();
      if (state != EndpointState.STARTED)
         throw new IllegalStateException("Cannot stop endpoint in state: " + state);

      endpoint.setState(EndpointState.STOPED);
   }

   public void destroy(Endpoint endpoint)
   {
      log.debug("Destroy: " + endpoint.getName());
      endpoint.setState(EndpointState.DESTROYED);
   }
}
