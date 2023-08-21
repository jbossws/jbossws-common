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
package org.jboss.ws.common.injection.resolvers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jboss.ws.common.Messages;
import org.jboss.ws.common.injection.ReferenceResolver;

/**
 * This class adds support for notion of annotated fields and methods.
 * It also ensures passed methods and fields are non null references
 * plus it implements some common logic that would otherwise be
 * implemented in all subclasses. It is highly recommended that all
 * reference resolvers extend this base class for high code reuse.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public abstract class AbstractReferenceResolver<A extends Annotation>
implements ReferenceResolver
{
   /**
    * Resolved annotation.
    */
   private final Class<A> annotationClass;

   /**
    * Constructor.
    */
   public AbstractReferenceResolver(final Class<A> annotationClass)
   {
      super();

      if (annotationClass == null)
      {
         throw Messages.MESSAGES.annotationClassCannotBeNull();
      }

      this.annotationClass = annotationClass; 
   }

   /* (non-Javadoc)
    * @see org.jboss.wsf.spi.metadata.injection.ReferenceResolver#resolve(java.lang.reflect.Method)
    */
   public final String resolve(final AccessibleObject accessibleObject)
   {
      if (!this.canResolve(accessibleObject))
      {
         throw Messages.MESSAGES.cannotResolve(accessibleObject);
      }

      if (accessibleObject.getClass().equals(Method.class))
      {
         return this.resolveMethod((Method)accessibleObject);
      }
      else
      {
         return this.resolveField((Field)accessibleObject);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.wsf.spi.metadata.injection.ReferenceResolver#canResolve(java.lang.reflect.AccessibleObject)
    */
   public final boolean canResolve(final AccessibleObject accessibleObject)
   {
      this.assertNotNull(accessibleObject);

      final boolean isField = accessibleObject.getClass().equals(Field.class);
      final boolean isMethod = accessibleObject.getClass().equals(Method.class);
      final boolean hasAnnotation = accessibleObject.getAnnotation(this.annotationClass) != null; 

      return (isField || isMethod) && hasAnnotation;
   }

   /**
    * All subclasses have to implement this template method.
    *
    * @param Method method
    * @return JNDI name
    */
   protected abstract String resolveMethod(Method method);

   /**
    * All subclasses have to implement this template method.
    *
    * @param Field field
    * @return JNDI name
    */
   protected abstract String resolveField(Field field);

   /**
    * Asserts passed object is not null
    *
    * @param accessibleObject to validate
    */
   private void assertNotNull(final AccessibleObject accessibleObject)
   {
      if (accessibleObject == null)
      {
         throw Messages.MESSAGES.accessibleObjectClassCannotBeNull();
      }
   }

}
