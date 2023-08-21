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
package org.jboss.ws.common.utils;

import static org.jboss.ws.common.Messages.MESSAGES;

import java.util.HashMap;

/**
 * Utility class for resolving predefined XML entity and character references.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class XMLPredefinedEntityReferenceResolver
{
   private static final HashMap<String, Character> entities = new HashMap<String, Character>(8);

   static
   {
      entities.put("quot", '"');
      entities.put("amp", '&');
      entities.put("lt", '<');
      entities.put("gt", '>');
      entities.put("apos", '\'');
   }

   private static int resolveCharRef(String source, int pos, StringBuilder builder)
   {
      int radix = 10;
      if (source.charAt(pos += 2) == 'x')
      {
         pos++;
         radix = 16;
      }

      int end = source.indexOf(';', pos);
      if (end == -1)
         throw MESSAGES.entityResolutionInvalidCharacterReference(source);

      int c = Integer.parseInt(source.substring(pos, end), radix);
      builder.append((char) c);

      return end + 1;
   }

   private static int resolveEntityRef(String source, int pos, StringBuilder builder)
   {
      int end = source.indexOf(';', ++pos);
      if (end == -1)
         throw MESSAGES.entityResolutionInvalidEntityReference(source);

      String entity = source.substring(pos, end);
      Character c = entities.get(entity);
      if (c == null)
         throw MESSAGES.entityResolutionInvalidEntity(entity);

      builder.append(c.charValue());

      return end + 1;
   }

   /**
    * Transforms an XML normalized string by resolving all predefined character and entity references
    *
    * @param normalized an XML normalized string
    * @return a standard java string that is no longer XML normalized
    */
   public static String resolve(String normalized)
   {
      StringBuilder builder = new StringBuilder();
      int end = normalized.length();
      int pos = normalized.indexOf('&');
      int last = 0;

      // No references
      if (pos == -1)
         return normalized;

      while (pos != -1)
      {
         String sub = normalized.subSequence(last, pos).toString();
         builder.append(sub);
         
         int peek = pos + 1;
         if (peek == end)
            throw MESSAGES.entityResolutionInvalidEntityReference(normalized);

         if (normalized.charAt(peek) == '#')
            pos = resolveCharRef(normalized, pos, builder);
         else
            pos = resolveEntityRef(normalized, pos, builder);

         last = pos;
         pos = normalized.indexOf('&', pos);
      }

      if (last < end)
      {
         String sub = normalized.subSequence(last, end).toString();
         builder.append(sub);
      }

      return builder.toString();
   }
}
