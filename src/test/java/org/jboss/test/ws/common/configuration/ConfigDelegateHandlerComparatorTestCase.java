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
package org.jboss.test.ws.common.configuration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.jboss.ws.common.configuration.ConfigDelegateHandler;
import org.jboss.ws.common.configuration.ConfigDelegateHandlerComparator;

/**
 * Test the ConfigDelegateHandlerComparator
 *
 * @author alessio.soldano@jboss.org
 * @since 07-Jun-2012
 */
public class ConfigDelegateHandlerComparatorTestCase extends TestCase
{
   public void testComparator() throws Exception
   {
      List<NamedHandler> list = new LinkedList<NamedHandler>();
      list.add(new TestConfigDelegateHandler(new TestHandler("post1"), false));
      list.add(new TestHandler("h1"));
      list.add(new TestConfigDelegateHandler(new TestHandler("post2"), false));
      list.add(new TestConfigDelegateHandler(new TestHandler("pre1"), true));
      list.add(new TestConfigDelegateHandler(new TestHandler("pre2"), true));
      list.add(new TestHandler("h2"));
      list.add(new TestHandler("h3"));
      list.add(new TestHandler("h4"));
      list.add(new TestHandler("h5"));
      list.add(new TestConfigDelegateHandler(new TestHandler("post3"), false));
      list.add(new TestConfigDelegateHandler(new TestHandler("pre3"), true));
      list.add(new TestConfigDelegateHandler(new TestHandler("pre4"), true));
      list.add(new TestHandler("h6"));
      list.add(new TestConfigDelegateHandler(new TestHandler("post4"), false));

      Collections.sort(list, new ConfigDelegateHandlerComparator<Handler<MessageContext>>());

      StringBuilder sb = new StringBuilder();
      for (NamedHandler h : list)
      {
         sb.append(h.getName());
         sb.append(" ");
      }
      assertEquals("pre1 pre2 pre3 pre4 h1 h2 h3 h4 h5 h6 post1 post2 post3 post4", sb.toString().trim());
   }

   private static interface NamedHandler extends Handler<MessageContext>
   {

      public String getName();

   }

   private static class TestConfigDelegateHandler extends ConfigDelegateHandler<MessageContext> implements NamedHandler
   {

      public TestConfigDelegateHandler(Handler<MessageContext> delegate, boolean isPre)
      {
         super(delegate, isPre);
      }

      @Override
      public String getName()
      {
         return delegate instanceof NamedHandler ? ((NamedHandler) delegate).getName() : null;
      }

   }

   private static class TestHandler implements NamedHandler
   {

      private String name;

      public TestHandler(String name)
      {
         this.name = name;
      }

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public boolean handleMessage(MessageContext context)
      {
         return false;
      }

      @Override
      public boolean handleFault(MessageContext context)
      {
         return false;
      }

      @Override
      public void close(MessageContext context)
      {
      }
   }
}
