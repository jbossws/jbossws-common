/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
