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

// $Id$

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * A handler callback for the EJB21 Invoker
 *
 * @author Thomas.Diesler@jboss.org
 * @since 26-Apr-2007
 */
public interface HandlerCallback
{
   /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
   boolean callRequestHandlerChain(Invocation wsInv, HandlerType type);

   /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
   boolean callResponseHandlerChain(Invocation wsInv, HandlerType type);

   /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
   boolean callFaultHandlerChain(Invocation wsInv, HandlerType type, Exception ex);
}