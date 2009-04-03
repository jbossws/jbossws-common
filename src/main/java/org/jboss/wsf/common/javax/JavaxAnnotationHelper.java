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
package org.jboss.wsf.common.javax;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.annotation.Resource;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.javax.finders.InjectionFieldFinder;
import org.jboss.wsf.common.javax.finders.InjectionMethodFinder;
import org.jboss.wsf.common.javax.finders.PostConstructMethodFinder;
import org.jboss.wsf.common.javax.finders.PreDestroyMethodFinder;
import org.jboss.wsf.common.javax.finders.ResourceFieldFinder;
import org.jboss.wsf.common.javax.finders.ResourceMethodFinder;
import org.jboss.wsf.common.reflection.ClassProcessor;
import org.jboss.wsf.spi.metadata.injection.InjectionMetaData;
import org.jboss.wsf.spi.metadata.injection.InjectionsMetaData;

/**
 * A helper class for <b>javax.annotation</b> annotations. 
 * 
 * @author ropalka@redhat.com
 */
public final class JavaxAnnotationHelper
{
   
   private static final Logger LOG = Logger.getLogger(JavaxAnnotationHelper.class);
   private static final String JNDI_PREFIX = "java:comp/env/";
   private static final ClassProcessor<Method> POST_CONSTRUCT_METHOD_FINDER = new PostConstructMethodFinder();
   private static final ClassProcessor<Method> PRE_DESTROY_METHOD_FINDER = new PreDestroyMethodFinder();
   private static final ClassProcessor<Method> RESOURCE_METHOD_FINDER = new ResourceMethodFinder();
   private static final ClassProcessor<Field> RESOURCE_FIELD_FINDER = new ResourceFieldFinder();
   
   /**
    * Forbidden constructor.
    */
   private JavaxAnnotationHelper()
   {
      super();
   }
   
   /**
    * The Resource annotation marks a resource that is needed by the application. This annotation may be applied
    * to an application component class, or to fields or methods of the component class. When the annotation is
    * applied to a field or method, the container will inject an instance of the requested resource into the
    * application component when the component is initialized. If the annotation is applied to the component class,
    * the annotation declares a resource that the application will look up at runtime.
    * 
    * @param instance to inject resource on
    * @param injections injections metadata
    * @throws Exception if some error occurs
    */
   public static void injectResources(Object instance, InjectionsMetaData injections) throws Exception
   {
      if (instance == null)
         throw new IllegalArgumentException("Object instance cannot be null");
      
      Class<?> instanceClass = instance.getClass();
      
      InitialContext ctx = new InitialContext();

      // inject descriptor driven annotations
      if (injections != null)
      {
         Collection<InjectionMetaData> injectionMDs = injections.getInjectionsMetaData(instanceClass);
         for (InjectionMetaData injectionMD : injectionMDs)
         {
            Method method = getMethod(injectionMD, instanceClass);
            if (method != null)
            {
               // inject descriptor driven annotated method
               inject(instance, method, injectionMD.getEnvEntryName(), ctx);
            }
            else
            {
               Field field = getField(injectionMD, instanceClass);
               if (field != null)
               {
                  // inject descriptor driven annotated field
                  inject(instance, field, injectionMD.getEnvEntryName(), ctx);
               }
               else
               {
                  throw new RuntimeException("Cannot find injection target for: " + injectionMD);
               }
            }
         }
      }

      // inject @Resource annotated methods
      Collection<Method> resourceAnnotatedMethods = RESOURCE_METHOD_FINDER.process(instanceClass);
      for(Method method : resourceAnnotatedMethods)
      {
         inject(instance, method, method.getAnnotation(Resource.class).name(), ctx);
      }
      
      // inject @Resource annotated fields
      Collection<Field> resourceAnnotatedFields = RESOURCE_FIELD_FINDER.process(instanceClass);
      for (Field field : resourceAnnotatedFields)
      {
         inject(instance, field, field.getAnnotation(Resource.class).name(), ctx);
      }
   }
   
   /**
    * Calls @PostConstruct annotated method if exists.
    * 
    * @param instance to invoke @PostConstruct annotated method on
    * @throws Exception if some error occurs
    * @see org.jboss.wsf.common.javax.finders.PostConstructMethodFinder
    * @see javax.annotation.PostConstruct
    */
   public static void callPostConstructMethod(Object instance) throws Exception
   {
      if (instance == null)
         throw new IllegalArgumentException("Object instance cannot be null");

      Collection<Method> methods = POST_CONSTRUCT_METHOD_FINDER.process(instance.getClass());
      
      if (methods.size() > 0)
      {
         Method method = methods.iterator().next();
         LOG.debug("Calling @PostConstruct annotated method: " + method);
         invokeMethod(instance, method, null);
      }
   }
   
   /**
    * Calls @PreDestroy annotated method if exists.
    * 
    * @param instance to invoke @PreDestroy annotated method on
    * @throws Exception if some error occurs
    * @see org.jboss.wsf.common.javax.finders.PreDestroyMethodFinder
    * @see javax.annotation.PreDestroy
    */
   public static void callPreDestroyMethod(Object instance) throws Exception
   {
      if (instance == null)
         throw new IllegalArgumentException("Object instance cannot be null");

      Collection<Method> methods = PRE_DESTROY_METHOD_FINDER.process(instance.getClass());
      
      if (methods.size() > 0)
      {
         Method method = methods.iterator().next();
         LOG.debug("Calling @PreDestroy annotated method: " + method);
         invokeMethod(instance, method, null);
      }
   }
   
   /**
    * Injects @Resource annotated method.
    * 
    * @param method to invoke
    * @param instance to invoke method on
    * @param resourceName resource name
    * @param cxt JNDI context
    * @throws Exception if any error occurs
    * @see org.jboss.wsf.common.javax.finders.ResourceMethodFinder
    */
   private static void inject(final Object instance, final Method method, String resourceName, InitialContext ctx) throws Exception
   {
      final String beanName = convertToBeanName(method.getName()); 
      final Object value = ctx.lookup(getName(resourceName, beanName));

      LOG.debug("Injecting method: " + method);
      invokeMethod(instance, method, new Object[] {value});
   }
   
   /**
    * Injects @Resource annotated field.
    * 
    * @param field to set
    * @param instance to modify field on
    * @param resourceName resource name
    * @param cxt JNDI context
    * @throws Exception if any error occurs
    * @see org.jboss.wsf.common.javax.finders.ResourceFieldFinder
    */
   private static void inject(final Object instance, final Field field, String resourceName, InitialContext ctx) throws Exception
   {
      final String beanName = field.getName();
      final Object value = ctx.lookup(getName(resourceName, beanName));
      
      LOG.debug("Injecting field: " + field);
      setField(instance, field, value);
   }

   /**
    * Translates "setBeanName" to "beanName" string.
    * 
    * @param methodName to translate
    * @return bean name
    */
   private static String convertToBeanName(final String methodName)
   {
      return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
   }
   
   /**
    * Returns full JNDI name.
    * 
    * @param resourceName to be used if specified
    * @param beanName fallback bean name to be used
    * @return JNDI full name
    */
   private static String getName(final String resourceName, final String beanName)
   {
      return JNDI_PREFIX + (resourceName.length() > 0 ? resourceName : beanName);
   }
   
   /**
    * Invokes method on object with specified arguments.
    * 
    * @param instance to invoke method on
    * @param method method to invoke
    * @param args arguments to pass
    * @throws Exception if any error occurs
    */
   private static void invokeMethod(final Object instance, final Method method, final Object[] args) throws Exception
   {
      boolean accessability = method.isAccessible();
      
      try
      {
         method.setAccessible(true);
         method.invoke(instance, args);
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
         throw e; // propagate
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
    * @throws Exception if any error occurs
    */
   private static void setField(final Object instance, final Field field, final Object value) throws Exception
   {
      boolean accessability = field.isAccessible();
      
      try
      {
         field.setAccessible(true);
         field.set(instance, value);
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
         throw e; // propagate
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
    * @see org.jboss.wsf.common.javax.finders.InjectionMethodFinder
    */
   private static Method getMethod(InjectionMetaData injectionMD, Class<?> clazz)
   {
      Collection<Method> result = new InjectionMethodFinder(injectionMD).process(clazz);
      
      return result.isEmpty() ? null : result.iterator().next();
   }
   
   /**
    * Returns field that matches the descriptor injection metadata or null if not found.
    * 
    * @param injectionMD descriptor injection metadata
    * @param clazz to process
    * @return field that matches the criteria or null if not found
    * @see org.jboss.wsf.common.javax.finders.InjectionFieldFinder
    */
   private static Field getField(InjectionMetaData injectionMD, Class<?> clazz)
   {
      Collection<Field> result = new InjectionFieldFinder(injectionMD).process(clazz);
      
      return result.isEmpty() ? null : result.iterator().next();
   }
   
}
