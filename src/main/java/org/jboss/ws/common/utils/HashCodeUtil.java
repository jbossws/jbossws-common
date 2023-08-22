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

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 *
 * Example use case:
 * <pre>
 *  public int hashCode(){
 *    int result = HashCodeUtil.SEED;
 *    //collect the contributions of various fields
 *    result = HashCodeUtil.hash(result, fPrimitive);
 *    result = HashCodeUtil.hash(result, fObject);
 *    result = HashCodeUtil.hash(result, fArray);
 *    return result;
 *  }
 * </pre>
 */
public final class HashCodeUtil
{

   /**
    * An initial value for a <code>hashCode</code>, to which is added contributions
    * from fields. Using a non-zero value decreases collisons of <code>hashCode</code>
    * values.
    */
   public static final int SEED = 23;

   /**
    * booleans.
    */
   public static int hash(int aSeed, boolean aBoolean)
   {
      return org.jboss.ws.common.utils.HashCodeUtil.firstTerm(aSeed) + (aBoolean ? 1 : 0);
   }

   /**
    * chars.
    */
   public static int hash(int aSeed, char aChar)
   {
      return org.jboss.ws.common.utils.HashCodeUtil.firstTerm(aSeed) + (int)aChar;
   }

   /**
    * ints.
    */
   public static int hash(int aSeed, int aInt)
   {
      /*
       * Implementation Note
       * Note that byte and short are handled by this method, through
       * implicit conversion.
       */
      return org.jboss.ws.common.utils.HashCodeUtil.firstTerm(aSeed) + aInt;
   }

   /**
    * longs.
    */
   public static int hash(int aSeed, long aLong)
   {
      return org.jboss.ws.common.utils.HashCodeUtil.firstTerm(aSeed) + (int)(aLong ^ (aLong >>> 32));
   }

   /**
    * floats.
    */
   public static int hash(int aSeed, float aFloat)
   {
      return org.jboss.ws.common.utils.HashCodeUtil.hash(aSeed, Float.floatToIntBits(aFloat));
   }

   /**
    * doubles.
    */
   public static int hash(int aSeed, double aDouble)
   {
      return org.jboss.ws.common.utils.HashCodeUtil.hash(aSeed, Double.doubleToLongBits(aDouble));
   }

   /**
    * <code>aObject</code> is a possibly-null object field, and possibly an array.
    *
    * If <code>aObject</code> is an array, then each element may be a primitive
    * or a possibly-null object.
    */
   public static int hash(int aSeed, Object aObject)
   {
      int result = aSeed;
      if (aObject == null)
      {
         result = org.jboss.ws.common.utils.HashCodeUtil.hash(result, 0);
      }
      else if (!org.jboss.ws.common.utils.HashCodeUtil.isArray(aObject))
      {
         result = org.jboss.ws.common.utils.HashCodeUtil.hash(result, aObject.hashCode());
      }
      else
      {
         int length = Array.getLength(aObject);
         for (int idx = 0; idx < length; ++idx)
         {
            Object item = Array.get(aObject, idx);
            //recursive call!
            result = org.jboss.ws.common.utils.HashCodeUtil.hash(result, item);
         }
      }
      return result;
   }

   /// PRIVATE ///
   private static final int fODD_PRIME_NUMBER = 37;

   private static int firstTerm(int aSeed)
   {
      return org.jboss.ws.common.utils.HashCodeUtil.fODD_PRIME_NUMBER * aSeed;
   }

   private static boolean isArray(Object aObject)
   {
      return aObject.getClass().isArray();
   }
}
