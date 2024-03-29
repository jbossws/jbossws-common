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
package org.jboss.ws.common.monitoring;

import org.jboss.ws.api.monitoring.Record;

/**
 * Simple record factory
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public class RecordFactory
{
   private static long count = 0;
   
   public static String newGroupID()
   {
      long time = System.currentTimeMillis();
      StringBuilder sb = new StringBuilder();
      synchronized (RecordFactory.class)
      {
         count++;
         sb.append(count);
      }
      sb.append("-");
      sb.append(time);
      return sb.toString();
   }
   
   public static Record newRecord(String groupID)
   {
      Record record = new RecordImpl();
      record.setGroupID(groupID);
      return record;
   }
   
   public static Record newRecord()
   {
      return newRecord(newGroupID());
   }
}
