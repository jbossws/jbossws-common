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
package org.jboss.ws.common.deployment;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.management.JMException;
import javax.management.MBeanServer;

import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.api.util.BundleUtils;
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
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointRecordProcessorDeploymentAspect.class);
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
      try
      {
         mbeanServer.registerMBean(processor, ObjectNameFactory.create(ep.getName() + ",recordProcessor=" + processor.getName()));
      }
      catch (JMException ex)
      {
         log.debug("Cannot register endpoint with JMX server, trying with the default ManagedRecordProcessor: " + ex.getMessage());
         try
         {
            mbeanServer.registerMBean(new ManagedRecordProcessor(processor), ObjectNameFactory.create(ep.getName() + ",recordProcessor=" + processor.getName()));
         }
         catch (JMException innerEx)
         {
            log.error(BundleUtils.getMessage(bundle, "CANNOT_REGISTER_ENDPOINT_WITH_JMX_SERVER"),  innerEx);
         }
      }
   }

   private void unregisterRecordProcessor(RecordProcessor processor, Endpoint ep)
   {
      try
      {
         mbeanServer.unregisterMBean(ObjectNameFactory.create(ep.getName() + ",recordProcessor=" + processor.getName()));
      }
      catch (JMException ex)
      {
         log.error(BundleUtils.getMessage(bundle, "CANNOT_UNREGISTER_RECORD_PROCESSOR_WITH_JMX_SERVER"),  ex);
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
