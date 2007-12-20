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

// $Id$

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
 * @author Thomas.Diesler@jboss.com
 * @since 14-Jun-2007
 */
public class JDKLogHandler extends Handler
{
   public JDKLogHandler()
   {
      super.setFormatter(new SimpleFormatter());
   }

   @Override
   public void publish(LogRecord record)
   {
      if (isLoggable(record))
      {
         Logger logger = getJBossLogger(record);
         Level level = record.getLevel();
         if (level == Level.FINER || level == Level.FINEST)
         {
            String msg = getMessage(record);
            logger.trace(msg);
         }
         else if (level == Level.FINE)
         {
            String msg = getMessage(record);
            logger.debug(msg);
         }
         else if (level == Level.INFO || level == Level.CONFIG)
         {
            String msg = getMessage(record);
            logger.info(msg);
         }
         else if (level == Level.WARNING)
         {
            String msg = getMessage(record);
            logger.warn(msg);
         }
         else if (level == Level.SEVERE)
         {
            String msg = getMessage(record);
            logger.error(msg);
         }
         else if (level == Level.OFF)
         {
            // do nothing
         }
      }
   }

   private Logger getJBossLogger(LogRecord record)
   {
      return Logger.getLogger(record.getLoggerName());
   }

   private String getMessage(LogRecord record)
   {
      String msg = null;
      try
      {
         msg = getFormatter().formatMessage(record);
      }
      catch (Exception ex)
      {
         // We don't want to throw an exception here, but we
         // report the exception to any registered ErrorManager.
         reportError("Cannot obtain message from log record", ex, ErrorManager.FORMAT_FAILURE);
      }
      return msg;
   }

   @Override
   public boolean isLoggable(LogRecord record)
   {
      Logger logger = getJBossLogger(record);
      Level level = record.getLevel();

      boolean isLoggable = false;
      if (level == Level.FINER || level == Level.FINEST)
      {
         isLoggable = logger.isTraceEnabled();
      }
      else if (level == Level.FINE)
      {
         isLoggable = logger.isDebugEnabled();
      }
      else if (level == Level.INFO || level == Level.CONFIG)
      {
         isLoggable = logger.isInfoEnabled();
      }
      else if (level == Level.SEVERE || level == Level.WARNING)
      {
         isLoggable = true;
      }
      return isLoggable;
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