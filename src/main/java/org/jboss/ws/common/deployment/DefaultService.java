/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.deployment;

import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.EndpointTypeFilter;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * A service collecting endpoints belonging to the same deployment. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @author alessio.soldano@jboss.com
 * @since 20-Apr-2007 
 */
public class DefaultService extends AbstractExtensible implements Service
{
   private final Deployment dep;
   private final List<Endpoint> endpoints = new LinkedList<Endpoint>();
   private volatile String contextRoot;
   private volatile String virtualHost;

   DefaultService(Deployment dep)
   {
      super(4, 4);
      this.dep = dep;
   }

   public Deployment getDeployment()
   {
      return dep;
   }

   public void addEndpoint(Endpoint endpoint)
   {
      endpoint.setService(this);
      endpoints.add(endpoint);
   }
   
   public boolean removeEndpoint(Endpoint endpoint)
   {
      boolean done = endpoints.remove(endpoint);
      if (done) {
         endpoint.setService(null);
      }
      return done;
   }

   public List<Endpoint> getEndpoints()
   {
      return Collections.unmodifiableList(endpoints);
   }

   @Override
   public List<Endpoint> getEndpoints(final EndpointTypeFilter filter)
   {
      List<Endpoint> result = new LinkedList<Endpoint>();
      for (Endpoint endpoint : endpoints)
      {
         if (filter.accept(endpoint.getType()))
         {
            result.add(endpoint);
         }
      }
      return Collections.unmodifiableList(result);
   }      

   public Endpoint getEndpointByName(String shortName)
   {
      Endpoint retEndpoint = null;
      for (Endpoint ep : endpoints)
      {
         if (ep.getShortName().equals(shortName))
         {
            retEndpoint = ep;
            break;
         }
      }
      return retEndpoint;
   }
   
   public String getContextRoot()
   {
      return contextRoot;
   }

   public void setContextRoot(String contextRoot)
   {
      this.contextRoot = contextRoot;
   }

   public String getVirtualHost()
   {
      return virtualHost;
   }

   public void setVirtualHost(String virtualHost)
   {
      this.virtualHost = virtualHost;      
   }

   @Override
   public Set<String> getRuntimeConfigFlags()
   {
      return dep.getRuntimeConfigFlags();
   }

}
