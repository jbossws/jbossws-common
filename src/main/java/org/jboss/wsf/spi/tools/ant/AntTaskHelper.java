/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.spi.tools.ant;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.apache.tools.ant.types.Environment.Variable;

/**
 * Helper class for ANT tasks.
 * 
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class AntTaskHelper
{
   /**
    * Constructor.
    */
   private AntTaskHelper()
   {
      // forbidden constructor
   }

   /**
    * Converts array of JVM arguments to ANT SysProperties object.
    * 
    * @param arguments to be converted.
    * @return ANT SysProperties object.
    */
   static SysProperties toSystemProperties(final String[] arguments)
   {
      final SysProperties retVal = new SysProperties();

      if (arguments != null && arguments.length != 0)
      {
         for (final String argument : arguments)
         {
            if (argument.startsWith("-D"))
            {
               Variable var = AntTaskHelper.toVariable(argument);
               retVal.addVariable(var);
            }
         }
      }

      return retVal;
   }

   /**
    * Converts JVM property of format -Dkey=value to ANT Variable object.
    *
    * @param argument to be converted
    * @return ANT Variable object
    */
   private static Variable toVariable(final String argument)
   {
      final Variable retVal = new Variable();
      final int equalSignIndex = argument.indexOf('=');

      if (equalSignIndex == -1)
      {
         final String key = argument.substring(2);
         retVal.setKey(key);
      }
      else
      {
         final String key = argument.substring(2, equalSignIndex);
         retVal.setKey(key);
         final String value = argument.substring(equalSignIndex + 1);
         retVal.setValue(value);
      }

      return retVal;
   }
}
