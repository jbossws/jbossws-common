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

// $Id$

import java.util.Date;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * Service Endpoint Metrics
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Dec-2005
 */
public class EndpointMetricsImpl implements EndpointMetrics
{
   private Endpoint endpoint;

   private Date startTime;
   private Date stopTime;
   private long requestCount;
   private long responseCount;
   private long faultCount;
   private long maxProcessingTime;
   private long minProcessingTime;
   private long avgProcessingTime;
   private long totalProcessingTime;

   public Endpoint getEndpoint()
   {
      return endpoint;
   }

   public void setEndpoint(Endpoint endpoint)
   {
      this.endpoint = endpoint;
   }

   public void start()
   {
      startTime = new Date();
      stopTime = null;
      requestCount = 0;
      responseCount = 0;
      faultCount = 0;
      maxProcessingTime = 0;
      minProcessingTime = 0;
      avgProcessingTime = 0;
      totalProcessingTime = 0;
   }

   public void stop()
   {
      stopTime = new Date();
   }

   public long processRequestMessage()
   {
      requestCount++;
      return System.currentTimeMillis();
   }

   public void processResponseMessage(long beginTime)
   {
      responseCount++;
      processAnyMessage(beginTime);
   }

   public void processFaultMessage(long beginTime)
   {
      faultCount++;
      processAnyMessage(beginTime);
   }

   private void processAnyMessage(long beginTime)
   {
      if (beginTime > 0)
      {
         long procTime = System.currentTimeMillis() - beginTime;

         if (minProcessingTime == 0)
            minProcessingTime = procTime;

         maxProcessingTime = Math.max(maxProcessingTime, procTime);
         minProcessingTime = Math.min(minProcessingTime, procTime);
         totalProcessingTime = totalProcessingTime + procTime;
         avgProcessingTime = totalProcessingTime / (responseCount + faultCount);
      }
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public Date getStopTime()
   {
      return stopTime;
   }

   public long getMinProcessingTime()
   {
      return minProcessingTime;
   }

   public long getMaxProcessingTime()
   {
      return maxProcessingTime;
   }

   public long getAverageProcessingTime()
   {
      return avgProcessingTime;
   }

   public long getTotalProcessingTime()
   {
      return totalProcessingTime;
   }

   public long getRequestCount()
   {
      return requestCount;
   }

   public long getFaultCount()
   {
      return faultCount;
   }

   public long getResponseCount()
   {
      return responseCount;
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nEndpoint Metrics: " + endpoint.getName());
      buffer.append("\n  startTime=" + startTime);
      buffer.append("\n  stopTime=" + stopTime);
      buffer.append("\n  requestCount=" + requestCount);
      buffer.append("\n  responseCount=" + responseCount);
      buffer.append("\n  faultCount=" + faultCount);
      buffer.append("\n  maxProcessingTime=" + maxProcessingTime);
      buffer.append("\n  minProcessingTime=" + minProcessingTime);
      buffer.append("\n  avgProcessingTime=" + avgProcessingTime);
      buffer.append("\n  totalProcessingTime=" + totalProcessingTime);
      return buffer.toString();
   }
}
