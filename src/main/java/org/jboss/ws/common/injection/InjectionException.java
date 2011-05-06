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
package org.jboss.ws.common.injection;

import org.jboss.logging.Logger;

/**
 * Represents generic injection error.
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public class InjectionException extends RuntimeException
{

   /**
    * Serial version UID.
    */
   private static final long serialVersionUID = 1L;
   /**
    * Logger.
    */
   private static final Logger LOG = Logger.getLogger(InjectionException.class);

   /**
    * Constructor.
    */
   public InjectionException()
   {
      super();
   }

   /**
    * Constructor.
    *
    * @param message
    */
   public InjectionException(String message)
   {
      super(message);
   }

   /**
    * Constructor.
    *
    * @param cause
    */
   public InjectionException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Constructor.
    *
    * @param message
    * @param cause
    */
   public InjectionException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Rethrows Injection exception that will wrap passed reason.
    * 
    * @param reason to wrap.
    */
   public static void rethrow(final Exception reason)
   {
      rethrow(null, reason);
   }

   /**
    * Rethrows Injection exception that will wrap passed reason.
    *
    * @param message custom message
    * @param reason to wrap.
    */
   public static void rethrow(final String message, final Exception reason)
   {
      if (reason == null)
      {
         throw new IllegalArgumentException("Reason expected");
      }

      LOG.error(message == null ? reason.getMessage() : message, reason);
      throw new InjectionException(message, reason);
   }

}
