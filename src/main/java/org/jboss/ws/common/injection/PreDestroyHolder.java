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