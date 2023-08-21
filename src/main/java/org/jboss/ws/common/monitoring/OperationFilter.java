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

import javax.xml.namespace.QName;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;

/**
 * This filter matches records having a given operation QName value.
 * 
 * @author alessio.soldano@jboss.com
 * @since 11-Dec-2007
 */
public class OperationFilter implements RecordFilter
{
   private static final long serialVersionUID = -726794729964445956L;
   
   private QName operation;

   public OperationFilter(QName operation)
   {
      this.operation = operation;
   }

   public boolean match(Record record)
   {
      if (record != null)
      {
         if (record.getOperation() == null && operation == null)
         {
            return true;
         }
         else if (operation != null && operation.equals(record.getOperation()))
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return true;
      }
   }

   public QName getOperation()
   {
      return operation;
   }

   @Override
   public Object clone() throws CloneNotSupportedException
   {
      OperationFilter retObj = (OperationFilter)super.clone();
      retObj.operation = this.operation;
      return retObj;
   }
}
