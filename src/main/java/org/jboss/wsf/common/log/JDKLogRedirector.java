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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.log.JBossLogHandler;

/**
 * Redirects JDK Logger output to the JBoss Logger.
 * 
 * @author Alessio Soldano, <alessio.soldano@javalinux.it>
 * @author Stefano Maestri, <stefano.maestri@javalinux.it>
 * @since 14-Jun-2007
 *
 */
public class JDKLogRedirector
{
   private List<String> namespaces = new LinkedList<String>();

   public JDKLogRedirector()
   {
   }

   public void changeHandlers()
   {
      for (String ns : namespaces)
      {
         changeHandler(ns);
      }
   }

   /**
    * Modifies the jdk root logger in order not to log records coming from
    * loggers with the provided namespace; these records are then logged
    * through the JBoss Logger.
    * 
    * @param ns
    */
   public void changeHandler(String ns)
   {
      if (ns == null)
         ns = "";
      Logger.getLogger(this.getClass()).info("Changing current root logger's log handlers to hide logs with namespace " + ns);
      java.util.logging.Logger jdkRootLogger = java.util.logging.Logger.getLogger("");
      Handler[] handlers = jdkRootLogger.getHandlers();
      for (int i = 0; i < handlers.length; i++)
      {
         Handler handler = handlers[i];
         if (!(handler instanceof JBossLogHandler))
         {
            StringBuffer sb = new StringBuffer("Disableing handler ");
            sb.append(handler).append(" with level ").append(handler.getLevel());
            Logger.getLogger(this.getClass()).debug(sb);
            Filter f = handler.getFilter();
            if (f != null && f instanceof NamespaceFilter)
            {
               ((NamespaceFilter)f).addNamespace(ns);
            }
            else
            {
               NamespaceFilter nsFilter = new NamespaceFilter(false);
               nsFilter.addNamespace(ns);
               handler.setFilter(nsFilter);
            }
         }
      }
      Handler jbossLogHandler = new JBossLogHandler();
      jbossLogHandler.setLevel(Level.ALL);
      java.util.logging.Logger.getLogger(ns).addHandler(jbossLogHandler);
   }

   public void addNamespace(String ns)
   {
      namespaces.add(ns);
      changeHandler(ns);
   }

   public List<String> getNamespaces()
   {
      return namespaces;
   }

   public void setNamespaces(List<String> namespaces)
   {
      this.namespaces = namespaces;
      changeHandlers();
   }
}
