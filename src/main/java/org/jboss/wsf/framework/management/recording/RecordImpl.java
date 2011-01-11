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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.wsf.spi.management.recording.Record;

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
