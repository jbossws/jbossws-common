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
package org.jboss.wsf.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for JBossWS test cases
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public abstract class JBossWSTest extends TestCase
{
   // provide logging
   protected Logger log = Logger.getLogger(getClass().getName());

   private JBossWSTestHelper delegate = new JBossWSTestHelper();

   public JBossWSTest()
   {
   }

   public JBossWSTest(String name)
   {
      super(name);
   }

   public MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }

   public boolean isTargetJBoss50()
   {
      return delegate.isTargetJBoss50();
   }

   public boolean isTargetJBoss42()
   {
      return delegate.isTargetJBoss42();
   }

   public boolean isTargetJBoss40()
   {
      return delegate.isTargetJBoss40();
   }

   public boolean isIntegrationNative()
   {
      return delegate.isIntegrationNative();

   }

   public boolean isIntegrationMetro()
   {
      return delegate.isIntegrationMetro();
   }

   public boolean isIntegrationCXF()
   {
      return delegate.isIntegrationCXF();
   }

   /** Deploy the given archive
    */
   public void deploy(String archive) throws Exception
   {
      delegate.deploy(archive);
   }

   /** Undeploy the given archive
    */
   public void undeploy(String archive) throws Exception
   {
      delegate.undeploy(archive);
   }

   public String getServerHost()
   {
      return JBossWSTestHelper.getServerHost();
   }

   public File getArchiveFile(String archive)
   {
      return delegate.getArchiveFile(archive);
   }

   public URL getArchiveURL(String archive) throws MalformedURLException
   {
      return delegate.getArchiveURL(archive);
   }

   public File getResourceFile(String resource)
   {
      return delegate.getResourceFile(resource);
   }

   public URL getResourceURL(String resource) throws MalformedURLException
   {
      return delegate.getResourceURL(resource);
   }

   public File createResourceFile(String filename)
   {
      File resDir = new File(JBossWSTestHelper.getTestResourcesDir());
      File file = new File(resDir.getAbsolutePath() + File.separator + filename);
      return file;
   }

   public File createResourceFile(File parent, String filename)
   {
      File file = new File(parent, filename);
      return file;
   }

   /** Get the client's env context for a given name.
    */
   @SuppressWarnings("unchecked")
   protected InitialContext getInitialContext(String clientName) throws NamingException
   {
      InitialContext iniCtx = new InitialContext();
      Hashtable env = iniCtx.getEnvironment();
      env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
      env.put("j2ee.clientName", clientName);
      return new InitialContext(env);
   }

   /** Get the client's env context
    */
   protected InitialContext getInitialContext() throws NamingException
   {
      return getInitialContext("jbossws-client");
   }

   public static void assertEquals(Element expElement, Element wasElement, boolean ignoreWhitespace)
   {
      normalizeWhitespace(expElement, ignoreWhitespace);
      normalizeWhitespace(wasElement, ignoreWhitespace);
      String expStr = DOMWriter.printNode(expElement, false);
      String wasStr = DOMWriter.printNode(wasElement, false);
      if (expStr.equals(wasStr) == false)
      {
         System.out.println("\nExp: " + expStr + "\nWas: " + wasStr);
         Logger.getLogger(JBossWSTest.class).error("\nExp: " + expStr + "\nWas: " + wasStr);
      }
      assertEquals(expStr, wasStr);
   }

   public static void assertEquals(Element expElement, Element wasElement)
   {
      assertEquals(expElement, wasElement, false);
   }

   public static void assertEquals(Object exp, Object was)
   {
      if (exp instanceof Object[] && was instanceof Object[])
         assertEqualsArray((Object[])exp, (Object[])was);
      else if (exp instanceof byte[] && was instanceof byte[])
         assertEqualsArray((byte[])exp, (byte[])was);
      else if (exp instanceof boolean[] && was instanceof boolean[])
         assertEqualsArray((boolean[])exp, (boolean[])was);
      else if (exp instanceof short[] && was instanceof short[])
         assertEqualsArray((short[])exp, (short[])was);
      else if (exp instanceof int[] && was instanceof int[])
         assertEqualsArray((int[])exp, (int[])was);
      else if (exp instanceof long[] && was instanceof long[])
         assertEqualsArray((long[])exp, (long[])was);
      else if (exp instanceof float[] && was instanceof float[])
         assertEqualsArray((float[])exp, (float[])was);
      else if (exp instanceof double[] && was instanceof double[])
         assertEqualsArray((double[])exp, (double[])was);
      else
         TestCase.assertEquals(exp, was);
   }

   private static void assertEqualsArray(Object[] exp, Object[] was)
   {
      if (exp == null && was == null)
         return;

      if (exp != null && was != null)
      {
         if (exp.length != was.length)
         {
            fail("Expected <" + exp.length + "> array items, but was <" + was.length + ">");
         }
         else
         {
            for (int i = 0; i < exp.length; i++)
            {

               Object compExp = exp[i];
               Object compWas = was[i];
               assertEquals(compExp, compWas);
            }
         }
      }
      else if (exp == null)
      {
         fail("Expected a null array, but was: " + Arrays.asList(was));
      }
      else if (was == null)
      {
         fail("Expected " + Arrays.asList(exp) + ", but was: null");
      }
   }

   private static void assertEqualsArray(byte[] exp, byte[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(boolean[] exp, boolean[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(short[] exp, short[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(int[] exp, int[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(long[] exp, long[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(float[] exp, float[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   private static void assertEqualsArray(double[] exp, double[] was)
   {
      assertTrue("Arrays don't match", Arrays.equals(exp, was));
   }

   /** Removes whitespace text nodes if they have an element sibling.
    */
   private static void normalizeWhitespace(Element element, boolean ignoreWhitespace)
   {
      boolean hasChildElement = false;
      ArrayList<Node> toDetach = new ArrayList<Node>();

      NodeList childNodes = element.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++)
      {
         Node node = childNodes.item(i);
         if (node.getNodeType() == Node.TEXT_NODE)
         {
            String nodeValue = node.getNodeValue();
            if (nodeValue.trim().length() == 0)
               toDetach.add(node);
         }
         if (node.getNodeType() == Node.ELEMENT_NODE)
         {
            normalizeWhitespace((Element)node, ignoreWhitespace);
            hasChildElement = true;
         }
      }

      // remove whitespace nodes
      if (hasChildElement || ignoreWhitespace)
      {
         Iterator<Node> it = toDetach.iterator();
         while (it.hasNext())
         {
            Node whiteSpaceNode = it.next();
            element.removeChild(whiteSpaceNode);
         }
      }
   }
}
