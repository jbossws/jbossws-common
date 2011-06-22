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
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.injection.finders.EJBFieldFinder;
import org.jboss.ws.common.injection.finders.EJBMethodFinder;
import org.jboss.ws.common.injection.finders.InjectionFieldFinder;
import org.jboss.ws.common.injection.finders.InjectionMethodFinder;
import org.jboss.ws.common.injection.finders.PostConstructMethodFinder;
import org.jboss.ws.common.injection.finders.PreDestroyMethodFinder;
import org.jboss.ws.common.injection.finders.ResourceFieldFinder;
import org.jboss.ws.common.injection.finders.ResourceMethodFinder;
import org.jboss.ws.common.reflection.ClassProcessor;
import org.jboss.wsf.spi.metadata.injection.InjectionMetaData;
import org.jboss.wsf.spi.metadata.injection.InjectionsMetaData;

/**
 * An injection helper class for <b>javax.*</b> annotations.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class InjectionHelper
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(InjectionHelper.class);

   private static final Logger LOG = Logger.getLogger(InjectionHelper.class);

   private static final ClassProcessor<Method> POST_CONSTRUCT_METHOD_FINDER = new PostConstructMethodFinder();
   private static final ClassProcessor<Method> PRE_DESTROY_METHOD_FINDER = new PreDestroyMethodFinder();
   private static final ClassProcessor<Method> RESOURCE_METHOD_FINDER = new ResourceMethodFinder(WebServiceContext.class, false);
   private static final ClassProcessor<Field> RESOURCE_FIELD_FINDER = new ResourceFieldFinder(WebServiceContext.class, false);
   private static final ClassProcessor<Method> EJB_METHOD_FINDER = new EJBMethodFinder();
   private static final ClassProcessor<Field> EJB_FIELD_FINDER = new EJBFieldFinder();
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
    * The resource annotations mark resources that are needed by the application. These annotations may be applied
    * to an application component class, or to fields or methods of the component class. When the annotation is
    * applied to a field or method, the container will inject an instance of the requested resource into the
    * application component when the component is initialized. If the annotation is applied to the component class,
    * the annotation declares a resource that the application will look up at runtime.
    *
    * This method handles the following injection types:
    * <ul>
    *   <li>Descriptor specified injections</li>
    *   <li>@Resource annotated methods and fields</li>
    *   <li>@EJB annotated methods and fields</li>
    * </ul>
    *
    * @param instance to inject resources on
    * @param injections injection metadata
    * @see org.jboss.wsf.spi.metadata.injection.InjectionsMetaData
    * @see javax.annotation.Resource
    * @see javax.ejb.EJB
    */
   public static void injectResources(final Object instance, final InjectionsMetaData injections, final Context ctx) 
   {
      if (instance == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "OBJECT_INSTANCE_CANNOT_BE_NULL"));

      if (injections == null)
         return;

      if (ctx == null)
         return;

      // inject descriptor driven annotations
      injectDescriptorAnnotatedAccessibleObjects(instance, injections, ctx);

      // inject @Resource annotated methods and fields
      injectResourceAnnotatedAccessibleObjects(instance, injections, ctx);

      // inject @EJB annotated methods and fields
      injectEJBAnnotatedAccessibleObjects(instance, injections);
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
    * @see javax.annotation.PostConstruct
    */
   public static void callPostConstructMethod(final Object instance)
   {
      if (instance == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "OBJECT_INSTANCE_CANNOT_BE_NULL"));

      final Collection<Method> methods = POST_CONSTRUCT_METHOD_FINDER.process(instance.getClass());

      if (methods.size() > 0)
      {
         final Method method = methods.iterator().next();
         if (LOG.isTraceEnabled())
            LOG.trace("Calling @PostConstruct annotated method: " + method);
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
    * @see javax.annotation.PreDestroy
    */
   public static void callPreDestroyMethod(final Object instance)
   {
      if (instance == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "OBJECT_INSTANCE_CANNOT_BE_NULL"));

      final Collection<Method> methods = PRE_DESTROY_METHOD_FINDER.process(instance.getClass());

      if (methods.size() > 0)
      {
         final Method method = methods.iterator().next();
         if (LOG.isTraceEnabled())
            LOG.trace("Calling @PreDestroy annotated method: " + method);
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
    * Performs descriptor driven injections.
    *
    * @param instance to operate on
    * @param injections injections metadata
    * @param ctx JNDI context
    */
   private static void injectDescriptorAnnotatedAccessibleObjects(final Object instance, final InjectionsMetaData injections, final Context ctx)
   {
      final Collection<InjectionMetaData> injectionMDs = injections.getInjectionsMetaData(instance.getClass());

      for (InjectionMetaData injectionMD : injectionMDs)
      {

         final Method method = getMethod(injectionMD, instance.getClass());
         if (method != null)
         {
            try
            {
               inject(instance, method, injectionMD.getEnvEntryName(), ctx);
            }
            catch (Exception e)
            {
               final String message = "Cannot inject method (descriptor driven injection): " + injectionMD;
               InjectionException.rethrow(message, e);
            }
         }
         else
         {
            final Field field = getField(injectionMD, instance.getClass());
            if (field != null)
            {
               try
               {
                  inject(instance, field, injectionMD.getEnvEntryName(), ctx);
               }
               catch (Exception e)
               {
                  final String message = "Cannot inject field (descriptor driven injection): " + injectionMD;
                  InjectionException.rethrow(message, e);
               }
            }
            else
            {
               final String message = "Cannot find injection target for: " + injectionMD;
               throw new InjectionException(message);
            }
         }
      }
   }

   /**
    * Injects @Resource annotated accessible objects.
    *
    * @param instance to operate on
    * @param injections injections meta data
    * @param ctx JNDI context
    * @see org.jboss.ws.common.injection.finders.ResourceFieldFinder
    * @see org.jboss.ws.common.injection.finders.ResourceMethodFinder
    * @see javax.annotation.Resource
    */
   private static void injectResourceAnnotatedAccessibleObjects(final Object instance, final InjectionsMetaData injections, final Context ctx)
   {
      final Collection<Field> resourceAnnotatedFields = RESOURCE_FIELD_FINDER.process(instance.getClass());
      final Collection<Method> resourceAnnotatedMethods = RESOURCE_METHOD_FINDER.process(instance.getClass());

      // Inject @Resource annotated fields
      for (Field field : resourceAnnotatedFields)
      {
         try
         {
            final String jndiName = injections.getResolver(Resource.class).resolve(field);
            inject(instance, field, jndiName, ctx);
         }
         catch (Exception e)
         {
            final String message = "Cannot inject field annotated with @Resource annotation: " + field;
            InjectionException.rethrow(message, e);
         }
      }

      // Inject @Resource annotated methods
      for(Method method : resourceAnnotatedMethods)
      {
         try
         {
            final String jndiName = injections.getResolver(Resource.class).resolve(method);
            inject(instance, method, jndiName, ctx);
         }
         catch (Exception e)
         {
            final String message = "Cannot inject method annotated with @Resource annotation: " + method;
            InjectionException.rethrow(message, e);
         }
      }
   }

   /**
    * Injects @EJB annotated accessible objects.
    *
    * @param instance to operate on
    * @param injections injections meta data
    * @param ctx JNDI context
    * @see org.jboss.ws.common.injection.finders.EJBFieldFinder
    * @see org.jboss.ws.common.injection.finders.EJBMethodFinder
    * @see javax.ejb.EJB
    */
   private static void injectEJBAnnotatedAccessibleObjects(final Object instance, final InjectionsMetaData injections)
   {
      final Collection<Field> ejbAnnotatedFields = EJB_FIELD_FINDER.process(instance.getClass());
      final Collection<Method> ejbAnnotatedMethods = EJB_METHOD_FINDER.process(instance.getClass());
      final Context ctx = InjectionHelper.getDefaultContext();

      // Inject @EJB annotated fields
      for (Field field : ejbAnnotatedFields)
      {
         try
         {
            final String jndiName = injections.getResolver(EJB.class).resolve(field);
            inject(instance, field, jndiName, ctx);
         }
         catch (Exception e)
         {
            final String message = "Cannot inject field annotated with @EJB annotation: " + field;
            InjectionException.rethrow(message, e);
         }
      }

      // Inject @EJB annotated methods
      for(Method method : ejbAnnotatedMethods)
      {
         try
         {
            final String jndiName = injections.getResolver(EJB.class).resolve(method);
            inject(instance, method, jndiName, ctx);
         }
         catch (Exception e)
         {
            final String message = "Cannot inject method annotated with @EJB annotation: " + method;
            InjectionException.rethrow(message, e);
         }
      }
   }

   /**
    * Injects method.
    *
    * @param instance to invoke method on
    * @param method to invoke
    * @param resourceName resource name
    * @param cxt JNDI context
    */
   private static void inject(final Object instance, final Method method, final String jndiName, final Context ctx)
   {
      final Object value = lookup(jndiName, ctx);
      if (LOG.isTraceEnabled())
         LOG.trace("Injecting method: " + method);
      invokeMethod(instance, method, new Object[] {value});
   }

   /**
    * Injects field.
    *
    * @param field to set
    * @param instance to modify field on
    * @param resourceName resource name
    * @param cxt JNDI context
    */
   private static void inject(final Object instance, final Field field, final String jndiName, final Context ctx)
   {
      final Object value = lookup(jndiName, ctx);
      if (LOG.isTraceEnabled())
         LOG.trace("Injecting field: " + field);
      setField(instance, field, value);
   }

   /**
    * Lookups object in JNDI namespace.
    *
    * @param jndiName jndi name
    * @param ctx context to use
    * @return Object if found
    */
   private static Object lookup(final String jndiName, final Context ctx)
   {
      Object value = null;
      try
      {
         value = ctx.lookup(jndiName);
      }
      catch (NamingException ne)
      {
         final String message = "Resource '" + jndiName + "' not found";
         InjectionException.rethrow(message, ne);
      }

      return value;
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

   /**
    * Returns method that matches the descriptor injection metadata or null if not found.
    *
    * @param injectionMD descriptor injection metadata
    * @param clazz to process
    * @return method that matches the criteria or null if not found
    * @see org.jboss.ws.common.injection.finders.InjectionMethodFinder
    */
   private static Method getMethod(final InjectionMetaData injectionMD, final Class<?> clazz)
   {
      final Collection<Method> result = new InjectionMethodFinder(injectionMD).process(clazz);

      return result.isEmpty() ? null : result.iterator().next();
   }

   /**
    * Returns field that matches the descriptor injection metadata or null if not found.
    *
    * @param injectionMD descriptor injection metadata
    * @param clazz to process
    * @return field that matches the criteria or null if not found
    * @see org.jboss.ws.common.injection.finders.InjectionFieldFinder
    */
   private static Field getField(final InjectionMetaData injectionMD, final Class<?> clazz)
   {
      final Collection<Field> result = new InjectionFieldFinder(injectionMD).process(clazz);

      return result.isEmpty() ? null : result.iterator().next();
   }

   /**
    * Returns default JNDI context.
    *
    * @return default JNDI context
    */
   private static Context getDefaultContext()
   {
      Context ctx = null;

      try
      {
         ctx = new InitialContext();
      }
      catch (NamingException ne)
      {
         final String message = "Cannot create default JNDI context";
         InjectionException.rethrow(message, ne);
      }

      return ctx;
   }

}
