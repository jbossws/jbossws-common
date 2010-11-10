/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.wsf.common.invocation;

import java.lang.reflect.Method;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;

/**
 * Handles invocations on JSE endpoints.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 */
public abstract class AbstractInvocationHandlerJSE extends AbstractInvocationHandler
{

   /**
    * Constructor.
    */
   protected AbstractInvocationHandlerJSE()
   {
      super();
   }

   /**
    * Retrieves endpoint implementation bean that will be used in invocation process.
    *
    * This method does the following steps:
    *
    * <ul>
    *   <li>tries to retrieve endpoint instance from invocation context,</li>
    *   <li>if endpoint instance is not found it's created and instantiated (lazy initialization)</li>
    *   <li>
    *     if endpoint instance was created all subclasses will be notified about this event
    *     (using {@link #onEndpointInstantiated(Endpoint, Invocation)} template method).  
    *   </li>
    * </ul>
    *
    * @param endpoint to lookup implementation instance for
    * @param invocation current invocation
    * @return endpoint implementation
    * @throws Exception if any error occurs
    */
   protected final Object getTargetBean(final Endpoint endpoint, final Invocation invocation) throws Exception
   {
      final InvocationContext invocationContext = invocation.getInvocationContext();
      Object targetBean = invocationContext.getTargetBean();

      if (targetBean == null)
      {
         try
         {
            // create endpoint instance
            final Class<?> endpointImplClass = endpoint.getTargetBeanClass();
            targetBean = endpointImplClass.newInstance();
            invocationContext.setTargetBean(targetBean);

            // notify subclasses
            this.onEndpointInstantiated(endpoint, invocation);
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Cannot create endpoint instance: ", ex);
         }
      }

      return targetBean;
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
         final Object targetBean = this.getTargetBean(endpoint, invocation);
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
         this.log.error("Method invocation failed with exception: " + e.getMessage(), e);
         // propagate exception
         this.handleInvocationException(e);
      }
      finally
      {
         // notify subclasses
         this.onAfterInvocation(invocation);
      }
   }

   /**
    * Template method for notifying subclasses that endpoint instance have been instantiated.
    *
    * @param endpoint instantiated endpoint
    * @param invocation current invocation
    * @throws Exception subclasses have to throw exception on any failure
    */
   protected void onEndpointInstantiated(final Endpoint endpoint, final Invocation invocation) throws Exception
   {
      // does nothing
   }

   /**
    * Template method for notifying subclasses that endpoint method is going to be invoked.
    *
    * @param invocation current invocation
    * @throws Exception subclasses have to throw exception on any failure
    */
   protected void onBeforeInvocation(final Invocation invocation) throws Exception
   {
      // does nothing
   }

   /**
    * Template method for notifying subclasses that endpoint method invocation was completed.
    *
    * @param invocation current invocation
    * @throws Exception subclasses have to throw exception on any failure
    */
   protected void onAfterInvocation(final Invocation invocation) throws Exception
   {
      // does nothing
   }

}
