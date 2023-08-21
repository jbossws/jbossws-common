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

/**
 * The record processor MBean interface
 * 
 * @author alessio.soldano@jboss.com
 * @since 12-Dec-2007
 */
public interface ManagedRecordProcessorMBean
{
   public void processRecord(Record record);

   public void setRecording(boolean value);

   public boolean isRecording();

   public List<RecordFilter> getFilters();

   public void addFilter(RecordFilter filter);

   public void setFilters(List<RecordFilter> filters);

   public boolean isProcessSourceHost();

   public void setProcessSourceHost(boolean value);

   public boolean isProcessDestinationHost();

   public void setProcessDestinationHost(boolean value);

   public boolean isProcessMessageType();

   public void setProcessMessageType(boolean value);

   public boolean isProcessEnvelope();

   public void setProcessEnvelope(boolean value);

   public boolean isProcessHeaders();

   public void setProcessHeaders(boolean value);

   public boolean isProcessOperation();

   public void setProcessOperation(boolean value);

   public boolean isProcessDate();

   public void setProcessDate(boolean value);
}
