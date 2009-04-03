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
import java.util.Collection;

import org.jboss.wsf.common.reflection.FieldFinder;
import org.jboss.wsf.spi.metadata.injection.InjectionMetaData;

/**
 * Lookups field that matches descriptor specified injection metadata.
 *
 * @author ropalka@redhat.com
 */
public final class InjectionFieldFinder
extends FieldFinder
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
   public InjectionFieldFinder(final InjectionMetaData injectionMD)
   {
      if (injectionMD == null)
         throw new IllegalArgumentException("Injection metadata cannot be null");

      this.injectionMD = injectionMD;
   }

   @Override
   public boolean matches(final Field field)
   {
      if (field.getName().equals(injectionMD.getTargetName()))
      {
         if (injectionMD.getValueClass() != null)
         {
            final Class<?> expectedClass = injectionMD.getValueClass();
            final Class<?> fieldClass = field.getType();

            return expectedClass.equals(fieldClass);
         }
         else
         {
            return true;
         }
      }

      return false;
   }

   @Override
   public void validate(final Collection<Field> fields)
   {
      super.validate(fields);

      if (fields.size() > 2)
      {
         throw new RuntimeException("More than one field found matching the criteria: " + injectionMD);
      }
   }

   @Override
   public void validate(final Field field)
   {
      super.validate(field);

      ReflectionUtils.assertNotVoidType(field);
      ReflectionUtils.assertNotStatic(field);
      ReflectionUtils.assertNotPrimitiveType(field);
   }

}
