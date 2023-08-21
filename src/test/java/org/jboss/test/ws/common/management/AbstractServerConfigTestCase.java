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
         public Integer getVirtualHostPort(String virtualHost, boolean secure)
         {
            return null;
         }

         public String getHostAlias(String host)
         {
            return host;
         }
      };
      
      //test default value
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());
      
      //test empty/null values
      conf.setWebServiceHost(null);
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());
      conf.setWebServiceHost("");
      assertEquals(ServerConfig.UNDEFINED_HOSTNAME, conf.getWebServiceHost());

      String tHost = InetAddress.getLocalHost().getHostAddress();
      String expectedResult = "127.0.0.1".equals(tHost) ? "localhost" : tHost;
      //test 0.0.0.0
      conf.setWebServiceHost("0.0.0.0");
      assertEquals(expectedResult, conf.getWebServiceHost());
      
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
