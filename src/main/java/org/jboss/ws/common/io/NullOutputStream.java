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
public final class NullOutputStream extends OutputStream
{

   private static final NullOutputStream INSTANCE = new NullOutputStream();

   private NullOutputStream()
   {
   }

   public static NullOutputStream getInstance()
   {
      return INSTANCE;
   }

   @Override
   public void write(final int data) throws IOException
   {
   }

   @Override
   public void write(final byte[] data) throws IOException
   {
   }

   @Override
   public void write(final byte[] data, final int offset, final int length) throws IOException
   {
   }

   @Override
   public void flush() throws IOException
   {
   }

   @Override
   public void close() throws IOException
   {
   }
}