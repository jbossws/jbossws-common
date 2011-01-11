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

/**
 * Simple record factory
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public class RecordFactory
{
   private static long count = 0;
   
   public static String newGroupID()
   {
      long time = System.currentTimeMillis();
      StringBuilder sb = new StringBuilder();
      synchronized (RecordFactory.class)
      {
         count++;
      }
      sb.append(count);
      sb.append("-");
      sb.append(time);
      return sb.toString();
   }
   
   public static Record newRecord(String groupID)
   {
      Record record = new RecordImpl();
      record.setGroupID(groupID);
      return record;
   }
   
   public static Record newRecord()
   {
      return newRecord(newGroupID());
   }
}
