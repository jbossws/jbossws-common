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
package org.jboss.ws.common.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

import org.jboss.ws.common.Messages;

/**
 * All annotation aware class processors should extend this class.
 *
 * @author ropalka@redhat.com
 */
public abstract class AbstractAnnotatedClassProcessor<AO extends AccessibleObject, A extends Annotation>
      extends AbstractClassProcessor<AO> implements AnnotationAware<A>
{
   /**
    * Annotation class.
    */
   private final Class<A> annotationClass;
   
   /**
    * Constructor.
    * 
    * @param annotationClass annotation class
    */
   public AbstractAnnotatedClassProcessor(final Class<A> annotationClass)
   {
      if (annotationClass == null)
         throw Messages.MESSAGES.annotationClassCannotBeNull();
      
      this.annotationClass = annotationClass;
   }
   
   @Override
   public boolean matches(final AO accessibleObject)
   {
      return accessibleObject.isAnnotationPresent(getAnnotation());
   }
   
   /**
    * @see org.jboss.ws.common.reflection.AnnotationAware#getAnnotation()
    */
   public Class<A> getAnnotation()
   {
      return this.annotationClass;
   }

}
