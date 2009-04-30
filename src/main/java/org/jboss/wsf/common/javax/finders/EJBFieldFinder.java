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

import javax.ejb.EJB;

import org.jboss.wsf.common.reflection.AnnotatedFieldFinder;

/**
 * Field based EJB injection.
 *
 * @author ropalka@redhat.com
 */
public final class EJBFieldFinder
extends AnnotatedFieldFinder<EJB>
{
   
   /**
    * Constructor.
    */
   public EJBFieldFinder()
   {
      super(EJB.class);
   }

   @Override
   public void validate(Field field)
   {
      super.validate(field);

      // Ensure all method preconditions
      Class<EJB> annotation = getAnnotation();
      ReflectionUtils.assertNotVoidType(field, annotation);
      ReflectionUtils.assertNotStatic(field, annotation);
      ReflectionUtils.assertNotFinal(field, annotation);
      ReflectionUtils.assertNotPrimitiveType(field, annotation);
   }

}
