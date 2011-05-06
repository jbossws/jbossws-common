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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.Record.MessageType;

/**
 * A simple record processor that writes records to the configured log.
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public class LogRecorder extends AbstractRecordProcessor implements Serializable
{
   private static final long serialVersionUID = -7126227194320867819L;
   
   private Logger log = Logger.getLogger(this.getClass());

   public LogRecorder()
   {
      this.name = "LogRecorder";
   }
   
   @Override
   public void processRecord(Record record)
   {
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
            log.warn("Unknown message type: " + record.getMessageType());
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
         for (String key : headers.keySet())
         {
            sb.append(key);
            sb.append(": ");
            for (String h : headers.get(key))
            {
               sb.append(h);
               sb.append("; ");
            }
            sb.append("\n");
         }
         sb.append("\n");
      }
      sb.append("\n");
      if (this.isProcessEnvelope())
      {
         sb.append(record.getEnvelope());
      }
      log.debug(sb.toString());
   }
}
