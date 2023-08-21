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
package org.jboss.ws.common.injection.finders;

import static org.jboss.ws.common.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * Reflection utility class.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
final class ReflectionUtils
{
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
            throw annotation == null ? MESSAGES.methodCannotDeclarePrimitiveParameters(method) : MESSAGES.methodCannotDeclarePrimitiveParameters2(method, annotation);
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
         throw annotation == null ? MESSAGES.fieldCannotBeOfPrimitiveOrVoidType(field) : MESSAGES.fieldCannotBeOfPrimitiveOrVoidType2(field, annotation);
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
         throw annotation == null ? MESSAGES.methodHasToHaveNoParameters(method) : MESSAGES.methodHasToHaveNoParameters2(method, annotation);
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
         throw annotation == null ? MESSAGES.methodHasToReturnVoid(method) : MESSAGES.methodHasToReturnVoid2(method, annotation);
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
         throw annotation == null ? MESSAGES.fieldCannotBeOfPrimitiveOrVoidType(field) : MESSAGES.fieldCannotBeOfPrimitiveOrVoidType2(field, annotation);
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
            throw annotation == null ? MESSAGES.methodCannotThrowCheckedException(method) : MESSAGES.methodCannotThrowCheckedException2(method, annotation);
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
         throw annotation == null ? MESSAGES.methodCannotBeStatic(method) : MESSAGES.methodCannotBeStatic2(method, annotation);
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
         throw annotation == null ? MESSAGES.fieldCannotBeStaticOrFinal(field) : MESSAGES.fieldCannotBeStaticOrFinal2(field, annotation);
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
         throw annotation == null ? MESSAGES.fieldCannotBeStaticOrFinal(field) : MESSAGES.fieldCannotBeStaticOrFinal2(field, annotation);
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
         throw annotation == null ? MESSAGES.methodHasToDeclareExactlyOneParameter(method) : MESSAGES.methodHasToDeclareExactlyOneParameter2(method, annotation);
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
         throw annotation == null ? MESSAGES.methodDoesNotRespectJavaBeanSetterMethodName(method) : MESSAGES.methodDoesNotRespectJavaBeanSetterMethodName2(method, annotation);
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
         throw annotation == null ? MESSAGES.onlyOneMethodCanExist() : MESSAGES.onlyOneMethodCanExist2(annotation);
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

}
