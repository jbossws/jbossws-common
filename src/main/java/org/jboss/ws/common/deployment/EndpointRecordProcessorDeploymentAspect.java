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

import java.util.LinkedList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.common.Loggers;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.monitoring.ManagedRecordProcessor;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A deployer that sets the record processors for each endpoint
 *
 * @author alessio.soldano@jboss.org
 * @since 12-Dec-2007
 */
public class EndpointRecordProcessorDeploymentAspect extends AbstractDeploymentAspect
{
   private MBeanServer mbeanServer;
   private List<RecordProcessor> processors;

   public void setProcessors(List<RecordProcessor> processors)
   {
      this.processors = processors;
   }

   @Override
   public void start(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         List<RecordProcessor> processorList = new LinkedList<RecordProcessor>();
         if (processors != null)
         {
            for (RecordProcessor pr : processors)
            {
               try
               {
                  RecordProcessor clone = (RecordProcessor)pr.clone();
                  registerRecordProcessor(clone, ep);
                  processorList.add(clone);
               }
               catch (CloneNotSupportedException ex)
               {
                  throw new RuntimeException(ex);
               }
            }
         }
         ep.setRecordProcessors(processorList);
      }
   }

   @Override
   public void stop(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         List<RecordProcessor> processors = ep.getRecordProcessors();
         for (RecordProcessor processor : processors)
         {
            this.unregisterRecordProcessor(processor, ep);
         }
      }
   }

   private void registerRecordProcessor(RecordProcessor processor, Endpoint ep)
   {
      final ObjectName on = ObjectNameFactory.create(ep.getName() + ",recordProcessor=" + processor.getName());
      try
      {
         mbeanServer.registerMBean(processor, on);
      }
      catch (JMException ex)
      {
         Loggers.MANAGEMENT_LOGGER.cannotRegisterProvidedProcessor(on, ex);
         try
         {
            mbeanServer.registerMBean(new ManagedRecordProcessor(processor), on);
         }
         catch (JMException innerEx)
         {
            Loggers.MANAGEMENT_LOGGER.cannotRegisterProcessorWithJmxServer(on, innerEx);
         }
      }
   }

   private void unregisterRecordProcessor(RecordProcessor processor, Endpoint ep)
   {
      final ObjectName on = ObjectNameFactory.create(ep.getName() + ",recordProcessor=" + processor.getName());
      try
      {
         mbeanServer.unregisterMBean(on);
      }
      catch (JMException ex)
      {
         Loggers.MANAGEMENT_LOGGER.cannotUnregisterProcessorWithJmxServer(on, ex);
      }
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }
}
