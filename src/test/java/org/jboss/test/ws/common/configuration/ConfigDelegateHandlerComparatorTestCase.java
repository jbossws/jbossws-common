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
