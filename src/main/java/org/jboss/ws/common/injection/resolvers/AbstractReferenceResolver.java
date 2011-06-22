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
package org.jboss.ws.common.injection.resolvers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.wsf.spi.metadata.injection.ReferenceResolver;

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
   private static final ResourceBundle bundle = BundleUtils.getBundle(AbstractReferenceResolver.class);

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
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "ANNOTATION_CLASS_CANNOT_BE_NULL"));
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
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_RESOLVE",  accessibleObject));
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
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "ACCESSIBLEOBJECT_CANNOT_BE_NULL"));
      }
   }

}
