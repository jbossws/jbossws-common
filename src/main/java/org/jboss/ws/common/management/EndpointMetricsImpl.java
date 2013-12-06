/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
      return System.currentTimeMillis();
   }

   public void processResponseMessage(long beginTime)
   {
      if (beginTime > 0) {
         final long procTime = System.currentTimeMillis() - beginTime;
         r.lock();
         try {
            responseCount.incrementAndGet();
            totalProcessingTime.addAndGet(procTime);
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
         final long procTime = System.currentTimeMillis() - beginTime;
         r.lock();
         try {
            faultCount.incrementAndGet();
            totalProcessingTime.addAndGet(procTime);
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
      return minProcessingTime.longValue();
   }

   public long getMaxProcessingTime()
   {
      return maxProcessingTime.longValue();
   }

   public long getAverageProcessingTime()
   {
      w.lock();
      try {
         return totalProcessingTime.get() / (responseCount.get() + faultCount.get());
      } finally {
         w.unlock();
      }
   }

   public long getTotalProcessingTime()
   {
      return totalProcessingTime.get();
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
}
