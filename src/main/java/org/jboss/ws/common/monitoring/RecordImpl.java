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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.ws.api.monitoring.Record;

/**
 * Default Record implementation
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public class RecordImpl implements Record
{
   private static final long serialVersionUID = -2421022916458208468L;
   
   private String sourceHost;
   private String destinationHost;
   private Map<String, List<String>> headers = new HashMap<String, List<String>>();
   private MessageType messageType;
   private QName operation;
   private String groupID;
   private Date date;
   private String envelope;
   
   RecordImpl()
   {
      
   }

   public void addHeaders(String key, List<String> value)
   {
      headers.put(key, value);
   }

   public Map<String, List<String>> getHeaders()
   {
      return headers;
   }

   public MessageType getMessageType()
   {
      return messageType;
   }

   public void setMessageType(MessageType messageType)
   {
      this.messageType = messageType;
   }

   public String getSourceHost()
   {
      return sourceHost;
   }

   public void setSourceHost(String sourceHost)
   {
      this.sourceHost = sourceHost;
   }

   public String getDestinationHost()
   {
      return destinationHost;
   }

   public void setDestinationHost(String destinationHost)
   {
      this.destinationHost = destinationHost;
   }

   public void setHeaders(Map<String, List<String>> headers)
   {
      if (headers != null)
         this.headers = new HashMap<String, List<String>>(headers);
   }

   public String getGroupID()
   {
      return groupID;
   }

   public QName getOperation()
   {
      return operation;
   }

   public void setGroupID(String groupID)
   {
      this.groupID = groupID;
   }

   public void setOperation(QName operation)
   {
      this.operation = operation;
   }

   public Date getDate()
   {
      return date;
   }

   public void setDate(Date date)
   {
      this.date = date;
   }
   
   public void setEnvelope(String envelope)
   {
      this.envelope = envelope;
   }
   
   public String getEnvelope()
   {
      return envelope;
   }

}
