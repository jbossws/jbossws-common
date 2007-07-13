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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import javax.management.MBeanException;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.utils.JavaUtils;

/**
 * Handles invocations on endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public abstract class BasicInvocationHandler implements InvocationHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(BasicInvocationHandler.class);
   
   public Invocation createInvocation()
   {
      return new BasicEndpointInvocation();
   }

   protected Method getImplMethod(Class implClass, Method seiMethod) throws ClassNotFoundException, NoSuchMethodException
   {
      String methodName = seiMethod.getName();
      Class[] paramTypes = seiMethod.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++)
      {
         Class paramType = paramTypes[i];
         if (JavaUtils.isPrimitive(paramType) == false)
         {
            String paramTypeName = paramType.getName();
            paramType = JavaUtils.loadJavaType(paramTypeName);
            paramTypes[i] = paramType;
         }
      }

      Method implMethod = implClass.getMethod(methodName, paramTypes);
      return implMethod;
   }

   public void create(Endpoint ep)
   {
      log.debug("Create: " + ep.getName());
   }

   public void start(Endpoint ep)
   {
      log.debug("Start: " + ep.getName());
   }

   public void stop(Endpoint ep)
   {
      log.debug("Stop: " + ep.getName());
   }

   public void destroy(Endpoint ep)
   {
      log.debug("Destroy: " + ep.getName());
   }
   
   protected void handleInvocationException(Throwable th) throws Exception
   {
      if (th instanceof MBeanException)
      {
         throw ((MBeanException)th).getTargetException();
      }

      if (th instanceof InvocationTargetException)
      {
         // Unwrap the throwable raised by the service endpoint implementation
         Throwable targetEx = ((InvocationTargetException)th).getTargetException();
         handleInvocationException(targetEx);
      }
      
      if (th instanceof Exception)
      {
         throw (Exception)th;
      }
      
      if (th instanceof Error)
      {
         throw (Error)th;
      }
      
      throw new UndeclaredThrowableException(th);
   }
}
