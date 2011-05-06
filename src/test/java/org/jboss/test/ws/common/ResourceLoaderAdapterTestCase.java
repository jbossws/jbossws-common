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
      UnifiedVirtualFile integration = null;
      for (UnifiedVirtualFile uvf : children)
      {
         if (uvf.getName().equals(UnifiedVirtualFile.class.getSimpleName() + ".class"))
            unifiedVirtualFile = uvf;
         else if (uvf.getName().equals("integration/"))
            integration = uvf;
      }
      assertNotNull(unifiedVirtualFile);
      assertNotNull(integration);
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
}
