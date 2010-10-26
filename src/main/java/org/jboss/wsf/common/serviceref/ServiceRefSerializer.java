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
package org.jboss.wsf.common.serviceref;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.NamingException;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * Provides utility methods and constants for web service reference de/serialization.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class ServiceRefSerializer
{
   public static final String SERVICE_REF_META_DATA = "SERVICE_REF_META_DATA";

   private ServiceRefSerializer()
   {
      // forbidden constructor
   }

   public static byte[] marshall(final UnifiedServiceRefMetaData obj) throws NamingException
   {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);

      try
      {
         final ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(obj);
         oos.close();
      }
      catch (final IOException e)
      {
         throw new NamingException("Cannot marshall service ref meta data, cause: " + e.toString());
      }

      return baos.toByteArray();
   }

   public static UnifiedServiceRefMetaData unmarshall(final byte[] data) throws NamingException
   {
      final UnifiedServiceRefMetaData sref;

      try
      {
         final ByteArrayInputStream bais = new ByteArrayInputStream(data);
         final ObjectInputStream ois = new ObjectInputStream(bais);
         sref = (UnifiedServiceRefMetaData) ois.readObject();
         ois.close();
      }
      catch (final IOException e)
      {
         throw new NamingException("Cannot unmarshall service ref meta data, cause: " + e.toString());
      }
      catch (final ClassNotFoundException e)
      {
         throw new NamingException("Cannot unmarshall service ref meta data, cause: " + e.toString());
      }

      return sref;
   }
}
