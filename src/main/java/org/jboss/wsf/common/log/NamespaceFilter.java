/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.wsf.common.log;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * A log filter allowing logging of LogRecord depending on the
 * namespace of the Logger they have been collected by. 
 * 
 * @author Alessio Soldano, <alessio.soldano@javalinux.it>
 * @author Stefano Maestri, <stefano.maestri@javalinux.it>
 * @since 14-Jun-2007
 *
 */
public class NamespaceFilter implements Filter
{

   private Set<String> namespaces;
   private boolean show;

   public NamespaceFilter(boolean show)
   {
      this.show = show;
   }

   public boolean isLoggable(LogRecord record)
   {
      String loggerName = record.getLoggerName();
      if (loggerName == null)
      {
         return true;
      }
      else
      {
         for (String ns : namespaces)
         {
            if (loggerName.startsWith(ns))
            {
               return show;
            }
         }
         return !show;
      }
   }

   public void addNamespace(String ns)
   {
      if (namespaces == null)
         namespaces = new LinkedHashSet<String>();
      namespaces.add(ns);
   }

   public Set<String> getNamespaces()
   {
      return namespaces;
   }

   public void setNamespaces(Set<String> namespaces)
   {
      this.namespaces = namespaces;
   }

   public boolean isShow()
   {
      return show;
   }

   public void setShow(boolean show)
   {
      this.show = show;
   }

}
