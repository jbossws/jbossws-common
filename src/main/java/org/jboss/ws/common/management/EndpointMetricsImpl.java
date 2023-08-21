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
package org.jboss.ws.common.management;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * Service Endpoint Metrics
 *
 * @author alessio.soldano@jboss.com
 * @author Thomas.Diesler@jboss.org
 * @since 14-Dec-2005
 */
public class EndpointMetricsImpl implements EndpointMetrics
{
   private volatile boolean started = false;
   
   //read-write lock for average calculation (there's no CAS function for
   //atomically computing the average, so we do that on-demand within a
   //write lock; the updates to sum and response/fault counts, which are
   //used to compute the average, are updated within read locks)
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Lock r = lock.readLock();
   private final Lock w = lock.writeLock();
   
   private final AtomicLong requestCount = new AtomicLong(0);
   private final AtomicLong responseCount = new AtomicLong(0);
   private final AtomicLong faultCount = new AtomicLong(0);
   private final AtomicLong maxProcessingTime = new AtomicLong(0);
   private final AtomicLong minProcessingTime = new AtomicLong(0);
   private final AtomicLong totalProcessingTime = new AtomicLong(0);
   
   private volatile long updateTime = 0;

   public void start()
   {
      started = true;
   }

   public void stop()
   {
      started = false;
   }

   public long processRequestMessage()
   {
      if (!started)
      {
         return 0;
      }
      requestCount.incrementAndGet();
      updateTime = System.nanoTime();
      return updateTime;
   }

   public void processResponseMessage(long beginTime)
   {
      if (beginTime > 0) {
         final long procTime = System.nanoTime() - beginTime;
         r.lock();
         try {
            responseCount.incrementAndGet();
            totalProcessingTime.addAndGet(procTime);
            updateTime = System.nanoTime();
         } finally {
            r.unlock();
         }
         minProcessingTime.compareAndSet(0, procTime);
         updateMax(maxProcessingTime, procTime);
         updateMin(minProcessingTime, procTime);
      }
   }

   public void processFaultMessage(long beginTime)
   {
      if (beginTime > 0) {
         final long procTime = System.nanoTime() - beginTime;
         r.lock();
         try {
            faultCount.incrementAndGet();
            totalProcessingTime.addAndGet(procTime);
            updateTime = System.nanoTime();
         } finally {
            r.unlock();
         }
         minProcessingTime.compareAndSet(0, procTime);
         updateMax(maxProcessingTime, procTime);
         updateMin(minProcessingTime, procTime);
      }
   }

   private void updateMin(AtomicLong min, long value)
   {
      long oldValue = min.get();
      while (value < oldValue)
      {
         if (min.compareAndSet(oldValue, value))
            break;
         oldValue = min.get();
      }
   }

   private void updateMax(AtomicLong max, long value)
   {
      long oldValue = max.get();
      while (value > oldValue)
      {
         if (max.compareAndSet(oldValue, value))
            break;
         oldValue = max.get();
      }
   }

   public long getMinProcessingTime()
   {
      return minProcessingTime.longValue() / 1000000;
   }

   public long getMaxProcessingTime()
   {
      return maxProcessingTime.longValue() / 1000000;
   }

   public long getAverageProcessingTime()
   {
      w.lock();
      try {
         final long totResponses = responseCount.get() + faultCount.get();
         return totResponses != 0 ? totalProcessingTime.get() / (totResponses * 1000000) : 0;
      } finally {
         w.unlock();
      }
   }

   public long getTotalProcessingTime()
   {
      return totalProcessingTime.get() / 1000000;
   }

   public long getRequestCount()
   {
      return requestCount.get();
   }

   public long getFaultCount()
   {
      return faultCount.get();
   }

   public long getResponseCount()
   {
      return responseCount.get();
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("requestCount=" + requestCount);
      buffer.append("\n  responseCount=" + responseCount);
      buffer.append("\n  faultCount=" + faultCount);
      buffer.append("\n  maxProcessingTime=" + maxProcessingTime);
      buffer.append("\n  minProcessingTime=" + minProcessingTime);
      buffer.append("\n  avgProcessingTime=" + getAverageProcessingTime());
      buffer.append("\n  totalProcessingTime=" + totalProcessingTime);
      return buffer.toString();
   }

   @Override
   public long getUpdateTime()
   {
      return this.updateTime;
   }
}
