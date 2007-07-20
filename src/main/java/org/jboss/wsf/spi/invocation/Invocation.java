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

import java.lang.reflect.Method;

/**
 * A general endpoint invocation.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007
 */
public class Invocation 
{
   private InvocationContext invocationContext;
   private Method javaMethod;
   private Object[] args;
   private Object returnValue;

   public Invocation()
   {
      this.invocationContext = new InvocationContext();
   }

   public InvocationContext getInvocationContext()
   {
      return invocationContext;
   }

   public void setInvocationContext(InvocationContext invocationContext)
   {
      this.invocationContext = invocationContext;
   }

   public Method getJavaMethod()
   {
      return javaMethod;
   }

   public void setJavaMethod(Method javaMethod)
   {
      this.javaMethod = javaMethod;
   }

   public Object[] getArgs()
   {
      return args;
   }

   public void setArgs(Object[] args)
   {
      this.args = args;
   }

   public Object getReturnValue()
   {
      return returnValue;
   }

   public void setReturnValue(Object returnValue)
   {
      this.returnValue = returnValue;
   }
}
