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
package org.jboss.wsf.common.javax.finders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import org.jboss.wsf.common.reflection.AnnotatedMethodFinder;

/**
 * Abstract @PostConstruct and @PreDestroy annotations method finder. 
 *
 * @author ropalka@redhat.com
 */
abstract class AbstractPostConstructPreDestroyAnnotatedMethodFinder<A extends Annotation>
extends AnnotatedMethodFinder<A>
{

   /**
    * Constructor.
    * 
    * @param annotationClass annotation.
    */
   AbstractPostConstructPreDestroyAnnotatedMethodFinder(final Class<A> annotationClass)
   {
      super(annotationClass);
   }

   @Override
   public void validate(final Collection<Method> methods)
   {
      super.validate(methods);

      // Ensure all methods preconditions
      ReflectionUtils.assertOnlyOneMethod(methods, getAnnotation());
   }

   @Override
   public void validate(final Method method)
   {
      super.validate(method);

      // Ensure all method preconditions
      Class<A> annotation = getAnnotation();
      ReflectionUtils.assertNoParameters(method, annotation);
      ReflectionUtils.assertVoidReturnType(method, annotation);
      ReflectionUtils.assertNoCheckedExceptionsAreThrown(method, annotation);
      ReflectionUtils.assertNotStatic(method, annotation);
   }
   
}
