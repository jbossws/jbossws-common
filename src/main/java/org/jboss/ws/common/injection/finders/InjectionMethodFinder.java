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
package org.jboss.ws.common.injection.finders;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.injection.InjectionException;
import org.jboss.ws.common.reflection.MethodFinder;
import org.jboss.wsf.spi.metadata.injection.InjectionMetaData;

/**
 * Lookups method that matches descriptor specified injection metadata.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class InjectionMethodFinder
extends MethodFinder
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(InjectionMethodFinder.class);

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
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "INJECTION_METADATA_CANNOT_BE_NULL"));

      this.injectionMD = injectionMD;
   }

   @Override
   public boolean matches(final Method method)
   {
      final String targetName = injectionMD.getTargetName();
      final String methodName = "set" + targetName.substring(0, 1).toUpperCase() + targetName.substring(1);
      if (method.getName().equals(methodName))
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
         throw new InjectionException(BundleUtils.getMessage(bundle, "MORE_THAN_ONE_METHOD_FOUND_MATCHING_THE_CRITERIA",  injectionMD));
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
