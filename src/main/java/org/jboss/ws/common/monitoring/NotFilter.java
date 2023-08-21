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
import org.jboss.ws.api.monitoring.RecordFilter;

/**
 * A simple record filter acting like the negation operator
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Dec-2007
 *
 */
public class NotFilter implements RecordFilter
{
   private static final long serialVersionUID = -3254118688017137981L;
   
   private RecordFilter filter;

   public NotFilter(RecordFilter filter)
   {
      this.filter = filter;
   }

   public boolean match(Record record)
   {
      return !filter.match(record);
   }

   public RecordFilter getFilter()
   {
      return filter;
   }

   @Override
   public Object clone() throws CloneNotSupportedException
   {
      NotFilter retObj = (NotFilter)super.clone();
      retObj.filter = (RecordFilter)this.filter.clone();
      return retObj;
   }
}
