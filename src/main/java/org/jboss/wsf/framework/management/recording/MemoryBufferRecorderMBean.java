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
import java.util.Map;
import java.util.Set;

import org.jboss.wsf.spi.management.recording.Record;
import org.jboss.wsf.spi.management.recording.RecordFilter;

/**
 * The MemoryBufferRecorder's MBean view
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Dec-2007
 */
public interface MemoryBufferRecorderMBean extends ManagedRecordProcessorMBean
{
   /**
    * Gets the records matching the provided filters. Records having the
    * same group ID are returned together.
    * 
    * @param filters
    * @return The matching records as a map GroupID->List<Record>
    */
   public Map<String, List<Record>> getMatchingRecords(RecordFilter[] filters);

   /**
    * Gets the records with the given operation. Records having the
    * same group ID are returned together.
    * 
    * @param namespace
    * @param localPart
    * @return The matching records as a map GroupID->List<Record>
    */
   public Map<String, List<Record>> getRecordsByOperation(String namespace, String localPart);

   /**
    * Gets the records with the given client host. Records having the
    * same group ID are returned together.
    * 
    * @param clientHost
    * @return The matching records as a map GroupID->List<Record>
    */
   public Map<String, List<Record>> getRecordsByClientHost(String clientHost);

   /**
    * Gets a set containing the client hosts of the last saved records.
    * 
    * @return
    */
   public Set<String> getClientHosts();

   /**
    * Gets the current buffer max size (i.e. the number of record groups stored at the same time)
    * 
    * @return
    */
   public int getMaxSize();

   public void setMaxSize(int maxSize);

   /**
    * Gets the buffer's current size
    * @return
    */
   public int getSize();
   
   /**
    * Shows all the records as a HTML table
    * 
    * @param groupRecords  whether the records should be grouped using the group ID
    * @param showDetails   whether to show headers and envelopes
    * @return
    */
   public String getRecordsAsHTMLTable(boolean groupRecords, boolean showDetails);
   
   /**
    * Shows the records with the given client host as a HTML table.
    * 
    * @param groupRecords
    * @param showDetails
    * @param clientHost
    * @return
    */
   public String getRecordsByClientHostAsHTMLTable(boolean groupRecords, boolean showDetails, String clientHost);
   
   /**
    * Shows the records with the given operation as a HTML table.
    * 
    * @param groupRecords
    * @param showDetails
    * @param namespace
    * @param localPart
    * @return
    */
   public String getRecordsByOperationAsHTMLTable(boolean groupRecords, boolean showDetails, String namespace, String localPart);
}
