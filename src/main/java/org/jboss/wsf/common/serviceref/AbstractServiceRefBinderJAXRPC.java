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

import java.lang.reflect.AnnotatedElement;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Referenceable;

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.serviceref.ServiceRefBinder;

/**
 * Binds a JAXRPC Service object in the client's ENC for every service-ref element in the
 * deployment descriptor.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractServiceRefBinderJAXRPC implements ServiceRefBinder
{
   // logging support
   private static Logger log = Logger.getLogger(AbstractServiceRefBinderJAXRPC.class);

   /**
    * Binds a Service into the callers ENC for every service-ref element
    */
   public void setupServiceRef(Context encCtx, String encName, AnnotatedElement ignored,
         UnifiedServiceRefMetaData serviceRef, ClassLoader loader) throws NamingException
   {
      String externalName = encCtx.getNameInNamespace() + "/" + encName;
      log.info("setupServiceRef [jndi=" + externalName + "]");

      // Do not use rebind, the binding should be unique
      Referenceable ref = this.createReferenceable(serviceRef);
      Util.bind(encCtx, encName, ref);
   }

   protected abstract Referenceable createReferenceable(UnifiedServiceRefMetaData serviceRef);
}
