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
package org.jboss.test.ws.common;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.ws.common.ResourceLoaderAdapter;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * Test the ResourceLoaderAdapter
 *
 * @author alessio.soldano@jboss.org
 * @since 30-Oct-2008
 */
public class ResourceLoaderAdapterTestCase extends TestCase
{
   public void testWithJar() throws Exception
   {
      //Getting the classloader for UnifiedVirtualFile which lives in SPI -> external jar (from the maven repo)
      ClassLoader cl = UnifiedVirtualFile.class.getClassLoader();
      ResourceLoaderAdapter ula = new ResourceLoaderAdapter(cl);
      
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
      //Getting the classloader for ResourceLoaderAdapter which lives in COMMON -> target/classes in this project
      ClassLoader cl = ResourceLoaderAdapter.class.getClassLoader();
      ResourceLoaderAdapter ula = new ResourceLoaderAdapter(cl);
      
      UnifiedVirtualFile common = ula.findChild("org/jboss/ws/common/");
      assertNotNull(common);
      assertTrue(common.toURL().toExternalForm().contains("target/classes")); //check we got a URL to dir
      assertEquals("common/", common.getName());
      List<UnifiedVirtualFile> children = common.getChildren();
      assertNotNull(children);
      assertTrue(children.size() > 0);
      UnifiedVirtualFile resourceLoaderAdapter = null;
      UnifiedVirtualFile utils = null;
      for (UnifiedVirtualFile uvf : children)
      {
         if (uvf.getName().equals(ResourceLoaderAdapter.class.getSimpleName() + ".class"))
            resourceLoaderAdapter = uvf;
         else if (uvf.getName().equals("utils/"))
            utils = uvf;
      }
      assertNotNull(resourceLoaderAdapter);
      assertNotNull(utils);
      assertTrue(resourceLoaderAdapter.getChildren().size() == 0);
   }
   
   public void testFailSafeGetChild()
   {
      ClassLoader cl = UnifiedVirtualFile.class.getClassLoader();
      ResourceLoaderAdapter ula = new ResourceLoaderAdapter(cl);
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
}
