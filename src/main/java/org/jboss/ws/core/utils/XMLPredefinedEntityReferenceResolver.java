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
package org.jboss.ws.core.utils;

import java.util.HashMap;

/**
 * Utility class for resolving predefined XML entity and character references.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class XMLPredefinedEntityReferenceResolver
{
   private static HashMap<String, Character> entities = new HashMap<String, Character>();

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
         throw new IllegalArgumentException("Invalid character reference");

      int c = Integer.parseInt(source.substring(pos, end), radix);
      builder.append((char) c);

      return end + 1;
   }

   private static int resolveEntityRef(String source, int pos, StringBuilder builder)
   {
      int end = source.indexOf(';', ++pos);
      if (end == -1)
         throw new IllegalArgumentException("Invalid entity reference");

      String entity = source.substring(pos, end);
      Character c = entities.get(entity);
      if (c == null)
         throw new IllegalArgumentException("Invalid entity: " + entity);

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
            throw new IllegalArgumentException("Invalid entity reference");

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
