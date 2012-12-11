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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.namespace.QName;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.api.monitoring.Record.MessageType;

/**
 * Keeps the last received records in memory and allows user to
 * search / get statistics on them.
 * 
 * @author alessio.soldano@jboss.com
 * @since 12-Dec-2007
 */
public class MemoryBufferRecorder extends AbstractRecordProcessor implements MemoryBufferRecorderMBean, Serializable
{
   private static final long serialVersionUID = 5180978625780333738L;
   
   private Map<String, List<Record>> recentRecords = Collections.synchronizedMap(new HashMap<String, List<Record>>());
   private ConcurrentLinkedQueue<String> recentRecordGroups = new ConcurrentLinkedQueue<String>();
   private int size = 0;
   private int maxSize = 50;

   public MemoryBufferRecorder()
   {
      this.name = "MemoryBufferRecorder";
   }

   @Override
   public void processRecord(Record record)
   {
      synchronized (recentRecords)
      {
         List<Record> list = recentRecords.get(record.getGroupID());
         if (list == null)
         {
            list = new LinkedList<Record>();
            recentRecords.put(record.getGroupID(), list);
            while (size > maxSize)
            {
               this.deleteOldestRecord();
            }
            recentRecordGroups.offer(record.getGroupID());
            size++;
         }
         list.add(record);
      }
   }
   

   private void deleteOldestRecord()
   {
      String id = recentRecordGroups.poll();
      if (id != null)
      {
         recentRecords.remove(id);
         size--;
      }
   }

   private Map<String, List<Record>> getRecentRecords()
   {
      synchronized (recentRecords)
      {
         return new HashMap<String, List<Record>>(recentRecords);
      }
   }

   public Set<String> getClientHosts()
   {
      Map<String, List<Record>> map = this.getRecentRecords();
      Set<String> hosts = new HashSet<String>();
      for (List<Record> list : map.values())
      {
         for (Record record : list)
         {
            if (MessageType.INBOUND.equals(record.getMessageType()) && record.getSourceHost() != null)
            {
               hosts.add(record.getSourceHost());
            }
         }
      }
      return hosts;
   }

   public Map<String, List<Record>> getMatchingRecords(RecordFilter[] filters)
   {
      Map<String, List<Record>> map = this.getRecentRecords();
      Map<String, List<Record>> result = new HashMap<String, List<Record>>();
      for (List<Record> list : map.values())
      {
         for (Record record : list)
         {
            boolean match = true;
            for (int i = 0; i < filters.length && match; i++)
            {
               match = match && filters[i].match(record);
            }
            if (match)
            {
               result.put(record.getGroupID(), list);
               break;
            }
         }
      }
      return result;
   }

   public Map<String, List<Record>> getRecordsByClientHost(String clientHost)
   {
      RecordFilter[] filters = new RecordFilter[1];
      filters[0] = new HostFilter(clientHost, true);
      return this.getMatchingRecords(filters);
   }
   
   public String getRecordsByClientHostAsHTMLTable(boolean groupRecords, boolean showDetails, String clientHost)
   {
      return toHtml(groupRecords, showDetails, this.getRecordsByClientHost(clientHost));
   }

   public Map<String, List<Record>> getRecordsByOperation(String namespace, String localPart)
   {
      RecordFilter[] filters = new RecordFilter[1];
      filters[0] = new OperationFilter(new QName(namespace, localPart));
      return this.getMatchingRecords(filters);
   }
   
   public String getRecordsByOperationAsHTMLTable(boolean groupRecords, boolean showDetails, String namespace, String localPart)
   {
      return toHtml(groupRecords, showDetails, this.getRecordsByOperation(namespace, localPart));
   }

   public int getMaxSize()
   {
      return maxSize;
   }

   public void setMaxSize(int maxSize)
   {
      synchronized (recentRecords)
      {
         while (maxSize < size)
         {
            this.deleteOldestRecord();
         }
         this.maxSize = maxSize;
      }
   }

   public int getSize()
   {
      return size;
   }
   
   public String getRecordsAsHTMLTable(boolean groupRecords, boolean showDetails)
   {
      Map<String,List<Record>> records = this.getRecentRecords();
      return toHtml(groupRecords, showDetails, records);
   }
   
   private String toHtml(boolean groupRecords, boolean showDetails, Map<String, List<Record>> records)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("</pre></span>");
      if (!groupRecords)
         openTable(sb, showDetails);
      for (Entry<String, List<Record>> e : records.entrySet()) {
         if (groupRecords)
            openTable(sb, showDetails);
         for (Record record : e.getValue())
         {
            appendRecordRow(sb, record, showDetails);
         }
         if (groupRecords)
            closeTable(sb);
      }
      if (!groupRecords)
         closeTable(sb);
      sb.append("<pre><span class='OpResult'>");
      return sb.toString();
   }
   
   private void openTable(StringBuilder sb, boolean showDetails)
   {
      sb.append("<table border='1'><tr class='AttributesHeader'><th>Group ID</th><th>Type</th><th>Date</th><th>From</th><th>To</th><th>Operation</th>");
      if (showDetails)
         sb.append("<th>Headers</th><th>Envelope</th>");
      sb.append("</tr><tr>");
   }
   
   private void closeTable(StringBuilder sb)
   {
      sb.append("</tr></table><br />");
   }
   
   private void appendRecordRow(StringBuilder sb, Record record, boolean showDetails) {
      sb.append("<tr><td><pre>");
      sb.append(record.getGroupID() != null ? record.getGroupID() : "[Not available]");
      sb.append("</pre></td>");
      sb.append("<td><pre>");
      sb.append(record.getMessageType() != null ? record.getMessageType() : "[Not available]");
      sb.append("</pre></td>");
      sb.append("<td><pre>");
      sb.append(record.getDate() != null ? record.getDate() : "[Not available]");
      sb.append("</pre></td>");
      sb.append("<td><pre>");
      sb.append(record.getSourceHost() != null ? record.getSourceHost() : "[Not available]");
      sb.append("</pre></td>");
      sb.append("<td><pre>");
      sb.append(record.getDestinationHost() != null ? record.getDestinationHost() : "[Not available]");
      sb.append("</pre></td>");
      sb.append("<td><pre>");
      sb.append(record.getOperation() != null ? record.getOperation() : "[Not available]");
      sb.append("</pre></td>");
      if (showDetails)
      {
         sb.append("<td><pre>");
         if (record.getHeaders() != null)
         {
            for (String headerName : record.getHeaders().keySet())
            {
               sb.append(headerName);
               sb.append(": ");
               sb.append(record.getHeaders().get(headerName));
               sb.append("<br />");
            }
         }
         else
            sb.append("[Not available]");
         sb.append("</pre></td>");
         sb.append("<td><pre>");
         sb.append(record.getEnvelope() != null ? record.getEnvelope().replaceAll("<", "&lt;").replaceAll(">", "&gt;") : "[Not available]");
         sb.append("</pre></td>");
      }
      sb.append("</tr>");
   }
   
   @Override
   public Object clone() throws CloneNotSupportedException
   {
      MemoryBufferRecorder cl = (MemoryBufferRecorder)super.clone();
      cl.recentRecords = Collections.synchronizedMap(new HashMap<String, List<Record>>());
      for (String key : this.recentRecords.keySet())
      {
         List<Record> list = new LinkedList<Record>();
         for (Record record : this.recentRecords.get(key))
         {
            list.add(record);
         }
         cl.recentRecords.put(key, list);
      }
      cl.recentRecordGroups = new ConcurrentLinkedQueue<String>();
      for (String id : this.recentRecordGroups)
      {
         cl.recentRecordGroups.add(id);
      }
      cl.maxSize = this.maxSize;
      cl.size = this.size;
      return cl;
   }
}
