/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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