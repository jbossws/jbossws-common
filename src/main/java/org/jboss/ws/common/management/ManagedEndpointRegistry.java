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

import static org.jboss.ws.common.Loggers.MANAGEMENT_LOGGER;

import javax.management.JMException;
import javax.management.MBeanServer;

import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A Service Endpoint Registry
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-May-2007
 */
public class ManagedEndpointRegistry extends DefaultEndpointRegistry implements ManagedEndpointRegistryMBean
{
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
         MANAGEMENT_LOGGER.cannotRegisterEndpointWithJmxServer(endpoint.getName(), ex);
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
            MANAGEMENT_LOGGER.cannotUnregisterDueToMBeanServerUnavailable();
      }
      catch (JMException ex)
      {
         MANAGEMENT_LOGGER.cannotUnregisterEndpointWithJmxServer(endpoint.getName(), ex);
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
      MANAGEMENT_LOGGER.destroyingServiceEndpointManager();
      if (mbeanServer != null)
      {
         getMbeanServer().unregisterMBean(OBJECT_NAME);
      }
   }
}
