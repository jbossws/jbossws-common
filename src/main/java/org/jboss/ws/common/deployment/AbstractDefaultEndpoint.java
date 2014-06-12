/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.wsf.spi.security.SecurityDomainContext;

/**
 * A general abstract JAXWS endpoint.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class AbstractDefaultEndpoint extends AbstractExtensible
{
   protected volatile Service service;
   protected volatile ObjectName name;
   protected volatile String shortName;
   protected volatile String urlPattern;
   protected volatile String targetBean;
   protected volatile Class<?> targetBeanClass;
   protected volatile EndpointState state;
   protected volatile EndpointType type;
   protected volatile RequestHandler requestHandler;
   protected volatile InvocationHandler invocationHandler;
   protected volatile LifecycleHandler lifecycleHandler;
   protected volatile EndpointMetrics metrics;
   protected volatile String address;
   protected volatile List<RecordProcessor> recordProcessors = new CopyOnWriteArrayList<RecordProcessor>();
   protected volatile SecurityDomainContext securityDomainContext;
   protected volatile InstanceProvider instanceProvider;
   
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

   public synchronized Class<?> getTargetBeanClass()
   {
      if (targetBeanClass != null)
         return targetBeanClass;

      ClassLoader classLoader = service.getDeployment().getClassLoader();

      try
      {
         targetBeanClass = classLoader.loadClass(targetBean);
      }
      catch (ClassNotFoundException ex)
      {
         throw new WSFDeploymentException(ex);
      }
      
      return targetBeanClass;
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

}
