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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.annotation.Resource;

/**
 * JNDI reference resolver for @Resource annotated methods and fields.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 * @see org.jboss.wsf.spi.metadata.injection.ReferenceResolver
 */
public final class ResourceReferenceResolver extends AbstractReferenceResolver<Resource>
{

   /**
    * Constructor.
    */
   public ResourceReferenceResolver()
   {
      super(Resource.class);
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.common.injection.resolvers.AbstractReferenceResolver#resolveField(java.lang.reflect.Field)
    */
   @Override
   protected String resolveField(Field field)
   {
      final String fallBackName = field.getName();
      final String resourceName = field.getAnnotation(Resource.class).name();

      return getName(resourceName, fallBackName);
   }

   /* (non-Javadoc)
    * @see org.jboss.ws.common.injection.resolvers.AbstractReferenceResolver#resolveMethod(java.lang.reflect.Method)
    */
   @Override
   protected String resolveMethod(Method method)
   {
      final String fallBackName = convertToBeanName(method.getName()); 
      final String resourceName = method.getAnnotation(Resource.class).name();

      return getName(resourceName, fallBackName);
   }

   /**
    * Returns JNDI resource name.
    *
    * @param resourceName to use if specified
    * @param fallBackName fall back bean name otherwise
    * @return JNDI resource name
    */
   private static String getName(final String resourceName, final String fallBackName)
   {
      return resourceName.length() > 0 ? resourceName : fallBackName;
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

}
