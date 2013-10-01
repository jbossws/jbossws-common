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
package org.jboss.test.ws.common.utils;

import junit.framework.TestCase;

import org.jboss.ws.common.utils.AddressUtils;

/**
 * Test the AddressUtils
 *
 * @author alessio.soldano@jboss.com
 * @since 01-Oct-2013
 */
public class AddressUtilsTestCase extends TestCase
{
   public void testHostname() {
      assertFalse(AddressUtils.isValidHostnameAddress("."));
      assertFalse(AddressUtils.isValidHostnameAddress("-"));
      assertTrue(AddressUtils.isValidHostnameAddress("ee.54"));
      assertTrue(AddressUtils.isValidHostnameAddress("r4-a.6y"));
      assertTrue(AddressUtils.isValidHostnameAddress("r4-a.6y"));
      assertFalse(AddressUtils.isValidHostnameAddress("-aa.com"));
      assertFalse(AddressUtils.isValidHostnameAddress("aa.-com"));
      assertFalse(AddressUtils.isValidHostnameAddress("aa-.com"));
      assertFalse(AddressUtils.isValidHostnameAddress("-.com"));
      assertFalse(AddressUtils.isValidHostnameAddress("--.com"));
      assertFalse(AddressUtils.isValidHostnameAddress("-.com"));
      assertFalse(AddressUtils.isValidHostnameAddress("aa.--"));
      assertFalse(AddressUtils.isValidHostnameAddress("aa.-"));
      assertTrue(AddressUtils.isValidHostnameAddress("a-a.com"));
      assertTrue(AddressUtils.isValidHostnameAddress("a-a.co-m"));
      assertTrue(AddressUtils.isValidHostnameAddress("a--a.com"));
      assertTrue(AddressUtils.isValidHostnameAddress("a-a.com.fs"));
      assertTrue(AddressUtils.isValidHostnameAddress("a-a.co-m.it"));
      assertFalse(AddressUtils.isValidHostnameAddress("fsd~fd"));
      assertFalse(AddressUtils.isValidHostnameAddress("fsd%fd"));
      assertFalse(AddressUtils.isValidHostnameAddress("fsd:fd"));
      assertFalse(AddressUtils.isValidHostnameAddress("fsd/fd"));
      assertFalse(AddressUtils.isValidHostnameAddress("fsd_fd"));
   }
   
   public void testIPv4Addresses() {
      assertTrue(AddressUtils.isValidIPv4Address("192.168.3.45"));
      assertTrue(AddressUtils.isValidIPv4Address("127.0.0.1"));
      assertTrue(AddressUtils.isValidIPv4Address("10.0.0.1"));
      assertTrue(AddressUtils.isValidIPv4Address("4.3.2.5"));
      assertFalse(AddressUtils.isValidIPv4Address("192.256.3.45"));
      assertFalse(AddressUtils.isValidIPv4Address("192.3.256.45"));
      assertFalse(AddressUtils.isValidIPv4Address("256.192.3.45"));
      assertFalse(AddressUtils.isValidIPv4Address("192.3.45.256"));
      assertFalse(AddressUtils.isValidIPv4Address("192.34.5"));
      assertFalse(AddressUtils.isValidIPv4Address("154.14.5."));
      assertFalse(AddressUtils.isValidIPv4Address("154.65"));
      assertFalse(AddressUtils.isValidIPv4Address("154.65"));
      assertFalse(AddressUtils.isValidIPv4Address("154"));
      assertFalse(AddressUtils.isValidIPv4Address("154."));
   }
   
   public void testIPv6Addresses() {
      assertTrue(AddressUtils.isValidIPv6Address("FEDC:BA98:7654:3210:FEDC:BA98:7654:3210"));
      assertTrue(AddressUtils.isValidIPv6Address("3ffe:2a00:100:7031::1"));
      assertTrue(AddressUtils.isValidIPv6Address("::FFFF:129.144.52.38"));
      assertTrue(AddressUtils.isValidIPv6Address("0:0:0:0:0:0:13.1.68.3"));
      assertTrue(AddressUtils.isValidIPv6Address("0:0:0:0:0:0:0:1"));
      assertTrue(AddressUtils.isValidIPv6Address("::1"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::"));
      assertTrue(AddressUtils.isValidIPv6Address("::"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::3333:4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::3333:4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:6666:7777:8888"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("::123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("::6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222:3333::5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("::5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111:2222::4444:5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::4444:5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("::4444:5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("1111::3333:4444:5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("::3333:4444:5555:6666:123.123.123.123"));
      assertTrue(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:6666:123.123.123.123"));
      
      // Invalid data
      assertFalse(AddressUtils.isValidIPv6Address("XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX"));
      // To much components
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:8888:9999"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:8888::"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:6666:7777:8888:9999"));
      // To less components
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222"));
      assertFalse(AddressUtils.isValidIPv6Address("1111"));
      // Missing ":"
      assertFalse(AddressUtils.isValidIPv6Address("11112222:3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:22223333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:33334444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:44445555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:55556666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:66667777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:77778888"));
      // Missing ":" intended for "::"
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:"));
      assertFalse(AddressUtils.isValidIPv6Address(":"));
      assertFalse(AddressUtils.isValidIPv6Address(":8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":2222:3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555:6666:7777:8888"));
      // :::
      assertFalse(AddressUtils.isValidIPv6Address(":::2222:3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:::3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:::4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:::8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:::"));
      // Double "::"
      assertFalse(AddressUtils.isValidIPv6Address("::2222::4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:7777::8888"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:7777:8888::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444:5555::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444:5555:6666::8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444:5555:6666:7777::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444:5555::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444:5555:6666::8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444:5555:6666:7777::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::5555::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::5555:6666::8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::5555:6666:7777::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::6666::8888"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::6666:7777::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::7777::"));
      // Invalid data
      assertFalse(AddressUtils.isValidIPv6Address("XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:00.00.00.00"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:000.000.000.000"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:256.256.256.256"));
      // To much components
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:8888:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:6666:7777:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:1.2.3.4.5"));
      // To less components
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1.2.3.4"));
      // Missing ":"
      assertFalse(AddressUtils.isValidIPv6Address("11112222:3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:22223333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:33334444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:44445555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:55556666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:66661.2.3.4"));
      // Missing "."
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:255255.255.255"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:255.255255.255"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:255.255.255255"));
      // Missing ":" intended for "::"
      assertFalse(AddressUtils.isValidIPv6Address(":1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":2222:3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555:6666:1.2.3.4"));
      // ":::"
      assertFalse(AddressUtils.isValidIPv6Address(":::2222:3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:::3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:::4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:::1.2.3.4"));
      // Double "::"
      assertFalse(AddressUtils.isValidIPv6Address("::2222::4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444:5555::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444:5555::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::5555::1.2.3.4"));
      // Missing parts
      assertFalse(AddressUtils.isValidIPv6Address("::."));
      assertFalse(AddressUtils.isValidIPv6Address("::.."));
      assertFalse(AddressUtils.isValidIPv6Address("::..."));
      assertFalse(AddressUtils.isValidIPv6Address("::1..."));
      assertFalse(AddressUtils.isValidIPv6Address("::1.2.."));
      assertFalse(AddressUtils.isValidIPv6Address("::1.2.3."));
      assertFalse(AddressUtils.isValidIPv6Address("::.2.."));
      assertFalse(AddressUtils.isValidIPv6Address("::.2.3."));
      assertFalse(AddressUtils.isValidIPv6Address("::.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("::..3."));
      assertFalse(AddressUtils.isValidIPv6Address("::..3.4"));
      assertFalse(AddressUtils.isValidIPv6Address("::...4"));
      // Extra ":" in front
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555:6666:7777::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555:6666::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::"));
      assertFalse(AddressUtils.isValidIPv6Address(":::"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555:6666::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":::2222:3333:4444:5555:6666:7777:8888"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444:5555::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":::1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333:4444::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":::6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222:3333::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":::5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111:2222::4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":::4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":1111::3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":::3333:4444:5555:6666:1.2.3.4"));
      assertFalse(AddressUtils.isValidIPv6Address(":::2222:3333:4444:5555:6666:1.2.3.4"));
      // Extra ":" at end
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:7777:::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666:::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:::"));
      assertFalse(AddressUtils.isValidIPv6Address(":::"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555:6666::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444:5555::7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333:4444::6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222:3333::5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111:2222::4444:5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::4444:5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::4444:5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("1111::3333:4444:5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::3333:4444:5555:6666:7777:8888:"));
      assertFalse(AddressUtils.isValidIPv6Address("::2222:3333:4444:5555:6666:7777:8888:"));
   }
   
   public void testValidAddress()
   {
      assertFalse(AddressUtils.isValidAddress("z<.trr"));
      assertFalse(AddressUtils.isValidAddress("tt533!fd"));
      assertFalse(AddressUtils.isValidAddress("fdss("));
      assertFalse(AddressUtils.isValidAddress(".."));
      assertFalse(AddressUtils.isValidAddress(" tetre"));
      assertFalse(AddressUtils.isValidAddress("78.."));
      assertFalse(AddressUtils.isValidAddress("rer..it"));
      assertFalse(AddressUtils.isValidAddress("foo_3.it"));
      assertFalse(AddressUtils.isValidAddress("65.654.45."));
   }
}
