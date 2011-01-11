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

import org.jboss.wsf.spi.management.recording.Record;
import org.jboss.wsf.spi.management.recording.RecordFilter;

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
