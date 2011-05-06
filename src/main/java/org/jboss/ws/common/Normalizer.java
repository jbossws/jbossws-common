/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
*
* @author <a href="mailto:mvecera@redhat.com">Martin Vecera</a>
* @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
* @since 09-Dic-2009
* 
*/
final public class Normalizer
{
   private static final Pattern PATTERN = Pattern.compile("[&<>'\"\r\n]");

   public static String normalize(String strValue)
   {
      return normalize(strValue, false);
   }

   public static String normalize(String strValue, boolean canonical)
   {
      Matcher m = PATTERN.matcher(strValue);
      if (m.find())
      {
         int pos = m.start(); // we can use previous match to skip some part at the string beginning
         int len = strValue.length(); // just a single call to length()
         char[] input = new char[len]; // this is to ommit calls to String.charAt()
         strValue.getChars(0, len, input, 0);
         StringBuilder sb = new StringBuilder(len * 3); // faster than StringBuffer, not thread safe

         int copyStart = 0;

         for (int i = pos; i < len; i++)
         {
            char ch = input[i];
            switch (ch)
            {
               case '<':
                  if (copyStart < i)
                  {
                     sb.append(input, copyStart, i - copyStart);
                  }
                  copyStart = i + 1;
                  sb.append("&lt;");
                  break;
               case '>':
                  if (copyStart < i)
                  {
                     sb.append(input, copyStart, i - copyStart);
                  }
                  copyStart = i + 1;
                  sb.append("&gt;");
                  break;
               case '"':
                  if (copyStart < i)
                  {
                     sb.append(input, copyStart, i - copyStart);
                  }
                  copyStart = i + 1;
                  sb.append("&quot;");
                  break;
               case '\'':
                  if (copyStart < i)
                  {
                     sb.append(input, copyStart, i - copyStart);
                  }
                  copyStart = i + 1;
                  sb.append("&apos;");
                  break;
               case '&':
                  if (copyStart < i)
                  {
                     sb.append(input, copyStart, i - copyStart);
                  }
                  copyStart = i + 1;
                  sb.append("&amp;");
                  break;
               case '\r':
               case '\n':
                  if (canonical)
                  {
                     if (copyStart < i)
                     {
                        sb.append(input, copyStart, i - copyStart);
                     }
                     copyStart = i + 1;
                     sb.append("&#");
                     sb.append(Integer.toString(ch));
                     sb.append(';');
                     break;
                  }

            }
         }
         if (copyStart < len)
         {
            sb.append(input, copyStart, len - copyStart);
         }

         return sb.toString();
      }
      else
      {
         return strValue;
      }
   }
}
