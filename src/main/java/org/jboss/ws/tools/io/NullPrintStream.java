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
package org.jboss.ws.tools.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Print stream singleton that does nothing
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 12, 2007
 */
public final class NullPrintStream extends PrintStream
{
   
   private static final PrintStream instance = new NullPrintStream();
   
   public static PrintStream getInstance()
   {
      return instance;
   }
   
   private NullPrintStream()
   {
      super(new ByteArrayOutputStream());
   }

   @Override
   public PrintStream append(char c)
   {
      return this;
   }

   @Override
   public PrintStream append(CharSequence csq, int start, int end)
   {
      return this;
   }

   @Override
   public PrintStream append(CharSequence csq)
   {
      return this;
   }

   @Override
   public boolean checkError()
   {
      return false;
   }

   @Override
   public void close()
   {
   }

   @Override
   public void flush()
   {
   }

   @Override
   public PrintStream format(Locale l, String format, Object... args)
   {
      return this;
   }

   @Override
   public PrintStream format(String format, Object... args)
   {
      return this;
   }

   @Override
   public void print(boolean b)
   {
   }

   @Override
   public void print(char c)
   {
   }

   @Override
   public void print(char[] s)
   {
   }

   @Override
   public void print(double d)
   {
   }

   @Override
   public void print(float f)
   {
   }

   @Override
   public void print(int i)
   {
   }

   @Override
   public void print(long l)
   {
   }

   @Override
   public void print(Object obj)
   {
   }

   @Override
   public void print(String s)
   {
   }

   @Override
   public PrintStream printf(Locale l, String format, Object... args)
   {
      return this;
   }

   @Override
   public PrintStream printf(String format, Object... args)
   {
      return this;
   }

   @Override
   public void println()
   {
   }

   @Override
   public void println(boolean x)
   {
   }

   @Override
   public void println(char x)
   {
   }

   @Override
   public void println(char[] x)
   {
   }

   @Override
   public void println(double x)
   {
   }

   @Override
   public void println(float x)
   {
   }

   @Override
   public void println(int x)
   {
   }

   @Override
   public void println(long x)
   {
   }

   @Override
   public void println(Object x)
   {
   }

   @Override
   public void println(String x)
   {
   }

   @Override
   protected void setError()
   {
   }

   @Override
   public void write(byte[] buf, int off, int len)
   {
   }

   @Override
   public void write(int b)
   {
   }

   @Override
   public void write(byte[] b) throws IOException
   {
   }

}
