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

import java.lang.reflect.Method;

import javax.xml.ws.WebServiceContext;

import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * Handles invocations on JSE endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class InvocationHandlerJSE extends AbstractInvocationHandler
{
   protected Object getBeanInstance(Endpoint ep) throws InstantiationException, IllegalAccessException
   {
      Class epImpl = ep.getTargetBeanClass();
      Object targetBean = epImpl.newInstance();
      return targetBean;
   }

   public void invoke(Endpoint ep, Object beanInstance, EndpointInvocation epInv) throws Exception
   {
      try
      {
         if (beanInstance == null)
            beanInstance = getBeanInstance(ep);

         InvocationContext invContext = epInv.getInvocationContext();
         WebServiceContext wsContext = invContext.getAttachment(WebServiceContext.class);
         if (wsContext != null)
         {
            new WebServiceContextInjector().injectContext(beanInstance, (WebServiceContext)wsContext);
         }

         Method method = getImplMethod(beanInstance.getClass(), epInv.getJavaMethod());
         Object retObj = method.invoke(beanInstance, epInv.getArgs());
         epInv.setReturnValue(retObj);
      }
      catch (Exception e)
      {
         handleInvocationException(e);
      }
   }
}
