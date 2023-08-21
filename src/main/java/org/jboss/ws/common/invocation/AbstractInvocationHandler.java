/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.common.invocation;

import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * Base class for all Web Service invocation handlers inside AS.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 */
public abstract class AbstractInvocationHandler extends InvocationHandler
{
   /**
    * Constructor.
    */
   protected AbstractInvocationHandler()
   {
      super();
   }

   /**
    * Creates invocation.
    *
    * @return invocation instance
    */
   public final Invocation createInvocation()
   {
      return new Invocation();
   }

   /**
    * Initialization method.
    *
    * @param endpoint endpoint
    */
   public void init(final Endpoint endpoint)
   {
      // does nothing
   }

   public Context getJNDIContext(final Endpoint ep) throws NamingException
   {
      return null;
   }

   /**
    * Returns implementation method that will be used for invocation.
    *
    * @param implClass implementation endpoint class
    * @param seiMethod SEI interface method used for method finding algorithm
    * @return implementation method
    * @throws NoSuchMethodException if implementation method wasn't found
    */
   protected final Method getImplMethod(final Class<?> implClass, final Method seiMethod) throws NoSuchMethodException
   {
      final String methodName = seiMethod.getName();
      final Class<?>[] paramTypes = seiMethod.getParameterTypes();

      return implClass.getMethod(methodName, paramTypes);
   }

   @Override
   public void onEndpointInstantiated(final Endpoint endpoint, final Invocation invocation) throws Exception
   {
      // does nothing
   }

   @Override
   public void onBeforeInvocation(final Invocation invocation) throws Exception
   {
      // does nothing
   }

   @Override
   public void onAfterInvocation(final Invocation invocation) throws Exception
   {
      // does nothing
   }

}
