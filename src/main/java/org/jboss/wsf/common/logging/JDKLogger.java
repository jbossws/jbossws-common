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
package org.jboss.wsf.common.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A LogManager that adds a JBossLogHandler to every Logger
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 18-Dec-2007
 */
public class JDKLogger extends Logger
{
   protected JDKLogger(String name)
   {
      super(name, null);

      org.jboss.logging.Logger jbl = org.jboss.logging.Logger.getLogger(name);
      if (jbl.isTraceEnabled())
      {
         setLevel(Level.FINEST);
      }
      else if (jbl.isDebugEnabled())
      {
         setLevel(Level.FINE);
      }
      else if (jbl.isInfoEnabled())
      {
         setLevel(Level.INFO);
      }
   }
}
