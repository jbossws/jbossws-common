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
package org.jboss.ws;

/** 
 * A RuntimeException that should be thrown when a cause is needed
 * 
 * The retrotranslator-0.9.5 cannot handle
 *  
 *   new IllegalStateException(String, Throwable)
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-Jan-2006
 */
public class WSException extends RuntimeException
{
   public WSException()
   {
   }

   public WSException(String message)
   {
      super(message);
   }

   public WSException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public WSException(Throwable cause)
   {
      super(cause);
   }

   public static void rethrow(String string, Throwable th)
   {
      if (th instanceof WSException)
         throw (WSException)th;

      throw new WSException(string, th);
   }

   public static void rethrow(Throwable th)
   {
      if (th instanceof WSException)
         throw (WSException)th;

      throw new WSException(th);
   }
}
