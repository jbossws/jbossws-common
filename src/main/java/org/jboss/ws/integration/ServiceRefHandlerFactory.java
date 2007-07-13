/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ws.integration;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.wsf.spi.utils.ServiceLoader;

// $Id$

/**
 * A factory for the ServiceRefHandler 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2004
 */
public abstract class ServiceRefHandlerFactory
{
   public static ServiceRefHandler getServiceRefHandler()
   {
      ServiceRefHandler handler;
      if (KernelLocator.getKernel() != null)
      {
         handler = getServerSideServiceRefHandler();
      }
      else
      {
         handler = getClientSideServiceRefHandler();
      }
      return handler;
   }

   private static ServiceRefHandler getServerSideServiceRefHandler()
   {
      Kernel kernel = KernelLocator.getKernel();
      KernelRegistry registry = kernel.getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceRefHandler.BEAN_NAME);
      ServiceRefHandler handler = (ServiceRefHandler)entry.getTarget();

      // Try legacy JBossAS-4.2 name
      if (handler == null)
      {
         entry = registry.getEntry("ServiceRefHandler");
         handler = (ServiceRefHandler)entry.getTarget();
      }
      return handler;
   }

   private static ServiceRefHandler getClientSideServiceRefHandler()
   {
      String propName = ServiceRefHandler.class.getName();
      String defaultImpl = "org.jboss.wsf.spi.deployment.serviceref.ServiceRefHandlerImpl";
      ServiceRefHandler handler = (ServiceRefHandler)ServiceLoader.loadService(propName, defaultImpl);
      return handler;
   }
}
