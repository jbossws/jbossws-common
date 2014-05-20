/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
import static org.jboss.ws.common.Messages.MESSAGES;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointResolver;

/**
 * A general endpoint registry.
 *
 * @author Thomas.Diesler@jboss.com
 * @author alessio.soldano@jboss.com
 * 
 * @since 20-Apr-2007
 */
public class DefaultEndpointRegistry implements EndpointRegistry
{
   private final Map<ObjectName, Endpoint> endpoints = new ConcurrentHashMap<ObjectName, Endpoint>();

   public Endpoint getEndpoint(ObjectName epName)
   {
      if (epName == null)
         throw MESSAGES.endpointNameCannotBeNull();

      if (isRegistered(epName) == false)
         throw MESSAGES.endpointNotRegistered(epName);

      Endpoint endpoint = endpoints.get(epName);
      return endpoint;
   }

   public Endpoint resolve(EndpointResolver resolver)
   {
      return resolver.query(endpoints.values().iterator());
   }

   public boolean isRegistered(ObjectName epName)
   {
      if (epName == null)
         throw MESSAGES.endpointNameCannotBeNull();

      return endpoints.get(epName) != null;
   }

   public Set<ObjectName> getEndpoints()
   {
      return endpoints.keySet();
   }

   public void register(Endpoint endpoint)
   {
      if (endpoint == null)
         throw MESSAGES.cannotRegisterUnregisterNullEndpoint();

      ObjectName epName = endpoint.getName();
      if (epName == null)
         throw MESSAGES.cannotRegisterEndpointWithNullName(endpoint.getName());

      if (isRegistered(epName))
         throw MESSAGES.endpointAlreadyRegistered(epName);

      MANAGEMENT_LOGGER.endpointRegistered(epName);
      endpoints.put(epName, endpoint);
   }

   public void unregister(Endpoint endpoint)
   {
      if (endpoint == null)
         throw MESSAGES.cannotRegisterUnregisterNullEndpoint();

      ObjectName epName = endpoint.getName();
      if (isRegistered(epName) == false)
         throw MESSAGES.endpointNotRegistered(epName);

      MANAGEMENT_LOGGER.endpointUnregistered(epName);
      endpoints.remove(epName);
   }
}
