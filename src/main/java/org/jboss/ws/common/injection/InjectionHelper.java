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
