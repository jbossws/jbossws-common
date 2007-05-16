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

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.jboss.ws.integration.invocation.InvocationHandler;

/**
 * A general JAXWS endpoint.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class BasicEndpoint implements Endpoint
{
   private Service service;
   private ObjectName name;
   private Class targetBean;
   private EndpointState state;
   private RequestHandler requestHandler;
   private InvocationHandler invocationHandler;
   private LifecycleHandler lifecycleHandler;
   private Map<Class, Object> metaData = new HashMap<Class, Object>();

   public BasicEndpoint()
   {
      this.state = EndpointState.UNDEFINED;
   }

   public Service getService()
   {
      return service;
   }

   public void setService(Service service)
   {
      this.service = service;
   }

   public Class getTargetBean()
   {
      return targetBean;
   }

   public void setTargetBean(Class targetBean)
   {
      this.targetBean = targetBean;
   }

   public EndpointState getState()
   {
      return state;
   }

   public void setState(EndpointState state)
   {
      this.state = state;
   }

   public ObjectName getName()
   {
      return name;
   }

   public void setName(ObjectName name)
   {
      this.name = name;
   }

   public RequestHandler getRequestHandler()
   {
      return requestHandler;
   }

   public void setRequestHandler(RequestHandler handler)
   {
      this.requestHandler = handler;
   }

   public LifecycleHandler getLifecycleHandler()
   {
      return lifecycleHandler;
   }

   public void setLifecycleHandler(LifecycleHandler handler)
   {
      this.lifecycleHandler = handler;
   }

   public InvocationHandler getInvocationHandler()
   {
      return invocationHandler;
   }

   public void setInvocationHandler(InvocationHandler handler)
   {
      this.invocationHandler = handler;
   }

   public <T> T addAttachment(Class<T> key, Object value)
   {
      return (T)metaData.put(key, value);
   }

   public <T> T getAttachment(Class<T> key)
   {
      return (T)metaData.get(key);
   }

   public <T> T removeAttachment(Class<T> key)
   {
      return (T)metaData.get(key);
   }
}
