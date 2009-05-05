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
package org.jboss.wsf.common.injection.resolvers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;

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
    * @see org.jboss.wsf.common.injection.resolvers.AbstractReferenceResolver#resolveField(java.lang.reflect.Field)
    */
   @Override
   protected String resolveField(Field field)
   {
      final String fallBackName = field.getName();
      final String resourceName = field.getAnnotation(Resource.class).name();

      return getName(resourceName, fallBackName);
   }

   /* (non-Javadoc)
    * @see org.jboss.wsf.common.injection.resolvers.AbstractReferenceResolver#resolveMethod(java.lang.reflect.Method)
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
