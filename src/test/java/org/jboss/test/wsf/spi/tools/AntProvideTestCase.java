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
package org.jboss.test.wsf.spi.tools;

/**
 * @author Heiko.Braun@jboss.com
 */
public class AntProvideTestCase extends BuildFileTest
{
   protected void setUp() throws Exception
   {
      super.setUp();

      // cleanup events
      CmdProvideTracker.LAST_EVENT = "";

      // enforce loading of the tracker implemenation
      System.setProperty("org.jboss.wsf.spi.tools.ProviderFactoryImpl", "org.jboss.test.wsf.spi.tools.CmdProvideTrackerFactory");

      configureProject("src/test/resources/smoke/tools/provide-test.xml");
   }

   public void testPlainInvocation()
   {
      executeTarget("plainInvocation");
      assertTrue("provide() not invoked", CmdProvideTracker.LAST_EVENT.indexOf("provide") != -1);
   }

   public void testIncludeWSDL()
   {
      executeTarget("includeWSDL");
      assertTrue("setGenerateWsdl() not invoked", CmdProvideTracker.LAST_EVENT.indexOf("setGenerateWsdl") != -1);
   }

   public void testExtraClasspath()
   {
      executeTarget("extraClasspath");

   }

}
