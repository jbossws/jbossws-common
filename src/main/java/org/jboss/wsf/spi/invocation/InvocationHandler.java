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
package org.jboss.wsf.spi.invocation;

//$Id$

import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A general endpoint invocation handler.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public interface InvocationHandler
{
   /** Create the default invokation object */
   Invocation createInvocation();
   
   /** Create the invocation handler */
   void create(Endpoint ep);

   /** Start the invocation handler */
   void start(Endpoint ep);

   /** Invoke the the service endpoint */
   void invoke(Endpoint ep, Object beanInstance, Invocation inv) throws Exception;

   /** Stop the invocation handler */
   void stop(Endpoint ep);

   /** Destroy the invocation handler */
   void destroy(Endpoint ep);
}
