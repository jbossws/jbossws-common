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

import java.util.List;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.api.monitoring.RecordProcessor;

/**
 * The record processor MBean representation
 *
 * @author alessio.soldano@jboss.org
 * @since 12-Dec-2007
 */
public class ManagedRecordProcessor implements ManagedRecordProcessorMBean
{
   private RecordProcessor processor;
   
   public ManagedRecordProcessor(RecordProcessor processor)
   {
      this.processor = processor;
   }
   
   public void addFilter(RecordFilter filter)
   {
      processor.addFilter(filter);
   }

   public List<RecordFilter> getFilters()
   {
      return processor.getFilters();
   }

   public boolean isProcessDate()
   {
      return processor.isProcessDate();
   }

   public boolean isProcessDestinationHost()
   {
      return processor.isProcessDestinationHost();
   }

   public boolean isProcessHeaders()
   {
      return processor.isProcessHeaders();
   }

   public boolean isProcessEnvelope()
   {
      return processor.isProcessEnvelope();
   }

   public boolean isProcessMessageType()
   {
      return processor.isProcessMessageType();
   }

   public boolean isProcessOperation()
   {
      return processor.isProcessOperation();
   }

   public boolean isProcessSourceHost()
   {
      return processor.isProcessSourceHost();
   }

   public void processRecord(Record record)
   {
      processor.processRecord(record);
   }

   public void setFilters(List<RecordFilter> filters)
   {
      processor.setFilters(filters);
   }

   public void setProcessDate(boolean value)
   {
      processor.setProcessDate(value);
   }

   public void setProcessDestinationHost(boolean value)
   {
      processor.setProcessDestinationHost(value);
   }

   public void setProcessHeaders(boolean value)
   {
      processor.setProcessHeaders(value);
   }

   public void setProcessEnvelope(boolean value)
   {
      processor.setProcessEnvelope(value);
   }

   public void setProcessMessageType(boolean value)
   {
      processor.setProcessMessageType(value);
   }

   public void setProcessOperation(boolean value)
   {
      processor.setProcessOperation(value);
   }

   public void setProcessSourceHost(boolean value)
   {
      processor.setProcessSourceHost(value);
   }

   public boolean isRecording()
   {
      return processor.isRecording();
   }

   public void setRecording(boolean value)
   {
      processor.setRecording(value);
   }
}
