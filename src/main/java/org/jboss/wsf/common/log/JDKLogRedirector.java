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
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Redirects JDK Logger output to the JBoss Logger.
 * 
 * @author Alessio Soldano, <alessio.soldano@javalinux.it>
 * @author Stefano Maestri, <stefano.maestri@javalinux.it>
 * @author Thomas.Diesler@jboss.com
 * @since 14-Jun-2007
 */
public class JDKLogRedirector
{
   private List<String> namespaces = new LinkedList<String>();

   public void addNamespace(String ns)
   {
      namespaces.add(ns);
   }

   public List<String> getNamespaces()
   {
      return namespaces;
   }

   public void setNamespaces(List<String> namespaces)
   {
      this.namespaces = namespaces;
   }

   public void start()
   {
      removeRootConsoleHandler();
      addNamespaceHandlers();
   }

   private void removeRootConsoleHandler()
   {
      LogManager logManager = LogManager.getLogManager();
      Logger root = logManager.getLogger("");
      while (root.getParent() != null)
         root = root.getParent();

      Handler[] handlers = root.getHandlers();
      for (int i = 0; i < handlers.length; i++)
      {
         Handler handler = handlers[i];
         if (handler instanceof ConsoleHandler)
            root.removeHandler(handler);
      }
   }

   private void addNamespaceHandlers()
   {
      LogManager logManager = LogManager.getLogManager();
      for (String ns : namespaces)
      {
         JDKLogger log = new JDKLogger(ns);
         log.addHandler(new JDKLogHandler());
         logManager.addLogger(log);
      }
   }
}
