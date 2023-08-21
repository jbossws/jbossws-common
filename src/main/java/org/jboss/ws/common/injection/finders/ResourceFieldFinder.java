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

import java.lang.reflect.Field;

import jakarta.annotation.Resource;

import org.jboss.ws.common.reflection.AnnotatedFieldFinder;

/**
 * Field based resource injection.
 *
 * To access a resource a developer declares a setter method and annotates it as being a
 * resource reference. The name and type of resource maybe inferred by inspecting the
 * method declaration if necessary. The name of the resource, if not declared, is the
 * name of the JavaBeans property as determined starting from the name of the setter
 * method in question. The setter method must follow the standard JavaBeans
 * convention - name starts with a 'set', void return type and only one parameter.
 * Additionally, the type of the parameter must be compatible with the type specified
 * as a property of the Resource if present.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class ResourceFieldFinder
extends AnnotatedFieldFinder<Resource>
{

   /**
    * Parameter type to accept/ignore.
    */
   private final Class<?> accept;
   /**
    * If <b>accept</b> field is not null then:
    * <ul>
    *   <li><b>true</b> means include only methods with <b>accept</b> parameter,
    *   <li><b>false</b> means exclude all methods with <b>accept</b> parameter
    * </ul>
    */
   private final boolean include;

   /**
    * Constructor.
    *
    * @param accept filtering class
    * @param include whether include/exclude filtering class
    */
   public ResourceFieldFinder(final Class<?> accept, boolean include)
   {
      super(Resource.class);

      this.accept = accept;
      this.include = include;
   }

   @Override
   public void validate(Field field)
   {
      super.validate(field);

      // Ensure all method preconditions
      Class<Resource> annotation = getAnnotation();
      ReflectionUtils.assertNotVoidType(field, annotation);
      ReflectionUtils.assertNotStatic(field, annotation);
      ReflectionUtils.assertNotFinal(field, annotation);
      ReflectionUtils.assertNotPrimitiveType(field, annotation);
   }

   @Override
   public boolean matches(Field field)
   {
      final boolean matches = super.matches(field);

      if (matches)
      {
         // processing @Resource annotated method
         if (this.accept != null)
         {
            // filtering
            final Class<?> fieldType = field.getType();
            final boolean parameterMatch = this.accept.equals(fieldType);
            // include/exclude filtering
            return this.include ? parameterMatch : !parameterMatch;
         }
      }

      return matches;
   }

}
