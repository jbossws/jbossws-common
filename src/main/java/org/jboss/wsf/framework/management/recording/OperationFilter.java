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

import javax.xml.namespace.QName;

import org.jboss.wsf.spi.management.recording.Record;
import org.jboss.wsf.spi.management.recording.RecordFilter;

/**
 * This filter matches records having a given operation QName value.
 * 
 * @author alessio.soldano@jboss.com
 * @since 11-Dec-2007
 */
public class OperationFilter implements RecordFilter
{
   private static final long serialVersionUID = -726794729964445956L;
   
   private QName operation;

   public OperationFilter(QName operation)
   {
      this.operation = operation;
   }

   public boolean match(Record record)
   {
      if (record != null)
      {
         if (record.getOperation() == null && operation == null)
         {
            return true;
         }
         else if (operation != null && operation.equals(record.getOperation()))
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return true;
      }
   }

   public QName getOperation()
   {
      return operation;
   }

   @Override
   public Object clone() throws CloneNotSupportedException
   {
      OperationFilter retObj = (OperationFilter)super.clone();
      retObj.operation = this.operation;
      return retObj;
   }
}
