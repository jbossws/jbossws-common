/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.common.injection;

/**
 * Utility class for pre destroy registration.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class PreDestroyHolder
{

   private final Object object;
   private final int hashCode;

   public PreDestroyHolder(Object object)
   {
      super();
      this.hashCode = System.identityHashCode(object);
      this.object = object;
   }

   public final Object getObject()
   {
      return this.object;
   }

   public final boolean equals(Object o)
   {
      if (o instanceof PreDestroyHolder)
      {
         return ((PreDestroyHolder)o).hashCode == this.hashCode;
      }

      return false;
   }

   public final int hashCode()
   {
      return this.hashCode;
   }

}