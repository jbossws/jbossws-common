/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.jboss.ws.integration;

import javax.management.ObjectName;

import org.jboss.ws.integration.invocation.InvocationHandler;

/**
 * A general JAXWS endpoint.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public interface Endpoint
{
   static final String SEPID_DOMAIN = "jboss.ws";
   static final String SEPID_PROPERTY_CONTEXT = "context";
   static final String SEPID_PROPERTY_ENDPOINT = "endpoint";

   static final String SEPID_DOMAIN_ENDPOINT = SEPID_DOMAIN + "." + SEPID_PROPERTY_ENDPOINT;

   public enum EndpointState
   {
      UNDEFINED, CREATED, STARTED, STOPED, DESTROYED
   };

   /** Get the service this endpoint belongs to */
   Service getService();

   /** Set the service this endpoint belongs to */
   void setService(Service service);

   /** Get the unique identifier for this endpoint */
   ObjectName getName();

   /** Set the unique identifier for this endpoint */
   void setName(ObjectName epName);

   /** Get the current state for this endpoint */
   EndpointState getState();

   /** Set the current state for this endpoint */
   void setState(EndpointState state);

   /** Get the endpoint implementation bean */
   Class getEndpointImpl();

   /** Set the endpoint implementation bean */
   void setEndpointImpl(Class epImpl);

   /** Set the request handler for this endpoint */
   void setRequestHandler(RequestHandler handler);

   /** Get the request handler for this endpoint */
   RequestHandler getRequestHandler();

   /** Get the lifecycle handler for this endpoint */
   LifecycleHandler getLifecycleHandler();

   /** Set the lifecycle handler for this endpoint */
   void setLifecycleHandler(LifecycleHandler handler);

   /** Get the endpoint bean invoker */
   InvocationHandler getInvocationHandler();

   /** Set the endpoint bean invoker */
   void setInvocationHandler(InvocationHandler invoker);

   /** Add arbitrary meta data */
   <T> T addMetaData(Class<T> key, Object value);

   /** Get arbitrary meta data */
   <T> T getMetaData(Class<T> key);

   /** Remove arbitrary meta data */
   <T> T removeMetaData(Class<T> key);
}
