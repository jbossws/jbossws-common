/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.common;

import org.jboss.wsf.common.Normalizer;

import junit.framework.TestCase;

/**
 * [JBWS-2885] http://jira.jboss.com/jira/browse/JBWS-2885
 * 
 * Test case to test <![CDATA[ ]]> sections are skipped in the 
 * normalization process.
 * 
 * @author darran.lofthouse@jboss.com
 * @since Mar 10, 2010
 */
public class NormalizerTestCase extends TestCase
{

   private static final String CDATA_STRING = "<![CDATA[ABC]]>";

   public void testCDATAOnly()
   {
      String normalized = Normalizer.normalize(CDATA_STRING);

      assertEquals("Normalized String", CDATA_STRING, normalized);
   }

   public void testCDATABegin()
   {
      String normalized = Normalizer.normalize(CDATA_STRING + "ABC");

      assertEquals("Normalized String", CDATA_STRING + "ABC", normalized);
   }

   public void testCDATAEnd()
   {
      String normalized = Normalizer.normalize("ABD" + CDATA_STRING);

      assertEquals("Normalized String", "ABD" + CDATA_STRING, normalized);
   }

   public void testCDATAMid()
   {
      String normalized = Normalizer.normalize("ABD" + CDATA_STRING + "EFG");

      assertEquals("Normalized String", "ABD" + CDATA_STRING + "EFG", normalized);
   }

   public void testCDATADouble()
   {
      String normalized = Normalizer.normalize("ABD" + CDATA_STRING + "EFG" + CDATA_STRING + "HIJ");

      assertEquals("Normalized String", "ABD" + CDATA_STRING + "EFG" + CDATA_STRING + "HIJ", normalized);
   }
   
   public void testCDATABegin_Replace()
   {
      String normalized = Normalizer.normalize(CDATA_STRING + "<>");

      assertEquals("Normalized String", CDATA_STRING + "&lt;&gt;", normalized);
   }

   public void testCDATAEnd_Replace()
   {
      String normalized = Normalizer.normalize("<>" + CDATA_STRING);

      assertEquals("Normalized String", "&lt;&gt;" + CDATA_STRING, normalized);
   }

   public void testCDATAMid_Replace()
   {
      String normalized = Normalizer.normalize("<>" + CDATA_STRING + "<>");

      assertEquals("Normalized String", "&lt;&gt;" + CDATA_STRING + "&lt;&gt;", normalized);
   }

   public void testCDATADouble_Replace()
   {
      String normalized = Normalizer.normalize("<>" + CDATA_STRING + "<>" + CDATA_STRING + "<>");

      assertEquals("Normalized String", "&lt;&gt;" + CDATA_STRING + "&lt;&gt;" + CDATA_STRING + "&lt;&gt;", normalized);
   }
}
