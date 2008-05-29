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

// $Id$

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.MBeanServerConnection;
import javax.naming.NamingException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

/**
 * A test setup that deploys/undeploys archives
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossWSTestSetup extends TestSetup
{
   // provide logging
   private static Logger log = Logger.getLogger(JBossWSTestSetup.class);

   private JBossWSTestHelper delegate = new JBossWSTestHelper();
   private String[] archives = new String[0];
   private ClassLoader originalClassLoader;

   public JBossWSTestSetup(Class<?> testClass, String archiveList)
   {
      super(new TestSuite(testClass));
      getArchiveArray(archiveList);
   }

   public JBossWSTestSetup(Test test, String archiveList)
   {
      super(test);
      getArchiveArray(archiveList);
   }

   public JBossWSTestSetup(Test test)
   {
      super(test);
   }

   public File getArchiveFile(String archive)
   {
      return delegate.getArchiveFile(archive);
   }

   public URL getArchiveURL(String archive) throws MalformedURLException
   {
      return delegate.getArchiveFile(archive).toURL();
   }

   public File getResourceFile(String resource)
   {
      return delegate.getResourceFile(resource);
   }

   public URL getResourceURL(String resource) throws MalformedURLException
   {
      return delegate.getResourceFile(resource).toURL();
   }

   private void getArchiveArray(String archiveList)
   {
      if (archiveList != null)
      {
         StringTokenizer st = new StringTokenizer(archiveList, ", ");
         archives = new String[st.countTokens()];

         for (int i = 0; i < archives.length; i++)
            archives[i] = st.nextToken();
      }
   }

   protected void setUp() throws Exception
   {
      // verify integration target
      String integrationTarget = delegate.getIntegrationTarget();
      log.debug("Integration target: " + integrationTarget);

      List<URL> clientJars = new ArrayList<URL>();
      for (int i = 0; i < archives.length; i++)
      {
         String archive = archives[i];
         try
         {
            delegate.deploy(archive);
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
            delegate.undeploy(archive);
         }

         if (archive.endsWith("-client.jar"))
         {
            URL archiveURL = getArchiveURL(archive);
            clientJars.add(archiveURL);
         }
      }

      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      originalClassLoader = parent;
      // add client jars to the class loader
      if (!clientJars.isEmpty())
      {
         URL[] urls = new URL[clientJars.size()];
         for (int i = 0; i < clientJars.size(); i++)
         {
            urls[i] = clientJars.get(i);
         }
         URLClassLoader cl = new URLClassLoader(urls, parent);
         Thread.currentThread().setContextClassLoader(cl);
      }
   }

   protected void tearDown() throws Exception
   {
      try
      {
         for (int i = 0; i < archives.length; i++)
         {
            String archive = archives[archives.length - i - 1];
            delegate.undeploy(archive);
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(originalClassLoader);
      }
   }

   public MBeanServerConnection getServer() throws NamingException
   {
      return JBossWSTestHelper.getServer();
   }
}
