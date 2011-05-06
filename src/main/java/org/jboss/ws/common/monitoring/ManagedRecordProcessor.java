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
