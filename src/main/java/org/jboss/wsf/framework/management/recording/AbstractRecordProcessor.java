/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.framework.management.recording;

import java.util.List;
import java.util.Vector;

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
   protected List<RecordFilter> filters = new Vector<RecordFilter>();
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
      this.filters = new Vector<RecordFilter>(filters);
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
      retObj.filters = new Vector<RecordFilter>();
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
