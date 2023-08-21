/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.common.injection;

import org.jboss.ws.common.Loggers;


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
         throw new IllegalArgumentException();
      }

      Loggers.ROOT_LOGGER.error(message == null ? reason.getMessage() : message, reason);
      throw new InjectionException(message, reason);
   }

}
