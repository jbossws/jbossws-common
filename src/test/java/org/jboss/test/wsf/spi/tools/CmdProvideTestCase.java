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

import org.jboss.wsf.spi.tools.cmd.WSProvide;

/**
 * @author Heiko.Braun@jboss.com
 */
public class CmdProvideTestCase extends CommandlineTestBase
{

   protected void setUp() throws Exception
   {
      super.setUp();

      // clear events
      CmdProvideTracker.LAST_EVENT = "";

      // enforce loading of the tracker implemenation
      System.setProperty(
        "org.jboss.wsf.spi.tools.ProviderFactoryImpl",
        "org.jboss.test.wsf.spi.tools.CmdProvideTrackerFactory"
      );
   }

   /** <pre>
 *  usage: WSProvideTask [options] &lt;endpoint class name&gt;
 *  options:
 *  -h, --help                  Show this help message
 *  -k, --keep                  Keep/Generate Java source
 *  -w, --wsdl                  Enable WSDL file generation
 *  -c, --classpath=&lt;path&lt;      The classpath that contains the endpoint
 *  -o, --output=&lt;directory&gt;    The directory to put generated artifacts
 *  -r, --resource=&lt;directory&gt;  The directory to put resource artifacts
 *  -s, --source=&lt;directory&gt;    The directory to put Java source
 *  -q, --quiet                 Be somewhat more quiet
 *  -t, --show-traces           Show full exception stack traces
 *  -l, --load-provider           Load the provider and exit (debug utility)
 * </pre>
    * */

   public void testMissingOptions() throws Exception
   {
      executeCmd(null, true);   
   }

   public void testValidOutputDir() throws Exception
   {
      executeCmd("-o outputDir org.jboss.test.wsf.spi.tools.CalculatorBean", false);
      assertTrue("setOutputDirectory() not invoked", CmdProvideTracker.LAST_EVENT.indexOf("setOutputDirectory")!=-1);
   }

   void runDelegate(String[] args) throws Exception
   {
      WSProvide.main(args);
   }
}
