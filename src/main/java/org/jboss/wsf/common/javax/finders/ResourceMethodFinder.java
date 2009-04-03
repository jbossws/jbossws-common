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

import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.jboss.wsf.common.reflection.AnnotatedMethodFinder;

/**
 * Setter based injection.
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
public final class ResourceMethodFinder
extends AnnotatedMethodFinder<Resource>
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
   public ResourceMethodFinder(final Class<?> accept, boolean include)
   {
      super(Resource.class);

      this.accept = accept;
      this.include = include;
   }

   @Override
   public void validate(Method method)
   {
      super.validate(method);

      // Ensure all method preconditions
      Class<Resource> annotation = getAnnotation();
      ReflectionUtils.assertVoidReturnType(method, annotation);
      ReflectionUtils.assertOneParameter(method, annotation);
      ReflectionUtils.assertNoPrimitiveParameters(method, annotation);
      ReflectionUtils.assertValidSetterName(method, annotation);
      ReflectionUtils.assertNoCheckedExceptionsAreThrown(method, annotation);
      ReflectionUtils.assertNotStatic(method, annotation);
   }

   @Override
   public boolean matches(Method method)
   {
      final boolean matches = super.matches(method);

      if (matches)
      {
         // processing @Resource annotated method
         if (this.accept != null)
         {
            // filtering
            if (method.getParameterTypes().length == 1)
            {
               final Class<?> param = method.getParameterTypes()[0];
               final boolean parameterMatch = this.accept.equals(param);
               // include/exclude filtering
               return this.include ? parameterMatch : !parameterMatch;
            }
         }
      }

      return matches;
   }

}
