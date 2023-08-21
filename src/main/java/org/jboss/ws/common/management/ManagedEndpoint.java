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

import static org.jboss.ws.common.Loggers.MANAGEMENT_LOGGER;

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
   private final Endpoint endpoint;
   private final MBeanServer mbeanServer;

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

   public long getTotalProcessingTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getTotalProcessingTime() : 0;
   }
   @Override
   public long getUpdateTime()
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      return metrics != null ? metrics.getUpdateTime() : 0;
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
