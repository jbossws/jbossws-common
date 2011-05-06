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
package org.jboss.ws.common.management;

import javax.management.JMException;
import javax.management.MBeanServer;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A Service Endpoint Registry
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-May-2007
 */
public class ManagedEndpointRegistry extends DefaultEndpointRegistry implements ManagedEndpointRegistryMBean
{
   // provide logging
   private static final Logger log = Logger.getLogger(ManagedEndpointRegistry.class);

   // The MBeanServer
   private MBeanServer mbeanServer;

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   @Override
   public void register(Endpoint endpoint)
   {
      super.register(endpoint);

      try
      {
         ManagedEndpoint jmxEndpoint = new ManagedEndpoint(endpoint, mbeanServer);
         getMbeanServer().registerMBean(jmxEndpoint, endpoint.getName());
      }
      catch (Exception ex)
      {
         log.error("Cannot register endpoint with JMX server", ex);
      }
   }

   @Override
   public void unregister(Endpoint endpoint)
   {
      super.unregister(endpoint);
      try
      {
         if (getMbeanServer() != null)
            getMbeanServer().unregisterMBean(endpoint.getName());
         else
            log.warn("MBeanServer not available, cannot unregister endpoint with JMX server");
      }
      catch (JMException ex)
      {
         log.error("Cannot unregister endpoint with JMX server", ex);
      }
   }

   public void create() throws Exception
   {
      if (mbeanServer != null)
      {
         getMbeanServer().registerMBean(this, OBJECT_NAME);
      }
   }

   public void destroy() throws Exception
   {
      log.debug("Destroy service endpoint manager");
      if (mbeanServer != null)
      {
         getMbeanServer().unregisterMBean(OBJECT_NAME);
      }
   }
}
