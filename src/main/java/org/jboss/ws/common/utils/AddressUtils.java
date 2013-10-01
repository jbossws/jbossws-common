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
