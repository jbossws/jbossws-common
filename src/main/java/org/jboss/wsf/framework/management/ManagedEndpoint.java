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
package org.jboss.wsf.framework.management;

// $Id: ManagedEndpointRegistry.java 3146 2007-05-18 22:55:26Z thomas.diesler@jboss.com $

import java.util.Date;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * The endpoint MBean representation 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 20-Jun-2007
 */
public class ManagedEndpoint implements ManagedEndpointMBean
{
   private Endpoint endpoint;

   public ManagedEndpoint(Endpoint endpoint)
   {
      this.endpoint = endpoint;
   }

   public long getAverageProcessingTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getAverageProcessingTime() : 0;
   }

   public long getFaultCount()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getFaultCount() : 0;
   }

   public long getMaxProcessingTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getMaxProcessingTime() : 0;
   }

   public long getMinProcessingTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getMinProcessingTime() : 0;
   }

   public long getRequestCount()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getRequestCount() : 0;
   }

   public long getResponseCount()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getResponseCount() : 0;
   }

   public Date getStartTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getStartTime() : null;
   }

   public Date getStopTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getStopTime() : null;
   }

   public long getTotalProcessingTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getTotalProcessingTime() : 0;
   }

   public void start()
   {
      endpoint.getLifecycleHandler().start(endpoint);
   }

   public void stop()
   {
      endpoint.getLifecycleHandler().stop(endpoint);
   }
}
