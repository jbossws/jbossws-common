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
import java.util.Collections;
import java.util.LinkedList;

/**
 * All class processors should extend this class.
 *
 * @author ropalka@redhat.com
 */
public abstract class AbstractClassProcessor<A extends AccessibleObject>
extends AccessibleObjectProcessorAdapter<A>
implements ClassProcessor<A>
{

   /**
    * @see org.jboss.wsf.common.reflection.ClassProcessor#process(Class)
    */
   public Collection<A> process(final Class<?> clazz)
   {
      if (clazz == null)
         return Collections.emptyList();
      
      final Collection<A> retVal = new LinkedList<A>();
      
      final A[] accessibleObjects = this.getAccessibleObjects(clazz);
      for(A accessibleObject : accessibleObjects)
      {
         if (this.matches(accessibleObject))
         {
            this.validate(accessibleObject);
            retVal.add(accessibleObject);
         }
      }
      
      retVal.addAll(this.process(clazz.getSuperclass()));
      
      this.validate(retVal);
      
      return retVal;
   }
   
   /**
    * All subclasses have to implement this method.
    * 
    * @param clazz to get accessible objects from.
    * @return array of accessible objects
    */
   public abstract A[] getAccessibleObjects(final Class<?> clazz);

}
