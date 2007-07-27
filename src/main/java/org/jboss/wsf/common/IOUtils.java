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
package org.jboss.wsf.common;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;

/**
 * IO utilites
 *
 * @author Thomas.Diesler@jboss.org
 */
public final class IOUtils
{
   // Hide the constructor
   private IOUtils()
   {
   }

   public static Writer getCharsetFileWriter(File file, String charset) throws IOException
   {
      return new OutputStreamWriter(new FileOutputStream(file), charset);
   }

   /** Copy the input stream to the output stream
    */
   public static void copyStream(OutputStream outs, InputStream ins) throws IOException
   {
      byte[] bytes = new byte[1024];
      int r = ins.read(bytes);
      while (r > 0)
      {
         outs.write(bytes, 0, r);
         r = ins.read(bytes);
      }
   }

   /** Copy the reader to the output stream
    */
   public static void copyReader(OutputStream outs, Reader reader) throws IOException
   {
      OutputStreamWriter writer = new OutputStreamWriter(outs);
      char[] bytes = new char[1024];
      int r = reader.read(bytes);
      while (r > 0)
      {
         writer.write(bytes, 0, r);
         r = reader.read(bytes);
      }
   }

   public static byte[] convertToBytes(DataHandler dh)
   {
      try
      {
         ByteArrayOutputStream buffOS = new ByteArrayOutputStream();
         dh.writeTo(buffOS);
         return buffOS.toByteArray();
      }
      catch (IOException e)
      {
         throw new WebServiceException("Unable to convert DataHandler to byte[]: " + e.getMessage());
      }
   }

   /**
    * Transform a Reader to an InputStream
    * Background is that DocumentBuilder.parse() cannot take the Reader directly
    */
   public static InputStream transformReader(Reader reader) throws IOException
   {
      int capacity = 1024;
      char[] charBuffer = new char[capacity];
      StringBuffer strBuffer = new StringBuffer(capacity);

      int len = reader.read(charBuffer, 0, capacity);
      while (len > 0)
      {
         strBuffer.append(charBuffer, 0, len);
         len = reader.read(charBuffer, 0, capacity);
      }
      return new ByteArrayInputStream(strBuffer.toString().getBytes());
   }

   public static File createTempDirectory() throws IOException
   {
      File tmpdir = null;

      try
      {
         // TODO: recursive dependency, ohoh

         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         ServerConfig config = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();        
         tmpdir = new File(config.getServerTempDir().getCanonicalPath() + "/jbossws");
         tmpdir.mkdirs();
      }
      catch (Throwable t)
      {
         // Use the Java temp directory if there is no server config (the client)
      }

      return tmpdir;
   }
}
