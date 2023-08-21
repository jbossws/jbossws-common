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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import org.jboss.ws.common.reflection.AnnotatedMethodFinder;

/**
 * Abstract @PostConstruct and @PreDestroy annotations method finder.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
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
