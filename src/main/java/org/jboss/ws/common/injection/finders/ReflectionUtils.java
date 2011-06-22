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
package org.jboss.ws.common.injection.finders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.injection.InjectionException;

/**
 * Reflection utility class.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
final class ReflectionUtils
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ReflectionUtils.class);

   /**
    * Constructor.
    */
   private ReflectionUtils()
   {
      super();
   }

   /**
    * Asserts method don't declare primitive parameters.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNoPrimitiveParameters(final Method method, Class<? extends Annotation> annotation)
   {
      for (Class<?> type : method.getParameterTypes())
      {
         if (type.isPrimitive())
         {
            throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_CANNOT_DECLARE_PRIMITIVE_PARAMETERS", new Object[]{ getAnnotationMessage(annotation) , method}));
         }
      }
   }

   /**
    * Asserts method don't declare primitive parameters.
    *
    * @param method to validate
    */
   public static void assertNoPrimitiveParameters(final Method method)
   {
      assertNoPrimitiveParameters(method, null);
   }

   /**
    * Asserts field is not of primitive type.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNotPrimitiveType(final Field field, Class<? extends Annotation> annotation)
   {
      if (field.getType().isPrimitive())
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "FIELD_CANNOT_BE_OF_PRIMITIVE_TYPE", new Object[]{ getAnnotationMessage(annotation) ,  field}));
      }
   }

   /**
    * Asserts field is not of primitive type.
    *
    * @param method to validate
    */
   public static void assertNotPrimitiveType(final Field field)
   {
      assertNotPrimitiveType(field, null);
   }

   /**
    * Asserts method have no parameters.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNoParameters(final Method method, Class<? extends Annotation> annotation) 
   {
      if (method.getParameterTypes().length != 0)
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_HAVE_TO_HAVE_NO_PARAMETERS", new Object[]{ getAnnotationMessage(annotation) ,  method}));
      }
   }

   /**
    * Asserts method have no parameters.
    *
    * @param method to validate
    */
   public static void assertNoParameters(final Method method)
   {
      assertNoParameters(method, null);
   }

   /**
    * Asserts method return void.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertVoidReturnType(final Method method, Class<? extends Annotation> annotation) 
   {
      if ((!method.getReturnType().equals(Void.class)) && (!method.getReturnType().equals(Void.TYPE)))
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_HAVE_TO_RETURN_VOID", new Object[]{ getAnnotationMessage(annotation) ,  method}));
      }
   }

   /**
    * Asserts method return void.
    *
    * @param method to validate
    */
   public static void assertVoidReturnType(final Method method) 
   {
      assertVoidReturnType(method, null);
   }

   /**
    * Asserts field isn't of void type.
    *
    * @param field to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNotVoidType(final Field field, Class<? extends Annotation> annotation)
   {
      if ((field.getClass().equals(Void.class)) && (field.getClass().equals(Void.TYPE)))
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "FIELD_CANNOT_BE_OF_VOID_TYPE", new Object[]{ getAnnotationMessage(annotation) ,  field}));
      }
   }

   /**
    * Asserts field isn't of void type.
    *
    * @param field to validate
    */
   public static void assertNotVoidType(final Field field)
   {
      assertNotVoidType(field, null);
   }

   /**
    * Asserts method don't throw checked exceptions.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNoCheckedExceptionsAreThrown(final Method method, Class<? extends Annotation> annotation) 
   {
      Class<?>[] declaredExceptions = method.getExceptionTypes();
      for (int i = 0; i < declaredExceptions.length; i++)
      {
         Class<?> exception = declaredExceptions[i];
         if (!exception.isAssignableFrom(RuntimeException.class))
         {
            throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_CANNOT_THROW_CHECKED_EXCEPTIONS", new Object[]{ getAnnotationMessage(annotation) ,  method}));
         }
      }
   }

   /**
    * Asserts method don't throw checked exceptions.
    *
    * @param method to validate
    */
   public static void assertNoCheckedExceptionsAreThrown(final Method method) 
   {
      assertNoCheckedExceptionsAreThrown(method, null);
   }

   /**
    * Asserts method is not static.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNotStatic(final Method method, Class<? extends Annotation> annotation) 
   {
      if (Modifier.isStatic(method.getModifiers()))
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_CANNOT_BE_STATIC", new Object[]{ getAnnotationMessage(annotation) ,  method}));
      }
   }

   /**
    * Asserts method is not static.
    *
    * @param method to validate
    */
   public static void assertNotStatic(final Method method) 
   {
      assertNotStatic(method, null);
   }

   /**
    * Asserts field is not static.
    *
    * @param field to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNotStatic(final Field field, Class<? extends Annotation> annotation) 
   {
      if (Modifier.isStatic(field.getModifiers()))
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "FIELD_CANNOT_BE_STATIC", new Object[]{ getAnnotationMessage(annotation) ,  field}));
      }
   }

   /**
    * Asserts field is not static.
    *
    * @param field to validate
    */
   public static void assertNotStatic(final Field field) 
   {
      assertNotStatic(field, null);
   }

   /**
    * Asserts field is not final.
    *
    * @param field to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertNotFinal(final Field field, Class<? extends Annotation> annotation) 
   {
      if (Modifier.isFinal(field.getModifiers()))
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "FIELD_CANNOT_BE_FINAL", new Object[]{ getAnnotationMessage(annotation) ,  field}));
      }
   }

   /**
    * Asserts field is not final.
    *
    * @param field to validate
    */
   public static void assertNotFinal(final Field field) 
   {
      assertNotFinal(field, null);
   }

   /**
    * Asserts method have exactly one parameter.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertOneParameter(final Method method, Class<? extends Annotation> annotation)
   {
      if (method.getParameterTypes().length != 1)
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_HAVE_TO_DECLARE_EXACTLY_ONE_PARAMETER", new Object[]{ getAnnotationMessage(annotation) ,  method}));
      }
   }

   /**
    * Asserts method have exactly one parameter.
    *
    * @param method to validate
    */
   public static void assertOneParameter(final Method method)
   {
      assertOneParameter(method, null);
   }

   /**
    * Asserts valid Java Beans setter method name.
    *
    * @param method to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertValidSetterName(final Method method, Class<? extends Annotation> annotation)
   {
      final String methodName = method.getName();
      final boolean correctMethodNameLength = methodName.length() > 3;
      final boolean isSetterMethodName = methodName.startsWith("set");
      final boolean isUpperCasedPropertyName = correctMethodNameLength ? Character.isUpperCase(methodName.charAt(3)) : false;

      if (!correctMethodNameLength || !isSetterMethodName || !isUpperCasedPropertyName)
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "METHOD_DOESN'T_FOLLOW_JAVA_BEANS_SETTER_METHOD_NAME", new Object[]{ getAnnotationMessage(annotation) ,  method}));
      }
   }

   /**
    * Asserts valid Java Beans setter method name.
    *
    * @param method to validate
    */
   public static void assertValidSetterName(final Method method)
   {
      assertValidSetterName(method, null);
   }

   /**
    * Asserts only one method is annotated with annotation.
    *
    * @param method collection of methods to validate
    * @param annotation annotation to propagate in exception message
    */
   public static void assertOnlyOneMethod(final Collection<Method> methods, Class<? extends Annotation> annotation)
   {
      if (methods.size() > 1)
      {
         throw new InjectionException(BundleUtils.getMessage(bundle, "ONLY_ONE_METHOD_CAN_EXIST",  getAnnotationMessage(annotation) ));
      }
   }

   /**
    * Asserts only one method is annotated with annotation.
    *
    * @param method collection of methods to validate
    */
   public static void assertOnlyOneMethod(final Collection<Method> methods)
   {
      assertOnlyOneMethod(methods, null);
   }

   /**
    * Constructs annotation message. If annotation class is null it returns empty string.
    *
    * @param annotation to construct message for
    * @return annotation message or empty string
    */
   private static String getAnnotationMessage(Class<? extends Annotation> annotation)
   {
      return annotation == null ? "" : "annotated with @" + annotation + " annotation "; 
   }

}
