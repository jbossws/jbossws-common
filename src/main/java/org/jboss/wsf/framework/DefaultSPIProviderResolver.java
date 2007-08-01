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
package org.jboss.wsf.framework;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.ServiceLoader;
import org.jboss.wsf.framework.deployment.DefaultDeploymentAspectManagerFactory;
import org.jboss.wsf.framework.deployment.DefaultDeploymentModelFactory;
import org.jboss.wsf.framework.deployment.DefaultLifecycleHandlerFactory;
import org.jboss.wsf.framework.http.DefaultHttpContextFactory;
import org.jboss.wsf.framework.http.DefaultHttpServerFactory;
import org.jboss.wsf.framework.invocation.DefaultResourceInjectorFactory;
import org.jboss.wsf.framework.serviceref.DefaultServiceRefHandler;
import org.jboss.wsf.framework.serviceref.DefaultServiceRefHandlerFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.DeploymentAspectManagerFactory;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.LifecycleHandlerFactory;
import org.jboss.wsf.spi.http.HttpContextFactory;
import org.jboss.wsf.spi.http.HttpServerFactory;
import org.jboss.wsf.spi.invocation.InvocationHandlerFactory;
import org.jboss.wsf.spi.invocation.RequestHandlerFactory;
import org.jboss.wsf.spi.invocation.ResourceInjectorFactory;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.spi.invocation.WebServiceContextFactory;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.serviceref.ServiceRefBinderFactory;
import org.jboss.wsf.spi.serviceref.ServiceRefHandlerFactory;

/**
 * @author Heiko.Braun@jboss.com
 *         Created: Jul 18, 2007
 */
public class DefaultSPIProviderResolver extends SPIProviderResolver
{
   private final static SPIProvider DEFAULT_PROVIDER = new Provider();

   public SPIProvider getProvider()
   {
      return DEFAULT_PROVIDER;
   }

   static class Provider extends SPIProvider
   {
      // provide logging
      private static final Logger log = Logger.getLogger(Provider.class);

      /**
       * Gets the specified SPI.
       */
      public <T> T getSPI(Class<T> spiType)
      {
         log.debug("provide SPI '" + spiType + "'");

         T returnType = null;

         // SPI provided by framework, default that can be overridden

         if (DeploymentAspectManagerFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultDeploymentAspectManagerFactory.class.getName());
         }
         if (DeploymentModelFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultDeploymentModelFactory.class.getName());
         }
         else if (HttpContextFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultHttpContextFactory.class.getName());
         }
         else if (HttpServerFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultHttpServerFactory.class.getName());
         }
         else if (LifecycleHandlerFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultLifecycleHandlerFactory.class.getName());
         }
         else if (ResourceInjectorFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultResourceInjectorFactory.class.getName());
         }
         else if (ServiceRefHandlerFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, DefaultServiceRefHandlerFactory.class.getName());
         }

         // SPI provided by either container or stack integration

         else if (EndpointRegistryFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }
         else if (InvocationHandlerFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }
         else if (RequestHandlerFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }
         else if (SecurityAdaptorFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }
         else if (ServerConfigFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }
         else if (ServiceRefBinderFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }
         else if (WebServiceContextFactory.class.equals(spiType))
         {
            returnType = (T)loadService(spiType, null);
         }

         // help debugging
         if (null == returnType)
            throw new WSFException("Failed to provide SPI '" + spiType + "'");

         log.debug(spiType + " Implementation: " + returnType);

         return returnType;
      }

      // Load SPI implementation through ServiceLoader
      private <T> T loadService(Class<T> spiType, String defaultImpl)
      {
         return (T)ServiceLoader.loadService(spiType.getName(), defaultImpl);
      }
   }

}
