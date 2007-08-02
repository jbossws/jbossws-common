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

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.jboss.logging.Logger;

/**
 * A Handler (java.util.logging.Handler) class redirecting messages
 * to the jboss logging system.
 * 
 * @author Alessio Soldano, <alessio.soldano@javalinux.it>
 * @author Stefano Maestri, <stefano.maestri@javalinux.it>
 * @since 14-Jun-2007
 *
 */
public class JBossLogHandler extends Handler
{

   public JBossLogHandler()
   {
      super.setFormatter(new SimpleFormatter());
   }

   @Override
   public void publish(LogRecord record)
   {
      if (!isLoggable(record))
      {
         return;
      }
      String msg;
      try
      {
         msg = getFormatter().formatMessage(record);
      }
      catch (Exception ex)
      {
         // We don't want to throw an exception here, but we
         // report the exception to any registered ErrorManager.
         reportError(null, ex, ErrorManager.FORMAT_FAILURE);
         return;
      }
      if (record.getLevel() == Level.INFO)
      {
         Logger.getLogger(record.getSourceClassName()).info(msg);
      }
      else if (record.getLevel() == Level.SEVERE)
      {
         Logger.getLogger(record.getSourceClassName()).error(msg);
      }
      else if (record.getLevel() == Level.WARNING)
      {
         Logger.getLogger(record.getSourceClassName()).warn(msg);
      }
      else if (record.getLevel() == Level.FINE)
      {
         Logger.getLogger(record.getSourceClassName()).debug(msg);
      }
      else if (record.getLevel() == Level.FINER || record.getLevel() == Level.FINEST)
      {
         Logger.getLogger(record.getSourceClassName()).trace(msg);
      }
      else
      {
         Logger.getLogger(record.getSourceClassName()).debug(msg);
      }
   }

   @Override
   public boolean isLoggable(LogRecord record)
   {
      if (record == null)
      {
         return false;
      }
      return super.isLoggable(record);
   }

   @Override
   public void flush()
   {
      //nothing to do
   }

   @Override
   public void close() throws SecurityException
   {
      //nothing to do
   }

}