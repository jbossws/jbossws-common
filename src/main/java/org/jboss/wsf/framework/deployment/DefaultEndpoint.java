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

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.wsf.framework.DefaultExtensible;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * A general JAXWS endpoint.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class DefaultEndpoint extends DefaultExtensible implements Endpoint
{
   private Service service;
   private ObjectName name;
   private String shortName;
   private String urlPattern;
   private String targetBean;
   private EndpointState state;
   private RequestHandler requestHandler;
   private InvocationHandler invocationHandler;
   private LifecycleHandler lifecycleHandler;
   private EndpointMetrics metrics;
   private List<BindingCustomization> bindingCustomizsations = new ArrayList<BindingCustomization>();
   private String address;

   DefaultEndpoint(String targetBean)
   {
      this.targetBean = targetBean;
      this.state = EndpointState.UNDEFINED;
   }

   public Service getService()
   {
      return service;
   }

   public void setService(Service service)
   {
      assertEndpointSetterAccess();
      this.service = service;
   }

   public String getTargetBeanName()
   {
      return targetBean;
   }

   public void setTargetBeanName(String targetBean)
   {
      assertEndpointSetterAccess();
      this.targetBean = targetBean;
   }

   public String getAddress()
   {
      return this.address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   public Class getTargetBeanClass()
   {
      if (targetBean == null)
         throw new IllegalStateException("Target bean not set");

      ClassLoader classLoader = service.getDeployment().getRuntimeClassLoader();
      if (classLoader == null)
         classLoader = service.getDeployment().getInitialClassLoader();
      
      if (classLoader == null)
         throw new IllegalStateException("Deployment classloader not set");

      Class beanClass;
      try
      {
         beanClass = classLoader.loadClass(targetBean);
      }
      catch (ClassNotFoundException ex)
      {
         throw new WSFDeploymentException(ex);
      }
      return beanClass;
   }

   public ObjectName getName()
   {
      return name;
   }

   public void setName(ObjectName name)
   {
      assertEndpointSetterAccess();
      this.name = name;
   }

   public String getShortName()
   {
      return shortName;
   }

   public void setShortName(String shortName)
   {
      assertEndpointSetterAccess();
      this.shortName = shortName;
   }

   public String getURLPattern()
   {
      return urlPattern;
   }

   public void setURLPattern(String urlPattern)
   {
      assertEndpointSetterAccess();
      this.urlPattern = urlPattern;
   }

   public EndpointState getState()
   {
      return state;
   }

   public void setState(EndpointState state)
   {
      this.state = state;
   }

   public RequestHandler getRequestHandler()
   {
      return requestHandler;
   }

   public void setRequestHandler(RequestHandler handler)
   {
      assertEndpointSetterAccess();
      this.requestHandler = handler;
   }

   public LifecycleHandler getLifecycleHandler()
   {
      return lifecycleHandler;
   }

   public void setLifecycleHandler(LifecycleHandler handler)
   {
      assertEndpointSetterAccess();
      this.lifecycleHandler = handler;
   }

   public InvocationHandler getInvocationHandler()
   {
      return invocationHandler;
   }

   public void setInvocationHandler(InvocationHandler handler)
   {
      assertEndpointSetterAccess();
      this.invocationHandler = handler;
   }

   public EndpointMetrics getEndpointMetrics()
   {
      return metrics;
   }

   public void setEndpointMetrics(EndpointMetrics metrics)
   {
      assertEndpointSetterAccess();
      metrics.setEndpoint(this);
      this.metrics = metrics;

   }

   @Override
   public <T> T addAttachment(Class<T> clazz, Object obj)
   {
      assertEndpointSetterAccess();
      return super.addAttachment(clazz, obj);
   }

   @Override
   public <T> T removeAttachment(Class<T> key)
   {
      assertEndpointSetterAccess();
      return super.removeAttachment(key);
   }

   @Override
   public void removeProperty(String key)
   {
      assertEndpointSetterAccess();
      super.removeProperty(key);
   }

   @Override
   public void setProperty(String key, Object value)
   {
      assertEndpointSetterAccess();
      super.setProperty(key, value);
   }

   private void assertEndpointSetterAccess()
   {
      if (state == EndpointState.STARTED)
         throw new IllegalStateException("Cannot modify endpoint properties in state: " + state);
   }
}
