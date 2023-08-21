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
package org.jboss.ws.common.management;

import java.util.List;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordProcessor;

/**
 * MBean interface.
 * @since 15-April-2004
 */
public interface ManagedEndpointMBean
{
   void start();

   void stop();
   
   String getAddress();

   long getMinProcessingTime();

   long getMaxProcessingTime();

   long getAverageProcessingTime();

   long getTotalProcessingTime();

   long getRequestCount();

   long getFaultCount();

   long getResponseCount();
   
   long getUpdateTime();
   
   void processRecord(Record record);
   
   void addRecordProcessor(RecordProcessor processor);
   
   List<RecordProcessor> getRecordProcessors();
   
   void setRecordProcessors(List<RecordProcessor> processors);
   
}
