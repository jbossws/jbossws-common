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
package org.jboss.wsf.spi.management;

// $Id$

import java.util.Set;

import javax.management.ObjectName;

import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A general endpoint registry.
 * 
 * @author Thomas.Diesler@jboss.com
 * @author Heiko.Braun@jboss.com
 * 
 * @since 20-Apr-2007 
 */
public interface EndpointRegistry
{
   /** The bean name in the kernel registry */
   String BEAN_NAME = "WSEndpointRegistry";

   /** Get the list of registered endpoints */
   Set<ObjectName> getEndpoints();

   /** Get the registered endpoint */
   Endpoint getEndpoint(ObjectName epName);

   /** Resolve endpoints thrrough a resolve instance **/
   Endpoint resolve(EndpointResolver resolver);

   /** True is an endpoint for that name is registered */
   boolean isRegistered(ObjectName epName);

   /** Register an endpoint */
   void register(Endpoint endpoint);

   /** Unregister an endpoint */
   void unregister(Endpoint endpoint);
}
