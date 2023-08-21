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

package org.jboss.ws.common.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class TeeOutputStream extends OutputStream
{

   private final OutputStream[] delegates;

   public TeeOutputStream(final OutputStream first, final OutputStream second, final OutputStream... others)
   {
      // ensure preconditions
      if (first == null || second == null)
      {
         throw new IllegalArgumentException();
      }
      if (others != null && others.length > 0)
      {
         for (final OutputStream os : others)
         {
            if (os == null)
            {
               throw new IllegalArgumentException();
            }
         }
      }
      // initialize
      final int size = 2 + (others != null ? others.length : 0);
      delegates = new OutputStream[size];
      delegates[0] = first;
      delegates[1] = second;
      if (size > 2)
      {
         for (int i = 0; i < others.length; i++)
         {
            delegates[2 + i] = others[i];
         }
      }
   }

   @Override
   public void write(final int data) throws IOException
   {
      for (final OutputStream delegate : delegates)
      {
         delegate.write(data);
      }
   }

   @Override
   public void write(final byte[] data) throws IOException
   {
      for (final OutputStream delegate : delegates)
      {
         delegate.write(data);
      }
   }

   @Override
   public void write(final byte[] data, final int offset, final int length) throws IOException
   {
      for (final OutputStream delegate : delegates)
      {
         delegate.write(data, offset, length);
      }
   }

   @Override
   public void flush() throws IOException
   {
      for (final OutputStream delegate : delegates)
      {
         delegate.flush();
      }
   }

   @Override
   public void close() throws IOException
   {
      for (final OutputStream delegate : delegates)
      {
         delegate.close();
      }
   }
}