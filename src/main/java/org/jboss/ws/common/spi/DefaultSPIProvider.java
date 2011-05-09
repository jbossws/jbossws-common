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
package org.jboss.ws.common.spi;

import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.deployment.DefaultDeploymentModelFactory;
import org.jboss.ws.common.deployment.DefaultLifecycleHandlerFactory;
import org.jboss.ws.common.invocation.DefaultResourceInjectorFactory;
import org.jboss.ws.common.management.DefaultEndpointMetricsFactory;
import org.jboss.ws.common.management.DefaultEndpointRegistryFactory;
import org.jboss.ws.common.management.DefaultJMSEndpointResolver;
import org.jboss.ws.common.security.DefaultSecurityAdapterFactory;
import org.jboss.ws.common.serviceref.DefaultServiceRefHandlerFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.LifecycleHandlerFactory;
import org.jboss.wsf.spi.invocation.ResourceInjectorFactory;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.spi.management.EndpointMetricsFactory;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.management.JMSEndpointResolver;
import org.jboss.wsf.spi.serviceref.ServiceRefHandlerFactory;

/**
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
class DefaultSPIProvider extends SPIProvider
{
   /**
    * Gets the specified SPI, using the provided classloader
    */
   @Override
   public <T> T getSPI(Class<T> spiType, ClassLoader loader)
   {
      T returnType = null;

      // SPIs provided by framework, defaults can be overridden
      if (DeploymentModelFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultDeploymentModelFactory.class, loader);
      }
      else if (EndpointMetricsFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultEndpointMetricsFactory.class, loader);
      }
      else if (LifecycleHandlerFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultLifecycleHandlerFactory.class, loader);
      }
      else if (ResourceInjectorFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultResourceInjectorFactory.class, loader);
      }
      else if (ServiceRefHandlerFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultServiceRefHandlerFactory.class, loader);
      }
      else if (SecurityAdaptorFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultSecurityAdapterFactory.class, loader);
      }
      else if (EndpointRegistryFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultEndpointRegistryFactory.class, loader);
      }
      else if (JMSEndpointResolver.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultJMSEndpointResolver.class, loader);
      }
      else
      {
         // SPI provided by either container or stack integration that has no default implementation
         returnType = (T)loadService(spiType, null, loader);
      }

      if (returnType == null)
         throw new WSFException("Failed to provide SPI '" + spiType + "'");
      
      return returnType;
   }

   // Load SPI implementation through ServiceLoader
   @SuppressWarnings("unchecked")
   private <T> T loadService(Class<T> spiType, Class<?> defaultImpl, ClassLoader loader)
   {
      final String defaultImplName = defaultImpl != null ? defaultImpl.getName() : null;
      return (T)ServiceLoader.loadService(spiType.getName(), defaultImplName, loader);
   }

}
