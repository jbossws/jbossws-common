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
package org.jboss.ws.common.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import jakarta.xml.ws.WebServiceContext;

import org.jboss.ws.common.injection.finders.PostConstructMethodFinder;
import org.jboss.ws.common.injection.finders.PreDestroyMethodFinder;
import org.jboss.ws.common.injection.finders.ResourceFieldFinder;
import org.jboss.ws.common.injection.finders.ResourceMethodFinder;
import org.jboss.ws.common.reflection.ClassProcessor;

/**
 * An injection helper class for <b>javax.*</b> annotations.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class InjectionHelper
{
   private static final ClassProcessor<Method> POST_CONSTRUCT_METHOD_FINDER = new PostConstructMethodFinder();
   private static final ClassProcessor<Method> PRE_DESTROY_METHOD_FINDER = new PreDestroyMethodFinder();
   private static final ClassProcessor<Method> WEB_SERVICE_CONTEXT_METHOD_FINDER = new ResourceMethodFinder(WebServiceContext.class, true);
   private static final ClassProcessor<Field> WEB_SERVICE_CONTEXT_FIELD_FINDER = new ResourceFieldFinder(WebServiceContext.class, true);

   /**
    * Forbidden constructor.
    */
   private InjectionHelper()
   {
      super();
   }

   /**
    * Injects @Resource annotated accessible objects referencing WebServiceContext.
    *
    * @param instance to operate on
    * @param ctx current web service context
    */
   public static void injectWebServiceContext(final Object instance, final WebServiceContext ctx)
   {
      final Class<?> instanceClass = instance.getClass();

      // inject @Resource annotated methods accepting WebServiceContext parameter
      Collection<Method> resourceAnnotatedMethods = WEB_SERVICE_CONTEXT_METHOD_FINDER.process(instanceClass);
      for(Method method : resourceAnnotatedMethods)
      {
         try
         {
            invokeMethod(instance, method, new Object[] {ctx});
         }
         catch (Exception e)
         {
            final String message = "Cannot inject @Resource annotated method: " + method;
            InjectionException.rethrow(message, e);
         }
      }

      // inject @Resource annotated fields of WebServiceContext type
      final Collection<Field> resourceAnnotatedFields = WEB_SERVICE_CONTEXT_FIELD_FINDER.process(instanceClass);
      for (Field field : resourceAnnotatedFields)
      {
         try
         {
            setField(instance, field, ctx);
         }
         catch (Exception e)
         {
            final String message = "Cannot inject @Resource annotated field: " + field;
            InjectionException.rethrow(message, e);
         }
      }
   }

   /**
    * Calls @PostConstruct annotated method if exists.
    *
    * @param instance to invoke @PostConstruct annotated method on
    * @see org.jboss.ws.common.injection.finders.PostConstructMethodFinder
    * @see jakarta.annotation.PostConstruct
    */
   public static void callPostConstructMethod(final Object instance)
   {
      if (instance == null)
         throw new IllegalArgumentException();

      final Collection<Method> methods = POST_CONSTRUCT_METHOD_FINDER.process(instance.getClass());

      if (methods.size() > 0)
      {
         final Method method = methods.iterator().next();
         try
         {
            invokeMethod(instance, method, null);
         }
         catch (Exception e)
         {
            final String message = "Calling of @PostConstruct annotated method failed: " + method;
            InjectionException.rethrow(message, e);
         }
      }
   }

   /**
    * Calls @PreDestroy annotated method if exists.
    *
    * @param instance to invoke @PreDestroy annotated method on
    * @see org.jboss.ws.common.injection.finders.PreDestroyMethodFinder
    * @see jakarta.annotation.PreDestroy
    */
   public static void callPreDestroyMethod(final Object instance)
   {
      if (instance == null)
         throw new IllegalArgumentException();

      final Collection<Method> methods = PRE_DESTROY_METHOD_FINDER.process(instance.getClass());

      if (methods.size() > 0)
      {
         final Method method = methods.iterator().next();
         try
         {
            invokeMethod(instance, method, null);
         }
         catch (Exception e)
         {
            final String message = "Calling of @PreDestroy annotated method failed: " + method;
            InjectionException.rethrow(message, e);
         }
      }
   }

   /**
    * Invokes method on object with specified arguments.
    *
    * @param instance to invoke method on
    * @param method method to invoke
    * @param args arguments to pass
    */
   private static void invokeMethod(final Object instance, final Method method, final Object[] args)
   {
      final boolean accessability = method.isAccessible();

      try
      {
         method.setAccessible(true);
         method.invoke(instance, args);
      }
      catch (Exception e)
      {
         InjectionException.rethrow(e);
      }
      finally
      {
         method.setAccessible(accessability);
      }
   }

   /**
    * Sets field on object with specified value.
    *
    * @param instance to set field on
    * @param field to set
    * @param value to be set
    */
   private static void setField(final Object instance, final Field field, final Object value)
   {
      final boolean accessability = field.isAccessible();

      try
      {
         field.setAccessible(true);
         field.set(instance, value);
      }
      catch (Exception e)
      {
         InjectionException.rethrow(e);
      }
      finally
      {
         field.setAccessible(accessability);
      }
   }

}
