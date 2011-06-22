/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.management;

import org.jboss.logging.Logger;
import java.util.ResourceBundle;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.ioc.IoCContainerProxy;
import org.jboss.wsf.spi.ioc.IoCContainerProxyFactory;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;

/**
 * Retrieves registry from MC kernel.
 *
 * @author <a href="mailto:hbraun@redhat.com">Heiko Braun</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DefaultEndpointRegistryFactory extends EndpointRegistryFactory
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(DefaultEndpointRegistryFactory.class);

   private Logger log = Logger.getLogger(DefaultEndpointRegistryFactory.class);
   /** The bean name in the kernel registry. */
   private static final String BEAN_NAME = "WSEndpointRegistry";
   private static final String SERVICE_NAME = "jboss.ws.registry";
   private static final EndpointRegistry fallbackRegistry = new DefaultEndpointRegistry();;

   /**
    * Constructor.
    */
   public DefaultEndpointRegistryFactory()
   {
      super();
   }
   
   /**
    * Returns endpoint registry registered in MC kernel.
    * 
    * @return endpoint registry
    */
   public EndpointRegistry getEndpointRegistry()
   {
      try
      {
         final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
         final SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
         final IoCContainerProxyFactory iocContainerFactory = spiProvider.getSPI(IoCContainerProxyFactory.class, cl);
         final IoCContainerProxy iocContainer = iocContainerFactory.getContainer();
         
         EndpointRegistry registry = null;
         try
         {
            //try MSC service name
            registry = iocContainer.getBean(DefaultEndpointRegistryFactory.SERVICE_NAME, EndpointRegistry.class);
         }
         catch (Exception e)
         {
            log.debug("Can't get endpoint registry: ", e);
            //ignore
         }
         if (registry == null)
         {
            //try MC bean name
            registry = iocContainer.getBean(DefaultEndpointRegistryFactory.BEAN_NAME, EndpointRegistry.class);
         }
         return registry;
      }
      catch (Exception e)
      {
         log.warn(BundleUtils.getMessage(bundle, "UNABLE_TO_GET_WSENDPOINTREGISTRY"));
         return fallbackRegistry; // JSE environment
      }
   }

}
