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
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.api.monitoring.RecordProcessor;

/**
 * An abstract record processor providing basic implementation
 * of the processor configuration and filter's management
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public abstract class AbstractRecordProcessor implements RecordProcessor
{
   private static final long serialVersionUID = -1825185742740851152L;
   
   protected List<RecordFilter> filters = new CopyOnWriteArrayList<RecordFilter>();
   protected boolean processDestinationHost = true;
   protected boolean processSourceHost = true;
   protected boolean processHeaders = true;
   protected boolean processEnvelope = true;
   protected boolean processMessageType = true;
   protected boolean processOperation = true;
   protected boolean processDate = true;
   protected String name;
   protected boolean recording = false;

   public abstract void processRecord(Record record);

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void addFilter(RecordFilter filter)
   {
      filters.add(filter);
   }

   public List<RecordFilter> getFilters()
   {
      return filters;
   }

   public void setFilters(List<RecordFilter> filters)
   {
      this.filters = new CopyOnWriteArrayList<RecordFilter>(filters);
   }

   public boolean isProcessDestinationHost()
   {
      return processDestinationHost;
   }

   public void setProcessDestinationHost(boolean processDestinationHost)
   {
      this.processDestinationHost = processDestinationHost;
   }

   public boolean isProcessSourceHost()
   {
      return processSourceHost;
   }

   public void setProcessSourceHost(boolean processSourceHost)
   {
      this.processSourceHost = processSourceHost;
   }

   public boolean isProcessHeaders()
   {
      return processHeaders;
   }

   public void setProcessHeaders(boolean processHeaders)
   {
      this.processHeaders = processHeaders;
   }

   public boolean isProcessEnvelope()
   {
      return processEnvelope;
   }

   public void setProcessEnvelope(boolean processEnvelope)
   {
      this.processEnvelope = processEnvelope;
   }

   public boolean isProcessMessageType()
   {
      return processMessageType;
   }

   public void setProcessMessageType(boolean processMessageType)
   {
      this.processMessageType = processMessageType;
   }

   public boolean isProcessOperation()
   {
      return processOperation;
   }

   public void setProcessOperation(boolean processOperation)
   {
      this.processOperation = processOperation;
   }

   public boolean isProcessDate()
   {
      return processDate;
   }

   public void setProcessDate(boolean processDate)
   {
      this.processDate = processDate;
   }

   public boolean isRecording()
   {
      return recording;
   }

   public void setRecording(boolean recording)
   {
      this.recording = recording;
   }

   @Override
   public Object clone() throws CloneNotSupportedException
   {
      AbstractRecordProcessor retObj = (AbstractRecordProcessor)super.clone();
      retObj.filters = new CopyOnWriteArrayList<RecordFilter>();
      for (RecordFilter fil : this.filters)
      {
         RecordFilter clFil = (RecordFilter)fil.clone();
         retObj.filters.add(clFil);
      }
      retObj.processDestinationHost = this.processDestinationHost;
      retObj.processSourceHost = this.processSourceHost;
      retObj.processHeaders = this.processHeaders;
      retObj.processEnvelope = this.processEnvelope;
      retObj.processMessageType = this.processMessageType;
      retObj.processOperation = this.processOperation;
      retObj.processDate = this.processDate;
      retObj.recording = this.recording;
      retObj.name = this.name;
      return retObj;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append(" (recording = " + recording);
      sb.append(", processDestinationHost = " + processDestinationHost);
      sb.append(", processSourceHost = " + processSourceHost);
      sb.append(", processHeaders = " + processHeaders);
      sb.append(", processEnvelope = " + processEnvelope);
      sb.append(", processMessageType = " + processMessageType);
      sb.append(", processOperation = " + processOperation);
      sb.append(", processDate = " + processDate);
      sb.append(")");
      return sb.toString();
   }
}
