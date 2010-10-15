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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * A JNDI reference to a javax.xml.ws.Service metadata.
 * 
 * It holds all the information necessary to reconstruct the javax.xml.ws.Service
 * instances when client does a JNDI lookup.
 *
 * @param <T> JNDI object factory type
 * 
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractServiceReferenceableJAXWS<T extends ObjectFactory> implements Referenceable
{
   public static final String SERVICE_REF_META_DATA = "SERVICE_REF_META_DATA";

   public static final String SERVICE_IMPL_CLASS = "SERVICE_CLASS_NAME";

   public static final String TARGET_CLASS_NAME = "TARGET_CLASS_NAME";

   private final String serviceImplClass;

   private final String targetClassName;

   private final UnifiedServiceRefMetaData serviceRef;

   public AbstractServiceReferenceableJAXWS(final String serviceImplClass, final String targetClassName,
         final UnifiedServiceRefMetaData serviceRef)
   {
      this.serviceImplClass = serviceImplClass;
      this.targetClassName = targetClassName;
      this.serviceRef = serviceRef;
   }

   public Reference getReference() throws NamingException
   {
      final Reference myRef = new Reference(getClass().getName(), this.getObjectFactory().getName(), null);

      myRef.add(new StringRefAddr(SERVICE_IMPL_CLASS, this.serviceImplClass));
      myRef.add(new StringRefAddr(TARGET_CLASS_NAME, this.targetClassName));
      myRef.add(new BinaryRefAddr(SERVICE_REF_META_DATA, this.marshall(this.serviceRef)));

      return myRef;
   }

   protected abstract Class<T> getObjectFactory();

   private byte[] marshall(final Object obj) throws NamingException
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
         throw new NamingException("Cannot marshall object, cause: " + e.toString());
      }

      return baos.toByteArray();
   }

   public String toString()
   {
      final StringBuilder sb = new StringBuilder();

      sb.append("\n").append(getClass().getName());
      sb.append("\n serviceImplClass=" + serviceImplClass);
      sb.append("\n targetClassName=" + targetClassName);
      sb.append("\n serviceRef=" + this.serviceRef);

      return sb.toString();
   }
}
