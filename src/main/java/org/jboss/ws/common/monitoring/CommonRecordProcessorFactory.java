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

import java.util.LinkedList;
import java.util.List;

import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.api.monitoring.RecordProcessorFactory;

/**
 * JBossWS-Common factory of record processors: includes the LogRecorder
 * and the MemoryBufferRecorder.
 * 
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 * @since 18-Jul-2011
 */
public class CommonRecordProcessorFactory implements RecordProcessorFactory
{
   public List<RecordProcessor> newRecordProcessors() {
      List<RecordProcessor> list = new LinkedList<RecordProcessor>();
      list.add(new LogRecorder());
      list.add(new MemoryBufferRecorder());
      return list;
   }
}
