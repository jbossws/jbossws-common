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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.common.Messages;
import org.jboss.ws.common.injection.PreDestroyHolder;
import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointState;
import org.jboss.wsf.spi.deployment.EndpointType;
import org.jboss.wsf.spi.deployment.InstanceProvider;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;
import org.jboss.wsf.spi.security.SecurityDomainContext;

/**
 * A general abstract JAXWS endpoint.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public abstract class AbstractDefaultEndpoint extends AbstractExtensible implements Endpoint
{
   private volatile Service service;
   private volatile ObjectName name;
   private volatile String shortName;
   protected volatile String urlPattern;
   private volatile String targetBean;
   private volatile Class<?> targetBeanClass;
   private volatile EndpointState state;
   private volatile EndpointType type;
   private volatile RequestHandler requestHandler;
   private volatile InvocationHandler invocationHandler;
   private volatile LifecycleHandler lifecycleHandler;
   protected volatile EndpointMetrics metrics;
   private volatile String address;
   private volatile List<RecordProcessor> recordProcessors = new CopyOnWriteArrayList<RecordProcessor>();
   private volatile SecurityDomainContext securityDomainContext;
   private volatile InstanceProvider instanceProvider;
   private volatile EndpointConfig endpointConfig;
   
   AbstractDefaultEndpoint(String targetBean)
   {
      super(8, 4);
      this.targetBean = targetBean;
      this.state = EndpointState.UNDEFINED;
   }

   public Service getService()
   {
      return service;
   }

   public void setService(Service service)
   {
      assertEndpointSetterAccess();
      this.service = service;
   }

   public String getTargetBeanName()
   {
      return targetBean;
   }

   public void setTargetBeanName(String targetBean)
   {
      assertEndpointSetterAccess();
      this.targetBean = targetBean;
   }

   public Class<?> getTargetBeanClass()
   {
      Class<?> result = targetBeanClass;
      if (result == null) {
         synchronized (this)
         {
            result = targetBeanClass;
            if (result == null) {
               ClassLoader classLoader = service.getDeployment().getClassLoader();
               try
               {
                  targetBeanClass = classLoader.loadClass(targetBean);
                  result = targetBeanClass;
               }
               catch (ClassNotFoundException ex)
               {
                  throw new WSFDeploymentException(ex);
               }
            }
         }
      }
      return result;
   }

   public ObjectName getName()
   {
      if(null==name)
      {
         // build implicit name
         try
         {
            name = new ObjectName(
              Endpoint.SEPID_DOMAIN,
              Endpoint.SEPID_PROPERTY_ENDPOINT, targetBean
            );
            
         } catch (MalformedObjectNameException e)
         {
            //
         }
      }

      return name;
   }

   public void setName(ObjectName name)
   {
      assertEndpointSetterAccess();
      this.name = name;
   }

   public String getShortName()
   {
      return shortName;
   }

   public void setShortName(String shortName)
   {
      assertEndpointSetterAccess();
      this.shortName = shortName;
   }


   public EndpointState getState()
   {
      return state;
   }

   public void setState(EndpointState state)
   {
      this.state = state;
   }

   public EndpointType getType()
   {
      return type;
   }

   public void setType(EndpointType type)
   {
      this.type = type;
   }

   public RequestHandler getRequestHandler()
   {
      return requestHandler;
   }

   public void setRequestHandler(RequestHandler handler)
   {
      assertEndpointSetterAccess();
      this.requestHandler = handler;
   }

   public LifecycleHandler getLifecycleHandler()
   {
      return lifecycleHandler;
   }

   public void setLifecycleHandler(LifecycleHandler handler)
   {
      assertEndpointSetterAccess();
      this.lifecycleHandler = handler;
   }

   public InvocationHandler getInvocationHandler()
   {
      return invocationHandler;
   }

   public void setInvocationHandler(InvocationHandler handler)
   {
      assertEndpointSetterAccess();
      this.invocationHandler = handler;
   }

   @Override
   public <T> T addAttachment(Class<T> clazz, Object obj)
   {
      boolean isPreDestroyHolderClass = clazz.equals(PreDestroyHolder.class); // JBWS-2268 hack 
      boolean isObjectClass = clazz.equals(Object.class); // JBWS-2486 hack
      
      if (!isPreDestroyHolderClass && !isObjectClass) 
      {
         assertEndpointSetterAccess();
      }
      return super.addAttachment(clazz, obj);
   }

   @Override
   public <T> T removeAttachment(Class<T> key)
   {
      boolean isPreDestroyHolderClass = key.equals(PreDestroyHolder.class); // JBWS-2268 hack 
      boolean isObjectClass = key.equals(Object.class); // JBWS-2486 hack

      if (!isPreDestroyHolderClass && !isObjectClass) 
      {
         assertEndpointSetterAccess();
      }
      return super.removeAttachment(key);
   }

   public void removeProperty(String key)
   {
      assertEndpointSetterAccess();
      super.removeProperty(key);
   }

   public void setProperty(String key, Object value)
   {
      assertEndpointSetterAccess();
      super.setProperty(key, value);
   }

   protected void assertEndpointSetterAccess()
   {
      if (state == EndpointState.STARTED)
         throw Messages.MESSAGES.cannotModifyEndpointInState(state, getName());
   }

   public List<RecordProcessor> getRecordProcessors()
   {
      return recordProcessors;
   }
   
   public void setRecordProcessors(List<RecordProcessor> recordProcessors)
   {
      this.recordProcessors = new CopyOnWriteArrayList<RecordProcessor>(recordProcessors);
   }
   
   public void processRecord(Record record)
   {
      for (RecordProcessor processor : recordProcessors)
      {
         if (processor.isRecording())
         {
            boolean match = true;
            if (processor.getFilters() != null)
            {
               for (Iterator<RecordFilter> it = processor.getFilters().iterator(); it.hasNext() && match;)
               {
                  match = it.next().match(record);
               }
            }
            if (match)
            {
               processor.processRecord(record);
            }
         }
      }
   }

   public String getAddress()
   {
      return this.address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }
   
   public SecurityDomainContext getSecurityDomainContext()
   {
      return securityDomainContext;
   }

   public void setSecurityDomainContext(SecurityDomainContext securityDomainContext)
   {
      this.securityDomainContext = securityDomainContext;
   }

   public InstanceProvider getInstanceProvider()
   {
      return instanceProvider;
   }

   public void setInstanceProvider(final InstanceProvider instanceProvider)
   {
      assertEndpointSetterAccess();
      this.instanceProvider = instanceProvider;
   }

   public void setEndpointConfig(final EndpointConfig endpointConfig)
   {
      assertEndpointSetterAccess();
      this.endpointConfig = endpointConfig;
   }
   
   public EndpointConfig getEndpointConfig()
   {
      return endpointConfig;
   }
}
