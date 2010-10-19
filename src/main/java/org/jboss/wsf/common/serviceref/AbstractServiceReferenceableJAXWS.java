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
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractServiceReferenceableJAXWS<T extends ObjectFactory> implements Referenceable
{
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

   public final Reference getReference() throws NamingException
   {
      final Reference myRef = new Reference(getClass().getName(), this.getObjectFactory().getName(), null);

      myRef.add(new StringRefAddr(ServiceRefSerializer.SERVICE_IMPL_CLASS, this.serviceImplClass));
      myRef.add(new StringRefAddr(ServiceRefSerializer.TARGET_CLASS_NAME, this.targetClassName));
      myRef.add(new BinaryRefAddr(ServiceRefSerializer.SERVICE_REF_META_DATA, ServiceRefSerializer
            .marshall(this.serviceRef)));

      return myRef;
   }

   /**
    * Template method for providing stack specific JNDI object factory.
    *
    * @return JNDI object factory
    */
   protected abstract Class<T> getObjectFactory();

   public final String toString()
   {
      final StringBuilder sb = new StringBuilder();

      sb.append("\n").append(getClass().getName());
      sb.append("\n serviceImplClass=" + serviceImplClass);
      sb.append("\n targetClassName=" + targetClassName);
      sb.append("\n serviceRef=" + this.serviceRef);

      return sb.toString();
   }
}
