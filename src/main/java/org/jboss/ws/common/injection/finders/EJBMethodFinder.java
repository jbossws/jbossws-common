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

import javax.ejb.EJB;

import org.jboss.ws.common.reflection.AnnotatedMethodFinder;

/**
 * Setter based EJB injection.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class EJBMethodFinder
extends AnnotatedMethodFinder<EJB>
{

   /**
    * Constructor.
    */
   public EJBMethodFinder()
   {
      super(EJB.class);
   }

   @Override
   public void validate(Method method)
   {
      super.validate(method);

      // Ensure all method preconditions
      Class<EJB> annotation = getAnnotation();
      ReflectionUtils.assertVoidReturnType(method, annotation);
      ReflectionUtils.assertOneParameter(method, annotation);
      ReflectionUtils.assertNoPrimitiveParameters(method, annotation);
      ReflectionUtils.assertValidSetterName(method, annotation);
      ReflectionUtils.assertNoCheckedExceptionsAreThrown(method, annotation);
      ReflectionUtils.assertNotStatic(method, annotation);
   }

}
