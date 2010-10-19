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
 * Abstract service ref binder.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
abstract class AbstractServiceRefBinder implements ServiceRefBinder
{
   private static final Logger log = Logger.getLogger(AbstractServiceRefBinder.class);

   @Deprecated
   public final void setupServiceRef(final Context encCtx, final String encName, final AnnotatedElement ignored,
         final UnifiedServiceRefMetaData serviceRef, final ClassLoader loader) throws NamingException
   {
      final Referenceable jndiReferenceable = this.createReferenceable(serviceRef, loader);
      this.bind(encCtx, encName, jndiReferenceable);
   }

   public abstract Referenceable createReferenceable(final UnifiedServiceRefMetaData serviceRef,
         final ClassLoader loader);

   private void bind(final Context encCtx, final String encName, final Referenceable jndiReferenceable)
         throws NamingException
   {
      final String jndiFullName = encCtx.getNameInNamespace() + "/" + encName;

      log.info("Binding service reference to [jndi=" + jndiFullName + "]");
      Util.bind(encCtx, encName, jndiReferenceable);
   }
}
