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
package org.jboss.ws.common.serviceref;

import java.util.ResourceBundle;

import javax.naming.Referenceable;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.serviceref.ServiceRefBinder;
import org.jboss.wsf.spi.serviceref.ServiceRefBinderFactory;
import org.jboss.wsf.spi.serviceref.ServiceRefHandler;

/**
 * Binds service refs to the client's ENC.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DefaultServiceRefHandler implements ServiceRefHandler
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(DefaultServiceRefHandler.class);
   @Override
   public Referenceable createReferenceable(final UnifiedServiceRefMetaData serviceRefMD)
   {
      if (serviceRefMD.getVfsRoot() == null)
      {
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "VFSROOT_MUST_BE_PROVIDED"));
      }
      if (serviceRefMD.getType() == null)
      {
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "SERVICE_REFERENCE_TYPE_MUST_BE_PROVIDED"));
      }

      return this.getBinder(serviceRefMD.getType()).createReferenceable(serviceRefMD);
   }

   private ServiceRefBinder getBinder(final Type type)
   {
      final SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      final ServiceRefBinderFactory serviceRefBindingFactory = spiProvider.getSPI(ServiceRefBinderFactory.class);

      return serviceRefBindingFactory.newServiceRefBinder(type);
   }
}
