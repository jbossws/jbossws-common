/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;

/**
 * A helper class for <b>javax.annotation</b> annotations. 
 * @author richard.opalka@jboss.com
 */
public final class JavaxAnnotationHelper
{
   
   private static Logger log = Logger.getLogger(JavaxAnnotationHelper.class);
   private static final Object[] noArgs = new Object[] {};
   
   /**
    * Constructor
    */
   private JavaxAnnotationHelper()
   {
      // forbidden inheritance
   }
   
   /**
    * @see JavaxAnnotationHelper#callPreDestroyMethod(Object, ClassLoader)
    * @param instance to inject resource on
    * @throws Exception if some error occurs
    */
   public static void injectResources(Object instance) throws Exception
   {
      injectResources(instance, Thread.currentThread().getContextClassLoader());
   }
   
   /**
    * The Resource annotation marks a resource that is needed by the application. This annotation may be applied
    * to an application component class, or to fields or methods of the component class. When the annotation is
    * applied to a field or method, the container will inject an instance of the requested resource into the
    * application component when the component is initialized. If the annotation is applied to the component class,
    * the annotation declares a resource that the application will look up at runtime.
    * @param instance to inject resource on
    * @param classLoader to check whether javax.annotation annotations are available
    * @throws Exception if some error occurs
    */
   public static void injectResources(Object instance, ClassLoader classLoader) throws Exception
   {
      if (instance == null)
         throw new IllegalArgumentException("Object instance cannot be null");
      if (classLoader == null)
         throw new IllegalArgumentException("ClassLoader cannot be null");
      
      try
      {
         classLoader.loadClass("javax.annotation.Resource");
      }
      catch (Throwable th)
      {
         log.debug("Cannot inject resources: " + th.toString());
         return;
      }

      Class<?> instanceClass = instance.getClass();
      
      // handle Resource injection on types
      if (instanceClass.isAnnotationPresent(Resource.class))
         throw new NotImplementedException("@Resource not implemented for: " + instanceClass.getName());
      
      // handle Resource injection on fields
      for (Field field : getAllDeclaredFields(instanceClass))
      {
         if (field.isAnnotationPresent(Resource.class))
            throw new NotImplementedException("@Resource not implemented for: " + instanceClass.getName());
      }
      
      // handle Resource injection on methods
      for (Method method : getAllDeclaredMethods(instanceClass))
      {
         if (method.isAnnotationPresent(Resource.class))
            throw new NotImplementedException("@Resource not implemented for: " + instanceClass.getName());
      }
   }
   
   /**
    * @see JavaxAnnotationHelper#callPreDestroyMethod(Object, ClassLoader)
    * @param instance to invoke pre destroy method on
    * @throws Exception if some error occurs
    */
   public static void callPreDestroyMethod(Object instance) throws Exception
   {
      callPreDestroyMethod(instance, Thread.currentThread().getContextClassLoader());
   }
   
   /**
    * The PreDestroy annotation is used on methods as a callback notification to signal that the instance
    * is in the process of being removed by the container. The method annotated with PreDestroy is typically
    * used to release resources that it has been holding. This annotation MUST be supported by all container
    * managed objects that support PostConstruct except the application client container in Java EE 5.
    * The method on which the PreDestroy annotation is applied MUST fulfill all of the following criteria:
    * <ul>
    *   <li>The method MUST NOT have any parameters.
    *   <li>The return type of the method MUST be void.
    *   <li>The method MUST NOT throw a checked exception.
    *   <li>The method on which PreDestroy is applied MAY be public, protected, package private or private.
    *   <li>The method MUST NOT be static.
    *   <li>The method MAY be final.
    *   <li>If the method throws an unchecked exception it is ignored.
    * </ul>
    * @param instance to invoke pre destroy method on
    * @param classLoader to check whether javax.annotation annotations are available
    * @throws Exception if some error occurs
    */
   public static void callPreDestroyMethod(Object instance, ClassLoader classLoader) throws Exception
   {
      if (instance == null)
         throw new IllegalArgumentException("Object instance cannot be null");
      if (classLoader == null)
         throw new IllegalArgumentException("ClassLoader cannot be null");
      
      try
      {
         classLoader.loadClass("javax.annotation.PreDestroy");
      }
      catch (Throwable th)
      {
         log.debug("Cannot call pre destroy: " + th.toString());
         return;
      }

      Method targetMethod = null;
      for (Method method : getAllDeclaredMethods(instance.getClass()))
      {
         if (method.isAnnotationPresent(PreDestroy.class))
         {
            if (targetMethod == null)
            {
               targetMethod = method;
            }
            else
            {
               throw new RuntimeException("Only one method can be annotated with javax.annotation.PreDestroy annotation");
            }
         }
      }
      
      if (targetMethod != null)
      {
         // Ensure all method preconditions
         assertNoParameters(targetMethod);
         assertVoidReturnType(targetMethod);
         assertNoCheckedExceptionsAreThrown(targetMethod);
         assertNotStatic(targetMethod);

         // Finally call annotated method
         invokeMethod(targetMethod, instance);
      }
   }
   
   /**
    * @see JavaxAnnotationHelper#callPostConstructMethod(Object, ClassLoader)
    * @param instance to invoke post construct method on
    * @throws Exception if some error occurs
    */
   public static void callPostConstructMethod(Object instance) throws Exception
   {
      callPostConstructMethod(instance, Thread.currentThread().getContextClassLoader());
   }
   
   /**
    * The PostConstruct annotation is used on a method that needs to be executed after dependency injection is done
    * to perform any initialization. This method MUST be invoked before the class is put into service. This annotation
    * MUST be supported on all classes that support dependency injection. The method annotated with PostConstruct MUST
    * be invoked even if the class does not request any resources to be injected. Only one method can be annotated with
    * this annotation. The method on which the PostConstruct annotation is applied MUST fulfill all of the following criteria:
    * <ul>
    *   <li>The method MUST NOT have any parameters.
    *   <li>The return type of the method MUST be void.
    *   <li>The method MUST NOT throw a checked exception.
    *   <li>The method on which PostConstruct is applied MAY be public, protected, package private or private.
    *   <li>The method MUST NOT be static.
    *   <li>The method MAY be final.
    *   <li>If the method throws an unchecked exception the class MUST NOT be put into service.
    * </ul> 
    * @param instance to invoke post construct method on
    * @param classLoader to check whether javax.annotation annotations are available
    * @throws Exception if some error occurs
    */
   public static void callPostConstructMethod(Object instance, ClassLoader classLoader) throws Exception
   {
      if (instance == null)
         throw new IllegalArgumentException("Object instance cannot be null");
      if (classLoader == null)
         throw new IllegalArgumentException("ClassLoader cannot be null");
      
      try
      {
         classLoader.loadClass("javax.annotation.PostConstruct");
      }
      catch (Throwable th)
      {
         log.debug("Cannot call post construct: " + th.toString());
         return;
      }

      Method targetMethod = null;
      for (Method method : getAllDeclaredMethods(instance.getClass()))
      {
         if (method.isAnnotationPresent(PostConstruct.class))
         {
            if (targetMethod == null)
            {
               targetMethod = method;
            }
            else
            {
               throw new RuntimeException("Only one method can be annotated with javax.annotation.PostConstruct annotation");
            }
         }
      }
      
      if (targetMethod != null)
      {
         // Ensure all method preconditions
         assertNoParameters(targetMethod);
         assertVoidReturnType(targetMethod);
         assertNoCheckedExceptionsAreThrown(targetMethod);
         assertNotStatic(targetMethod);

         // Finally call annotated method
         invokeMethod(targetMethod, instance);
      }
   }
   
   private static List<Method> getAllDeclaredMethods(Class<?> clazz)
   {
      List<Method> retVal = new LinkedList<Method>();
      while (clazz != null)
      {
         for (Method m : clazz.getDeclaredMethods())
         {
            retVal.add(m);
         }
         clazz = clazz.getSuperclass();
      }
      return retVal;
   }
   
   private static List<Field> getAllDeclaredFields(Class<?> clazz)
   {
      List<Field> retVal = new LinkedList<Field>();
      while (clazz != null)
      {
         for (Field f : clazz.getDeclaredFields())
         {
            retVal.add(f);
         }
         clazz = clazz.getSuperclass();
      }
      return retVal;
   }
   
   private static void invokeMethod(Method m, Object instance) throws Exception
   {
      if (!m.isAccessible())
      {
         m.setAccessible(true);
      }
      m.invoke(instance, noArgs);
   }
   
   private static void assertNoParameters(Method m) 
   {
      if (m.getParameterTypes().length != 0)
         throw new RuntimeException("Method annotated with javax.annotation annotations have to be parameterless");
   }

   private static void assertVoidReturnType(Method m) 
   {
      if ((!m.getReturnType().equals(Void.class)) && (!m.getReturnType().equals(Void.TYPE)))
         throw new RuntimeException("Method annotated with javax.annotation annotations have to return void");
   }

   private static void assertNoCheckedExceptionsAreThrown(Method m) 
   {
      Class<?>[] declaredExceptions = m.getExceptionTypes();
      for (int i = 0; i < declaredExceptions.length; i++)
      {
         Class<?> exception = declaredExceptions[i];
         if (!exception.isAssignableFrom(RuntimeException.class))
            throw new RuntimeException("Method annotated with javax.annotation annotations cannot throw checked exceptions");
      }
   }

   private static void assertNotStatic(Method m) 
   {
      if (Modifier.isStatic(m.getModifiers()))
         throw new RuntimeException("Method annotated with javax.annotation annotations cannot be static");
   }
   
}
