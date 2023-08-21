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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.Record.MessageType;
import org.jboss.ws.common.Loggers;

/**
 * A simple record processor that writes records to the configured log.
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public class LogRecorder extends AbstractRecordProcessor implements Serializable
{
   private static final long serialVersionUID = -7126227194320867819L;
   
   public LogRecorder()
   {
      this.name = "LogRecorder";
   }
   
   @Override
   public void processRecord(Record record)
   {
      if (!Loggers.MONITORING_LOGGER.isDebugEnabled()) {
         return;
      }
      StringBuilder sb = new StringBuilder();
      if (this.isProcessMessageType())
      {
         if (record.getMessageType() == MessageType.INBOUND)
         {
            sb.append("INBOUND MESSAGE ");
            if (this.isProcessSourceHost() && record.getSourceHost() != null)
            {
               sb.append("from ");
               sb.append(record.getSourceHost());
            }
         }
         else if (record.getMessageType() == MessageType.OUTBOUND)
         {
            sb.append("OUTBOUND MESSAGE ");
            if (this.isProcessDestinationHost() && record.getDestinationHost() != null)
            {
               sb.append("to ");
               sb.append(record.getDestinationHost());
            }
         }
         else
         {
            if (this.isProcessSourceHost() && record.getSourceHost() != null)
            {
               sb.append("from ");
               sb.append(record.getSourceHost());
            }
            if (this.isProcessDestinationHost() && record.getDestinationHost() != null)
            {
               sb.append("to ");
               sb.append(record.getDestinationHost());
            }
         }
      }
      else
      {
         sb.append("MESSAGE");
      }
      sb.append(":");
      if (this.isProcessDate())
      {
         sb.append("\nDate: ");
         sb.append(record.getDate());
      }
      sb.append("\nGroupID: ");
      sb.append(record.getGroupID());
      if (this.isProcessOperation())
      {
         sb.append("\nOperation: ");
         sb.append(record.getOperation());
      }
      sb.append("\n");
      Map<String, List<String>> headers = record.getHeaders();
      if (this.isProcessHeaders() && headers != null)
      {
         for (Entry<String, List<String>> e : headers.entrySet())
         {
            sb.append(e.getKey());
            sb.append(": ");
            for (String h : e.getValue())
            {
               sb.append(h);
               sb.append("; ");
            }
            sb.append("\n");
         }
      }
      sb.append("\n");
      if (this.isProcessEnvelope())
      {
         sb.append(record.getEnvelope());
      }
      Loggers.MONITORING_LOGGER.debug(LogRecorder.class.getName(), sb.toString(), null);
   }
}
