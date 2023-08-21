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
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ws.common.Loggers;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;

/**
 * Handles invocations on JSE endpoints.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 */
public abstract class AbstractInvocationHandlerJSE extends AbstractInvocationHandler
{
   private static final String POJO_JNDI_PREFIX = "java:comp/env/";

   private volatile boolean initialized;

   /**
    * Constructor.
    */
   protected AbstractInvocationHandlerJSE()
   {
      super();
   }

   private void init(final Endpoint endpoint, final Invocation invocation)
   throws Exception
   {
      if (!initialized)
      {
         synchronized(this)
         {
            if (!initialized)
            {
               onEndpointInstantiated(endpoint, invocation);
               initialized = true;
            }
         }
      }
   }

   /**
    * Invokes method on endpoint implementation.
    *
    * This method does the following steps:
    *
    * <ul>
    *   <li>lookups endpoint implementation method to be invoked,</li>
    *   <li>
    *     notifies all subclasses about endpoint method is going to be invoked<br/>
    *     (using {@link #onBeforeInvocation(Invocation)} template method),  
    *   </li>
    *   <li>endpoint implementation method is invoked,</li>
    *   <li>
    *     notifies all subclasses about endpoint method invocation was completed<br/>
    *     (using {@link #onAfterInvocation(Invocation)} template method).  
    *   </li>
    * </ul>
    *
    * @param endpoint which method is going to be invoked
    * @param invocation current invocation
    * @throws Exception if any error occurs
    */
   public final void invoke(final Endpoint endpoint, final Invocation invocation) throws Exception
   {
      try
      {
         // prepare for invocation
         this.init(endpoint, invocation);
         final Object targetBean = invocation.getInvocationContext().getTargetBean();
         final Class<?> implClass = targetBean.getClass();
         final Method seiMethod = invocation.getJavaMethod();
         final Method implMethod = this.getImplMethod(implClass, seiMethod);
         final Object[] args = invocation.getArgs();

         // notify subclasses
         this.onBeforeInvocation(invocation);

         // invoke implementation method
         final Object retObj = implMethod.invoke(targetBean, args);

         // set invocation result
         invocation.setReturnValue(retObj);
      }
      catch (Exception e)
      {
         Loggers.ROOT_LOGGER.methodInvocationFailed(e);
         // propagate exception
         this.handleInvocationException(e);
      }
      finally
      {
         // notify subclasses
         this.onAfterInvocation(invocation);
      }
   }

   @Override
   public Context getJNDIContext(final Endpoint ep) throws NamingException
   {
      return (Context) new InitialContext().lookup(POJO_JNDI_PREFIX);
   }

}
