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
import java.util.Collection;

import org.jboss.wsf.common.reflection.MethodFinder;
import org.jboss.wsf.spi.metadata.injection.InjectionMetaData;

/**
 * Lookups method that matches descriptor specified injection metadata.
 *
 * @author ropalka@redhat.com
 */
public final class InjectionMethodFinder
extends MethodFinder
{
   
   /**
    * Descriptor injection metadata.
    */
   private final InjectionMetaData injectionMD;
   
   /**
    * Constructor.
    * 
    * @param injectionMD descriptor injection metadata
    */
   public InjectionMethodFinder(final InjectionMetaData injectionMD)
   {
      if (injectionMD == null)
         throw new IllegalArgumentException("Injection metadata cannot be null");
      
      this.injectionMD = injectionMD;
   }

   @Override
   public boolean matches(final Method method)
   {
      if (method.getName().equals(injectionMD.getTargetName()))
      {
         if (injectionMD.getValueClass() != null)
         {
            if (method.getParameterTypes().length == 1)
            {
               final Class<?> expectedClass = injectionMD.getValueClass();
               final Class<?> parameterClass = method.getParameterTypes()[0];

               return expectedClass.equals(parameterClass);
            }
         }
         else
         {
            if (method.getParameterTypes().length == 1)
            {
               return true;
            }
         }
      }
      
      return false;
   }

   @Override
   public void validate(final Collection<Method> methods)
   {
      super.validate(methods);
      
      if (methods.size() > 2)
      {
         throw new RuntimeException("More than one method found matching the criteria: " + injectionMD);
      }
   }

   @Override
   public void validate(final Method method)
   {
      super.validate(method);
      
      ReflectionUtils.assertVoidReturnType(method);
      ReflectionUtils.assertOneParameter(method);
      ReflectionUtils.assertNoPrimitiveParameters(method);
      ReflectionUtils.assertValidSetterName(method);
      ReflectionUtils.assertNoCheckedExceptionsAreThrown(method);
      ReflectionUtils.assertNotStatic(method);
   }
   
}
