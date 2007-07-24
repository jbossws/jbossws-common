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
package org.jboss.wsf.spi.management;

// $Id$

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointRegistry;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A general endpoint registry.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007
 */
public class DefaultEndpointRegistry implements EndpointRegistry
{
   // provide logging
   private static final Logger log = Logger.getLogger(DefaultEndpointRegistry.class);

   private Map<ObjectName, Endpoint> endpoints = new HashMap<ObjectName, Endpoint>();

   public Endpoint getEndpoint(ObjectName epName)
   {
      if (epName == null)
         throw new IllegalArgumentException("Endpoint name cannot be null");

      if (isRegistered(epName) == false)
         throw new IllegalStateException("Endpoint not registered: " + epName);

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
         throw new IllegalArgumentException("Endpoint name cannot be null");

      return endpoints.get(epName) != null;
   }

   public Set<ObjectName> getEndpoints()
   {
      return endpoints.keySet();
   }

   public void register(Endpoint endpoint)
   {
      if (endpoint == null)
         throw new IllegalArgumentException("Endpoint cannot be null");

      ObjectName epName = endpoint.getName();
      if (epName == null)
         throw new IllegalStateException("Endpoint name cannot be null for: " + endpoint);

      if (isRegistered(epName))
         throw new IllegalStateException("Endpoint already registered: " + epName);

      log.info("register: " + epName);
      endpoints.put(epName, endpoint);
   }

   public void unregister(Endpoint endpoint)
   {
      if (endpoint == null)
         throw new IllegalArgumentException("Endpoint cannot be null");

      ObjectName epName = endpoint.getName();
      if (isRegistered(epName) == false)
         throw new IllegalStateException("Endpoint not registered: " + epName);

      log.info("remove: " + epName);
      endpoints.remove(epName);
   }
}
