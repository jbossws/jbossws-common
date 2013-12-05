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
      if (hosts instanceof List) {
         this.hosts = Collections.unmodifiableList((List<String>)hosts);
      } else {
         final List<String> l = new LinkedList<String>(hosts);
         this.hosts = Collections.unmodifiableList(l);
      }
      this.hosts.addAll(hosts);
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
