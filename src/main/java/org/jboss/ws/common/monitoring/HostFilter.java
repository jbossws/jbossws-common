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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;

/**
 * This filter matches records having the source/destination host equal to
 * any of the provided hosts.
 * 
 * @author alessio.soldano@jboss.com
 * @since 11-Dec-2007
 */
public class HostFilter implements RecordFilter
{
   private static final long serialVersionUID = -5935962601380315102L;
   
   private final List<String> hosts;
   private final boolean source;

   public HostFilter(String host, boolean source)
   {
      this.hosts = Collections.singletonList(host);
      this.source = source;
   }

   public HostFilter(Collection<String> hosts, boolean source)
   {
      this.hosts = Collections.unmodifiableList(new LinkedList<String>(hosts));
      this.source = source;
   }

   public boolean match(Record record)
   {
      for (String host : hosts)
      {
         if ((source && host.equalsIgnoreCase(record.getSourceHost())) || (!source && host.equalsIgnoreCase(record.getDestinationHost())))
         {
            return true;
         }
      }
      return false;
   }

   public List<String> getHosts()
   {
      return hosts;
   }

   public boolean isSource()
   {
      return source;
   }
   
   @Override
   public Object clone() throws CloneNotSupportedException
   {
      return new HostFilter(this.hosts, this.source);
   }
}
