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

import java.util.Date;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.monitoring.ManagedRecordProcessor;
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
   private MBeanServer mbeanServer;

   public ManagedEndpoint(Endpoint endpoint, MBeanServer mbeanServer)
   {
      this.endpoint = endpoint;
      this.mbeanServer = mbeanServer;
   }
   
   public String getAddress()
   {
      return endpoint.getAddress();
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

   public void processRecord(Record record)
   {
      endpoint.processRecord(record);
   }

   public void addRecordProcessor(RecordProcessor processor)
   {
      ObjectName on = ObjectNameFactory.create(endpoint.getName() + ",recordProcessor=" + processor.getName());
      this.getRecordProcessors().add(processor);
      try
      {
         mbeanServer.registerMBean(processor, on);
      }
      catch (JMException ex)
      {
         MANAGEMENT_LOGGER.cannotRegisterProvidedProcessor(on, ex);
         try
         {
            mbeanServer.registerMBean(new ManagedRecordProcessor(processor), on);
         }
         catch (JMException innerEx)
         {
            MANAGEMENT_LOGGER.cannotRegisterProcessorWithJmxServer(on, innerEx);
         }
      }
   }

   public List<RecordProcessor> getRecordProcessors()
   {
      return endpoint.getRecordProcessors();
   }

   public void setRecordProcessors(List<RecordProcessor> processors)
   {
      //unregister current processors
      for (RecordProcessor processor : endpoint.getRecordProcessors())
      {
         final ObjectName on = ObjectNameFactory.create(endpoint.getName() + ",recordProcessor=" + processor.getName());
         try
         {
            mbeanServer.unregisterMBean(on);
         }
         catch (JMException ex)
         {
            MANAGEMENT_LOGGER.cannotUnregisterProcessorWithJmxServer(on, ex);
         }
      }
      //set and register the new processors
      endpoint.setRecordProcessors(processors);
      for (RecordProcessor processor : processors)
      {
         final ObjectName on = ObjectNameFactory.create(endpoint.getName() + ",recordProcessor=" + processor.getName());
         try
         {
            mbeanServer.registerMBean(processor, on);
         }
         catch (JMException ex)
         {
            MANAGEMENT_LOGGER.cannotRegisterProvidedProcessor(on, ex);
            try
            {
               mbeanServer.registerMBean(new ManagedRecordProcessor(processor), on);
            }
            catch (JMException innerEx)
            {
               MANAGEMENT_LOGGER.cannotRegisterProcessorWithJmxServer(on, innerEx);
            }
         }
      }
   }
}
