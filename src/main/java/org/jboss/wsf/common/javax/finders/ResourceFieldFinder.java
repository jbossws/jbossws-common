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

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.jboss.wsf.common.reflection.AnnotatedFieldFinder;

/**
 * Field based injection.
 *
 * To access a resource a developer declares a setter method and annotates it as being a
 * resource reference. The name and type of resource maybe inferred by inspecting the
 * method declaration if necessary. The name of the resource, if not declared, is the
 * name of the JavaBeans property as determined starting from the name of the setter
 * method in question. The setter method must follow the standard JavaBeans
 * convention - name starts with a “set”, void return type and only one parameter.
 * Additionally, the type of the parameter must be compatible with the type specified
 * as a property of the Resource if present.
 *
 * @author ropalka@redhat.com
 */
public final class ResourceFieldFinder
extends AnnotatedFieldFinder<Resource>
{
   
   /**
    * Constructor.
    */
   public ResourceFieldFinder()
   {
      super(Resource.class);
   }

   @Override
   public void validate(Field field)
   {
      super.validate(field);

      // Ensure all method preconditions
      Class<Resource> annotation = getAnnotation();
      ReflectionUtils.assertNotVoidType(field, annotation);
      ReflectionUtils.assertNotStatic(field, annotation);
      ReflectionUtils.assertNotPrimitiveType(field, annotation);
   }

}
