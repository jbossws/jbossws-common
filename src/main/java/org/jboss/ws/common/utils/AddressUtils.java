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
package org.jboss.ws.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * IP address utils
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Oct-2013
 */
public final class AddressUtils
{
   private static final Pattern VALID_IPV4_PATTERN;
   private static final Pattern VALID_IPV6_PATTERN;
   private static final Pattern VALID_BASIC_HOSTNAME_PATTERN;
   private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
   private static final String ipv6Pattern = "^([\\dA-F]{1,4}:|((?=.*(::))(?!.*\\3.+\\3))\\3?)([\\dA-F]{1,4}(\\3|:\\b)|\\2){5}(([\\dA-F]{1,4}(\\3|:\\b|$)|\\2){2}|(((2[0-4]|1\\d|[1-9])?\\d|25[0-5])\\.?\\b){4})\\z";
   private static final String basicHostnamePattern = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
   static
   {
      try
      {
         VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
         VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
         VALID_BASIC_HOSTNAME_PATTERN = Pattern.compile(basicHostnamePattern, Pattern.CASE_INSENSITIVE);
      }
      catch (PatternSyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static boolean isValidIPv4Address(final String ipAddress)
   {
      Matcher m = VALID_IPV4_PATTERN.matcher(ipAddress);
      return m.matches();
   }
   
   public static boolean isValidIPv6Address(final String ipAddress)
   {
      Matcher m = VALID_IPV6_PATTERN.matcher(ipAddress);
      return m.matches();
   }
   
   public static boolean isValidHostnameAddress(final String hostname)
   {
      Matcher m = VALID_BASIC_HOSTNAME_PATTERN.matcher(hostname);
      return m.matches() ? hostname.length() <= 255 : false;
   }
   
   public static boolean isValidIPAddress(final String ipAddress)
   {
      return isValidIPv4Address(ipAddress) || isValidIPv6Address(ipAddress);
   }
   
   public static boolean isValidAddress(final String address)
   {
      return isValidIPAddress(address) || isValidHostnameAddress(address);
   }
}
