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
package org.jboss.test.ws.common;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.ws.common.URLLoaderAdapter;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * Test the URLLoaderAdapter
 *
 * @author alessio.soldano@jboss.org
 * @since 30-Oct-2008
 */
public class URLLoaderAdapterTestCase extends TestCase
{
   public void testWithJar() throws Exception
   {
      //Getting the SPI jar url
      ClassLoader cl = UnifiedVirtualFile.class.getClassLoader();
      URL rootURL = getJarUrl(cl.getResource("org/jboss/wsf/spi/deployment"));
      assertNotNull(rootURL);
      URLLoaderAdapter ula = new URLLoaderAdapter(rootURL);
      
      UnifiedVirtualFile deployment = ula.findChild("org/jboss/wsf/spi/deployment/");
      assertNotNull(deployment);
      assertTrue(deployment.toURL().toExternalForm().contains("jar!")); //check we got a URL to a jar
      assertEquals("deployment/", deployment.getName());
      List<UnifiedVirtualFile> children = deployment.getChildren();
      assertNotNull(children);
      assertTrue(children.size() > 0);
      UnifiedVirtualFile unifiedVirtualFile = null;
      for (UnifiedVirtualFile uvf : children)
      {
         if (uvf.getName().equals(UnifiedVirtualFile.class.getSimpleName() + ".class"))
            unifiedVirtualFile = uvf;
      }
      assertNotNull(unifiedVirtualFile);
      assertTrue(unifiedVirtualFile.getChildren().size() == 0);
   }
   
   public void testWithDir() throws Exception
   {
      ClassLoader cl = UnifiedVirtualFile.class.getClassLoader();
      URL rootURL = cl.getResource("org/jboss/ws/common/");
      assertNotNull(rootURL);
      URLLoaderAdapter ula = new URLLoaderAdapter(rootURL);
      
      UnifiedVirtualFile common = ula.findChild("org/jboss/ws/common/");
      assertNotNull(common);
      assertTrue(common.toURL().toExternalForm().contains("target/classes")); //check we got a URL to dir
      assertEquals("common/", common.getName());
      List<UnifiedVirtualFile> children = common.getChildren();
      assertNotNull(children);
      assertTrue(children.size() > 0);
      UnifiedVirtualFile urlLoaderAdapter = null;
      UnifiedVirtualFile utils = null;
      for (UnifiedVirtualFile uvf : children)
      {
         if (uvf.getName().equals(URLLoaderAdapter.class.getSimpleName() + ".class"))
            urlLoaderAdapter = uvf;
         else if (uvf.getName().equals("utils/"))
            utils = uvf;
      }
      assertNotNull(urlLoaderAdapter);
      assertNotNull(utils);
      assertTrue(urlLoaderAdapter.getChildren().size() == 0);
   }
   
   public void testFailSafeGetChild() throws Exception
   {
      ClassLoader cl = UnifiedVirtualFile.class.getClassLoader();
      URL rootURL = getJarUrl(cl.getResource("org/jboss/wsf/spi/deployment"));
      assertNotNull(rootURL);
      URLLoaderAdapter ula = new URLLoaderAdapter(rootURL);
      try {
         ula.findChild("foo/bar/");
         fail("IOException expected");
      } catch (IOException e) {
         //expected
      }
      try {
         UnifiedVirtualFile uvf = ula.findChildFailSafe("foo/bar/");
         assertNull(uvf);
      } catch (Exception e) {
         fail("Exception not expected, 'null' should have been returned instead: " + e.getMessage());
      }
   }
   
   private static URL getJarUrl(URL url) throws Exception
   {
      String urlString = url.toExternalForm();
      String jarRoot = urlString.substring(4, urlString.indexOf("ar!") + 2);
      return new URL(jarRoot);
   }
}
