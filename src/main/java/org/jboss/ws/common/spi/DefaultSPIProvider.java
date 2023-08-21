/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.common.spi;

import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.Messages;
import org.jboss.ws.common.deployment.DefaultDeploymentModelFactory;
import org.jboss.ws.common.deployment.DefaultLifecycleHandlerFactory;
import org.jboss.ws.common.management.DefaultEndpointMetricsFactory;
import org.jboss.ws.common.management.DefaultJMSEndpointResolver;
import org.jboss.ws.common.security.DefaultSecurityAdapterFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.LifecycleHandlerFactory;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.spi.management.EndpointMetricsFactory;
import org.jboss.wsf.spi.management.JMSEndpointResolver;

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
      else if (SecurityAdaptorFactory.class.equals(spiType))
      {
         returnType = loadService(spiType, DefaultSecurityAdapterFactory.class, loader);
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
         throw Messages.MESSAGES.failedToProvideSPI(spiType);
      
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
