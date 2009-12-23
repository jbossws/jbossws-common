/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.common.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.jboss.wsf.common.JBossWSDocumentBuilderFactory;

/**
 * Tests for the JBossWSDocumentBuilderFactory
 * 
 * @author alessio.soldano@jboss.com
 * @since 23-Dec-2009
 *
 */
public class JBossWSDocumentBuilderFactoryTestCase extends TestCase
{
   public void testCaching() throws Exception
   {
      final String propName = "javax.xml.parsers.DocumentBuilderFactory";
      String origValue = System.getProperty(propName);
      try
      {
         //remove system prop if any, to get the same factory for a given classloader
         System.getProperties().remove(propName);
         DocumentBuilderFactory factory1 = JBossWSDocumentBuilderFactory.newInstance();
         DocumentBuilderFactory factory2 = JBossWSDocumentBuilderFactory.newInstance();
         assertTrue("Expected the same factory", factory1.equals(factory2));
         
         //set the system prop, we should get different factories every time as the classloader based cache is by-passed
         System.setProperty(propName, DummyDocumentBuilderFactory.class.getCanonicalName());
         DocumentBuilderFactory factory3 = JBossWSDocumentBuilderFactory.newInstance();
         assertTrue("Expected different factories", !factory3.equals(factory1));
         assertTrue("Expected different factories", !factory3.equals(factory2));
         DocumentBuilderFactory factory4 = JBossWSDocumentBuilderFactory.newInstance();
         assertTrue("Expected different factories", !factory4.equals(factory1));
         assertTrue("Expected different factories", !factory4.equals(factory2));
         assertTrue("Expected different factories", !factory4.equals(factory3));
         
         //remove the prop again, we should get the first factory
         System.getProperties().remove(propName);
         DocumentBuilderFactory factory5 = JBossWSDocumentBuilderFactory.newInstance();
         assertTrue("Expected the same factory", factory5.equals(factory1));
         
         ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
         try
         {
            //change context classloader
            Thread.currentThread().setContextClassLoader(new TestClassLoader(origLoader));
            DocumentBuilderFactory factory6 = JBossWSDocumentBuilderFactory.newInstance();
            assertTrue("Expected different factories", !factory6.equals(factory1));
            DocumentBuilderFactory factory7 = JBossWSDocumentBuilderFactory.newInstance();
            assertTrue("Expected the same factory", factory7.equals(factory6));
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(origLoader);
         }
      }
      finally
      {
         if (origValue == null)
         {
            System.getProperties().remove(propName);
         }
         else
         {
            System.setProperty(propName, origValue);
         }
      }
   }
   
   public void testThreadSafety() throws Exception
   {
      DocumentBuilderFactory factory = JBossWSDocumentBuilderFactory.newInstance();
      List<Callable<Boolean>> callables = new LinkedList<Callable<Boolean>>();
      for (int j = 0; j < 3; j++)
      {
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, true, true, true));
         }
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, true, true, false));
         }
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, true, false, false));
         }
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, false, false, false));
         }
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, false, false, true));
         }
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, false, true, true));
         }
         for (int i = 0; i < 10; i++)
         {
            callables.add(new TestCallable(factory, false, true, false));
         }
      }
      ExecutorService es = Executors.newFixedThreadPool(210);
      List<Future<Boolean>> futures = es.invokeAll(callables);
      for (Future<Boolean> f : futures)
      {
         assertTrue(f.get());
      }
   }
   
   /**
    * A Callable that use the provided thread safe factory to create a document builder and verifies it has the required configuration
    */
   public static class TestCallable implements Callable<Boolean>
   {
      private final DocumentBuilderFactory factory;
      private final Boolean namespaceAware;
      private final Boolean validating;
      private final Boolean XIncludeAware;
      
      public TestCallable(DocumentBuilderFactory factory, boolean namespaceAware, boolean validating, boolean includeAware)
      {
         this.factory = factory;
         this.namespaceAware = namespaceAware;
         this.validating = validating;
         this.XIncludeAware = includeAware;
      }

      public Boolean call() throws Exception
      {
         factory.setNamespaceAware(namespaceAware);
         factory.setValidating(validating);
         factory.setXIncludeAware(XIncludeAware);
         DocumentBuilder builder = factory.newDocumentBuilder();
         if (!namespaceAware.equals(builder.isNamespaceAware()))
            return false;
         if (!validating.equals(builder.isValidating()))
            return false;
         if (!XIncludeAware.equals(builder.isXIncludeAware()))
            return false;
         return true;
      }
      
   }
   
   /**
    * A ClassLoader doing nothing except falling back to its parent
    */
   public static class TestClassLoader extends ClassLoader
   {
      public TestClassLoader(ClassLoader parent)
      {
         super(parent);
      }
   }
   
}
