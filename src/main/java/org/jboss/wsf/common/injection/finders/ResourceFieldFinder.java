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
package org.jboss.wsf.common.injection.finders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.jboss.wsf.common.reflection.AnnotatedFieldFinder;

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
