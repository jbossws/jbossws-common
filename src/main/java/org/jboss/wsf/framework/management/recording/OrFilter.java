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

import org.jboss.wsf.spi.management.recording.Record;
import org.jboss.wsf.spi.management.recording.RecordFilter;

/**
 * A simple record filter acting like the OR operator
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Dec-2007
 *
 */
public class OrFilter implements RecordFilter
{
   private static final long serialVersionUID = 3372989051076281559L;
   
   private RecordFilter filter1;
   private RecordFilter filter2;
   
   public OrFilter(RecordFilter filter1, RecordFilter filter2)
   {
      this.filter1 = filter1;
      this.filter2 = filter2;
   }
   
   public boolean match(Record record)
   {
      return filter1.match(record) || filter2.match(record);
   }

   public RecordFilter getFilter1()
   {
      return filter1;
   }

   public RecordFilter getFilter2()
   {
      return filter2;
   }

   @Override
   public Object clone() throws CloneNotSupportedException
   {
      OrFilter retObj = (OrFilter)super.clone();
      retObj.filter1 = (RecordFilter)this.filter1.clone();
      retObj.filter2 = (RecordFilter)this.filter2.clone();
      return retObj;
   }
}
