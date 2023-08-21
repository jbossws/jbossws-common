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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import jakarta.activation.DataHandler;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;

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
   
   public static String readAndCloseStream(InputStream is) throws IOException
   {
      return readAndCloseStream(is, "UTF-8");
   }
   
   public static String readAndCloseStream(InputStream is, String charsetName) throws IOException
   {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      copyStream(bos, is);
      return bos.toString(charsetName);
   }

   /** Copy the input stream to the output stream
    */
   public static void copyStream(OutputStream outs, InputStream ins) throws IOException
   {
      try
      {
         byte[] bytes = new byte[1024];
         int r = ins.read(bytes);
         while (r > 0)
         {
            outs.write(bytes, 0, r);
            r = ins.read(bytes);
         }
      }
      catch (IOException e)
      {
         throw e;
      }
      finally{
         ins.close();
      }
   }

   /** Copy the reader to the output stream
    */
   public static void copyReader(OutputStream outs, Reader reader) throws IOException
   {
      try
      {
         OutputStreamWriter writer = new OutputStreamWriter(outs, StandardCharsets.UTF_8);
         char[] bytes = new char[1024];
         int r = reader.read(bytes);
         while (r > 0)
         {
            writer.write(bytes, 0, r);
            r = reader.read(bytes);
         }
      }
      catch (IOException e)
      {
         throw e;
      }
      finally{
         reader.close();
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
         throw Messages.MESSAGES.unableToConvertDataHandler(e, dh != null ? dh.getName() : null);
      }
   }

   public static File createTempDirectory() throws IOException
   {
      File tmpdir = null;

      try
      {
         // TODO: recursive dependency, ohoh

         ServerConfig config = SPIProvider.getInstance().getSPI(ServerConfigFactory.class).getServerConfig();        
         tmpdir = new File(config.getServerTempDir().getCanonicalPath() + "/jbossws");
         if (!tmpdir.mkdirs()) {
             tmpdir = null;
         }
      }
      catch (Throwable t)
      {
         // Use the Java temp directory if there is no server config (the client)
          tmpdir = null;
      }

      return tmpdir;
   }
}
