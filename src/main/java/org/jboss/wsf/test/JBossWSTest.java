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
package org.jboss.wsf.test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMWriter;
import org.jboss.wsf.common.concurrent.CopyJob;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for JBossWS test cases
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 */
public abstract class JBossWSTest extends TestCase
{
   protected Logger log = Logger.getLogger(getClass().getName());
   private JBossWSTestHelper delegate = new JBossWSTestHelper();

   public JBossWSTest()
   {
   }

   public JBossWSTest(String name)
   {
      super(name);
   }
   
   /**
    * Execute <b>command</b> in separate process.
    * @param command command to execute
    * @throws IOException if I/O error occurs
    */
   public void executeCommand(String command) throws IOException
   {
      this.executeCommand(command, null, null, null);
   }
   
   /**
    * Execute <b>command</b> in separate process. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param message message to display if assertion fails
    * @throws IOException if I/O error occurs
    */
   public void executeCommand(String command, String message) throws IOException
   {
      this.executeCommand(command, null, message, null);
   }
   
   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @throws IOException if I/O error occurs
    */
   public void executeCommand(String command, OutputStream os) throws IOException
   {
      this.executeCommand(command, os, null, null);
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @param message message to display if assertion fails
    * @throws IOException if I/O error occurs
    */
   public void executeCommand(String command, OutputStream os, String message) throws IOException
   {
      this.executeCommand(command, os, message, null);
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @param message message to display if assertion fails
    * @param env environment
    * @throws IOException if I/O error occurs
    */
   public void executeCommand(String command, OutputStream os, String message, Map<String, String> env) throws IOException
   {
      if (command == null)
         throw new NullPointerException( "Command cannot be null" );
      
      System.out.println("Executing command: " + command);
      log.debug("Executing command: " + command);
      
      StringTokenizer st = new StringTokenizer(command, " \t\r");
      List<String> tokenizedCommand = new LinkedList<String>();
      while (st.hasMoreTokens())
      {
         // PRECONDITION: command doesn't contain whitespaces in the paths
         tokenizedCommand.add(st.nextToken());
      }
      
      try
      {
         this.executeCommand(tokenizedCommand, os, message, env);
      }
      catch (IOException e)
      {
         log.warn("Make sure there are no whitespaces in command paths", e);
         throw e;
      }
   }

   /**
    * Execute <b>command</b> in separate process, copy process input to <b>os</b>. If process will fail, display custom <b>message</b> in assertion.
    * @param command command to execute
    * @param os output stream to copy process input to. If null, <b>System.out</b> will be used
    * @param message message to display if assertion fails
    * @param env environment
    * @throws IOException if I/O error occurs
    */
   private void executeCommand(List<String> command, OutputStream os, String message, Map<String, String> env) throws IOException
   {
      ProcessBuilder pb = new ProcessBuilder(command);
      if (env != null)
      {
         for (String variable : env.keySet())
         {
            pb.environment().put(variable, env.get(variable));
         }
      }
      Process p = pb.start();
      CopyJob inputStreamJob = new CopyJob(p.getInputStream(), os == null ? System.out : os);
      CopyJob errorStreamJob = new CopyJob(p.getErrorStream(), System.err);
      // unfortunately the following threads are needed because of Windows behavior
      System.out.println("Process input stream:");
      System.err.println("Process error stream:");
      Thread inputJob = new Thread(inputStreamJob);
      Thread outputJob = new Thread(errorStreamJob);
      try
      {  
         inputJob.start();
         inputJob.join(5000);
         outputJob.start();
         outputJob.join(5000);
         int statusCode = p.waitFor();
         String fallbackMessage = "Process did exit with status " + statusCode; 
         assertTrue(message != null ? message : fallbackMessage, statusCode == 0);
      }
      catch (InterruptedException ie)
      {
         ie.printStackTrace(System.err);
      }
      finally
      {
         inputStreamJob.kill();
         errorStreamJob.kill();
         p.destroy();
      }
   }

   public MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }

   public boolean isTargetJBoss5()
   {
      return delegate.isTargetJBoss5();
   }
   
   public boolean isTargetJBoss51()
   {
      return delegate.isTargetJBoss51();
   }

   public boolean isTargetJBoss50()
   {
      return delegate.isTargetJBoss50();
   }

   public boolean isTargetJBoss6()
   {
      return delegate.isTargetJBoss6();
   }
   
   public boolean isTargetJBoss61()
   {
      return delegate.isTargetJBoss61();
   }

   public boolean isTargetJBoss60()
   {
      return delegate.isTargetJBoss60();
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
