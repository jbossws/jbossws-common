/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.common.management;

import java.io.File;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.management.ServerConfig;

/**
 * Test the AbstractServerConfig
 *
 * @author alessio.soldano@jboss.com
 * @since 01-Oct-2013
 */
public class AbstractServerConfigTestCase extends TestCase
{
   public void testSetWebServiceHost() throws Exception {
      final AbstractServerConfig conf = new AbstractServerConfig()
      {
         @Override
         public File getServerTempDir()
         {
            return null;
         }
         
         @Override
         public File getServerDataDir()
         {
            return null;
         }
         
         @Override
         public File getHomeDir()
         {
            return null;
         }
      };
      
      //test default value
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());
      
      //test empty/null values
      conf.setWebServiceHost(null);
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());
      conf.setWebServiceHost("");
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());

      //test 0.0.0.0
      conf.setWebServiceHost("0.0.0.0");
      assertEquals(InetAddress.getLocalHost().getHostName(), conf.getWebServiceHost());
      
      //test jbossws.undefined.host
      conf.setWebServiceHost(ServerConfig.UNDEFINED_HOSTNAME);
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());
      
      //test hostname
      conf.setWebServiceHost("foo.com");
      assertEquals("foo.com", conf.getWebServiceHost());
      
      //test IPv4 literal
      conf.setWebServiceHost("192.168.3.56");
      assertEquals("192.168.3.56", conf.getWebServiceHost());

      //test IPv6 literals
      conf.setWebServiceHost("0:0:0:0:0:0:0:1");
      assertEquals("[0:0:0:0:0:0:0:1]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210");
      assertEquals("[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("3ffe:2a00:100:7031::1");
      assertEquals("[3ffe:2a00:100:7031::1]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("::FFFF:129.144.52.38"); //IPv6 address with embedded IPv4 address, see RFC2373 2.5.4
      assertEquals("[::FFFF:129.144.52.38]", conf.getWebServiceHost());
      conf.setWebServiceHost("0:0:0:0:0:0:13.1.68.3");
      assertEquals("[0:0:0:0:0:0:13.1.68.3]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("::1"); //IPv6 loopback address
      assertEquals("[::1]", conf.getWebServiceHost());
      
      //test IPv6 literals already converted to URL format
      conf.setWebServiceHost("[0:0:0:0:0:0:0:1]");
      assertEquals("[0:0:0:0:0:0:0:1]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]");
      assertEquals("[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("[3ffe:2a00:100:7031::1]");
      assertEquals("[3ffe:2a00:100:7031::1]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("[::FFFF:129.144.52.38]");
      assertEquals("[::FFFF:129.144.52.38]", conf.getWebServiceHost());
      conf.setWebServiceHost("[0:0:0:0:0:0:13.1.68.3]");
      assertEquals("[0:0:0:0:0:0:13.1.68.3]", conf.getWebServiceHost());
      
      conf.setWebServiceHost("[::1]");
      assertEquals("[::1]", conf.getWebServiceHost());
      
      try {
         conf.setWebServiceHost("ff%");
         fail("IllegalArgumentException expected!");
      } catch (IllegalArgumentException e) {
         //OK
      }
      
   }
}
