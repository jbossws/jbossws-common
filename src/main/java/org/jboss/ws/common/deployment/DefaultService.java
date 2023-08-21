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
package org.jboss.ws.common.deployment;

import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.EndpointTypeFilter;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


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

}
