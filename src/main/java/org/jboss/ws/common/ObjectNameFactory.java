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
package org.jboss.ws.common;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A simple factory for creating safe object names.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-May-2006
 */
public class ObjectNameFactory
{
   public static ObjectName create(String name)
   {
      try
      {
         return new ObjectName(name);
      }
      catch (MalformedObjectNameException e)
      {
         throw Messages.MESSAGES.invalidObjectName(e, name);
      }
   }

   public static ObjectName create(String domain, String key, String value)
   {
      try
      {
         return new ObjectName(domain, key, value);
      }
      catch (MalformedObjectNameException e)
      {
         throw Messages.MESSAGES.invalidObjectName(e, domain + "," + key + "," + value);
      }
   }

   public static ObjectName create(String domain, Hashtable<String, String> table)
   {
      try
      {
         return new ObjectName(domain, table);
      }
      catch (MalformedObjectNameException e)
      {
         throw Messages.MESSAGES.invalidObjectName(e, domain + "," + table);
      }
   }
}
