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

import java.security.SecureRandom;

/**
 * Generates the string form of IETF variant UUIDs.
 *  
 * See <a href="http://www.ietf.org/internet-drafts/draft-mealling-uuid-urn-05.txt">
 * the latest IETF draft</a> for more information about UUID generation.
 * 
 * Currently only pseudo random (type 4) UUIDs are supported.
 * 
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a> 
 */
public class UUIDGenerator
{
   private static SecureRandom rand;
   
   private static String bytesToHex(byte[] buffer, int offset, int length) 
   {
      long value = 0;
      for (int i = 0, countDown = 8 * length; i < length; i++)
      {
         value |= (buffer[offset + i] & 0xffL) << (countDown -= 8);
      }
      
      return Long.toHexString(value);
   }
   
   /**
    * Generates a pseudo random UUID and returns it in byte array form.
    * 
    * @return a UUID byte array in network order
    */
   public static byte[] generateRandomUUIDBytes() 
   {
      if (rand == null)
         rand = new SecureRandom();
      
      byte[] buffer = new byte[16];
      rand.nextBytes(buffer);

      // Set version to 3 (Random)
      buffer[6] = (byte) ((buffer[6] & 0x0f) | 0x40);
      // Set variant to 2 (IETF)
      buffer[8] = (byte) ((buffer[8] & 0x3f) | 0x80);
      
      return buffer;
   }
   
   /**
    * Generates a pseudo random UUID and returns it the IETF specified
    * String form. See {@link #convertToString(byte[])} for a description
    * of the format.
    * 
    * @return a UUID in IETF string form.
    */
   public static String generateRandomUUIDString() 
   {
      return convertToString(generateRandomUUIDBytes());
   }
   
   /**
    * Converts a UUID in byte array form to the IETF string format.
    * 
    * <p>The BNF follows:
    * <pre>
    *  UUID                   = <time_low> "-" <time_mid> "-"
    *                           <time_high_and_version> "-"
    *                           <variant_and_sequence> "-"
    *                           <node>
    *  time_low               = 4*<hexOctet>
    *  time_mid               = 2*<hexOctet>
    *  time_high_and_version  = 2*<hexOctet>
    *  variant_and_sequence   = 2*<hexOctet>
    *  node                   = 6*<hexOctet>
    *  hexOctet               = <hexDigit><hexDigit>
    *  hexDigit               =
    *        "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
    *        | "a" | "b" | "c" | "d" | "e" | "f"
    *        | "A" | "B" | "C" | "D" | "E" | "F"
    * </pre>
    * 
    * @param uuid a 16 byte 
    * @return the IETF string form of the passed UUID
    */
   public static String convertToString(byte[] uuid) 
   {
      if (uuid.length != 16)
         throw new IllegalArgumentException("A UUID must be 16 bytes!");
      
      String string = bytesToHex(uuid, 0, 4) + "-" 
                    + bytesToHex(uuid, 4, 2) + "-" 
                    + bytesToHex(uuid, 6, 2) + "-"
                    + bytesToHex(uuid, 8, 2) + "-"
                    + bytesToHex(uuid, 10, 6);

      return string;
   }
}
