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
package org.jboss.wsf.common.reflection;

import java.lang.reflect.AccessibleObject;
import java.util.Collection;

/**
 * Accessible object processor.
 *
 * @author ropalka@redhat.com
 */
public interface AccessibleObjectProcessor<A extends AccessibleObject>
{
   
   /**
    * Validates accessible object.
    * 
    * @param accessibleObject object to validate
    * @return RuntimeException if validation failed
    */
   void validate(A accessibleObject);
   
   /**
    * Validates collection of accessible objects.
    * 
    * @param accessibleObjects collection of accessible objects to validate
    * @return RuntimeException if validation failed
    */
   void validate(Collection<A> accessibleObjects);
   
   /**
    * Indicates whether particular accessible object matches the criteria.
    * 
    * @param accessibleObject to check
    * @return true if accessible object matches the criteria, false otherwise
    */
   boolean matches(A accessibleObject);
   
}
