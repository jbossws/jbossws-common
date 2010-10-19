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

import javax.naming.Referenceable;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * Binds a JAXRPC service object factory to the client's ENC.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractServiceRefBinderJAXRPC extends AbstractServiceRefBinder
{
   public final Referenceable createReferenceable(final UnifiedServiceRefMetaData serviceRef, final ClassLoader ignored)
   {
      return this.createJAXRPCReferenceable(serviceRef);
   }

   /**
    * Template method for creating stack specific JAXRPC referenceables.
    *
    * @param serviceRef service reference UMDM
    * @return stack specific JAXRPC JNDI referenceable
    */
   protected abstract Referenceable createJAXRPCReferenceable(final UnifiedServiceRefMetaData serviceRef);
}
