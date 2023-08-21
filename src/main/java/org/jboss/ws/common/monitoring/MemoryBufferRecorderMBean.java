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
import java.util.Map;
import java.util.Set;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;

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
