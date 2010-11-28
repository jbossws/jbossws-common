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

import org.jboss.wsf.spi.tools.cmd.WSConsume;

/**
 * Test the command line interface to WSConsume.
 *
 * @author Heiko.Braun@jboss.com
 */
public class CmdConsumeTestCase extends CommandlineTestBase
{

   protected void setUp() throws Exception
   {
      super.setUp();

      // cleanup events
      CmdConsumeTracker.LAST_EVENT = "";
      
      // enforce loading of the tracker implemenation
      System.setProperty(
        "org.jboss.wsf.spi.tools.ConsumerFactoryImpl",
        "org.jboss.test.wsf.spi.tools.CmdConsumeTrackerFactory"
        );
   }

   public void testInvalidBindingOption() throws Exception
   {
      executeCmd("-b", true);
   }

   public void testValidBindingOption() throws Exception
   {
      executeCmd("-b binding-file.xml Service.wsdl", false);
      assertTrue("setBindingFiles() not invoked",  CmdConsumeTracker.LAST_EVENT.indexOf("setBindingFiles")!=-1);
   }

   public void testMissingOptions() throws Exception
   {
      executeCmd(null, true);
   }

   // TODO: add arbitrary combinations on a case by case basis


   void runDelegate(String[] args) throws Exception
   {
      WSConsume.main(args);
   }
}
