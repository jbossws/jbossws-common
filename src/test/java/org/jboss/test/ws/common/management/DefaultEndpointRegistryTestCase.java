/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.common.management;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.common.management.DefaultEndpointRegistry;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointState;
import org.jboss.wsf.spi.deployment.EndpointType;
import org.jboss.wsf.spi.deployment.InstanceProvider;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.security.SecurityDomainContext;

import junit.framework.TestCase;

/**
 * Test the DefaultEndpointRegistry
 *
 * @author alessio.soldano@jboss.com
 * @since 23-Apr-2012
 */
public class DefaultEndpointRegistryTestCase extends TestCase
{
   public void testSingleThreadAccess() throws Exception {
      final int MAX = 10;
      DefaultEndpointRegistry reg = new DefaultEndpointRegistry();
      for (int i = 0; i < MAX; i++) {
         Endpoint ep = getTestEndpoint("Foo" + i, "prop", "v" + i);
         reg.register(ep);
      }
      
      Iterator<ObjectName> it = reg.getEndpoints().iterator();
      checkContents(it, MAX);
      
      reg.register(getTestEndpoint("Foo" + MAX, "prop", "v" + MAX));

      assertFalse(it.hasNext());
      checkContents(reg.getEndpoints().iterator(), MAX + 1);
   }

   public void testConcurrentAccess() throws Exception {
      DefaultEndpointRegistry reg = new DefaultEndpointRegistry();
      reg.register(getTestEndpoint("Test", "prop", "my-value"));
      ExecutorService es = Executors.newFixedThreadPool(2);
      List<Callable<Boolean>> callables = new LinkedList<Callable<Boolean>>();
      final int size = 10;
      for (int i = 1; i <= 10; i++) {
         callables.add(new Register(reg, "Foo", i * size, size));
      }
      callables.add(new Reader(reg, "my-value"));
      for (int i = 11; i <= 20; i++) {
         callables.add(new Register(reg, "Foo", i * size, size));
      }
      callables.add(new Reader(reg, "my-value"));
      
      List<Future<Boolean>> results = es.invokeAll(callables);
      for (Future<Boolean> f : results) {
         assertTrue(f.get(10, TimeUnit.SECONDS));
      }
      es.shutdown();
   }
   
   private class Register implements Callable<Boolean> {
      private final EndpointRegistry reg;
      private final String name;
      private final int base;
      private final int n;
      
      public Register(EndpointRegistry reg, String name, int base, int n) {
         this.reg = reg;
         this.name = name;
         this.base = base;
         this.n = n;
      }

      @Override
      public Boolean call() throws Exception
      {
         for (int i = base; i < (base + n); i++) {
            try {
               Endpoint ep = getTestEndpoint(name + i, "prop", "v" + i);
               reg.register(ep);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         }
         return true;
      }
   }
   
   private class Reader implements Callable<Boolean> {
      private final EndpointRegistry reg;
      private final String value;
      
      public Reader(EndpointRegistry reg, String value) {
         this.reg = reg;
         this.value = value;
      }

      @Override
      public Boolean call() throws Exception
      {
         boolean result = false;
         //below is what org.jboss.wsf.stack.cxf.transport.ServletHelper does for instance
         for (ObjectName on : reg.getEndpoints()) {
            if (on.getKeyProperty("prop").equalsIgnoreCase(value)) {
               result = true; //do not early exit
            }
         }
         return result;
      }
      
   }
   
   private static void checkContents(Iterator<ObjectName> it, int n) {
      List<String> values = new LinkedList<String>();
      for (int i = 0; i < n; i++) {
         values.add("v" + i);
      }
      while (it.hasNext()) {
         String p = it.next().getKeyProperty("prop");
         if (values.contains(p)) {
            values.remove(p);
         }
      }
      assertTrue("Could not find " + values, values.isEmpty());
   }
   
   private static Endpoint getTestEndpoint(String name, String prop, String value) throws Exception {
      final ObjectName objName = new ObjectName(name, prop, value);
      return new Endpoint()
      {
         
         @Override
         public void setProperty(String key, Object value)
         {
            
         }
         
         @Override
         public void setProperties(Map<String, Object> props)
         {
            
         }
         
         @Override
         public void removeProperty(String key)
         {
            
         }
         
         @Override
         public <T> T removeAttachment(Class<T> key)
         {
            return null;
         }
         
         @Override
         public Object getProperty(String key)
         {
            return null;
         }
         
         @Override
         public Set<String> getProperties()
         {
            return null;
         }
         
         @Override
         public <T> Collection<T> getAttachments()
         {
            return null;
         }
         
         @Override
         public <T> T getAttachment(Class<T> key)
         {
            return null;
         }
         
         @Override
         public <T> T addAttachment(Class<T> key, Object value)
         {
            return null;
         }
         
         @Override
         public void setType(EndpointType type)
         {
            
         }
         
         @Override
         public void setTargetBeanName(String epImpl)
         {
            
         }
         
         @Override
         public void setState(EndpointState state)
         {
            
         }
         
         @Override
         public void setShortName(String shortName)
         {
            
         }
         
         @Override
         public void setService(Service service)
         {
            
         }
         
         @Override
         public void setSecurityDomainContext(SecurityDomainContext context)
         {
            
         }
         
         @Override
         public void setRequestHandler(RequestHandler handler)
         {
            
         }
         
         @Override
         public void setRecordProcessors(List<RecordProcessor> recordProcessors)
         {
            
         }
         
         @Override
         public void setName(ObjectName epName)
         {
            
         }
         
         @Override
         public void setLifecycleHandler(LifecycleHandler handler)
         {
            
         }
         
         @Override
         public void setInvocationHandler(InvocationHandler invoker)
         {
            
         }
         
         @Override
         public void setInstanceProvider(InstanceProvider provider)
         {
            
         }
         
         @Override
         public void setEndpointMetrics(EndpointMetrics metrics)
         {
            
         }
         
         @Override
         public void setAddress(String address)
         {
            
         }
         
         @Override
         public void processRecord(Record record)
         {
            
         }
         
         @Override
         public EndpointType getType()
         {
            return null;
         }
         
         @Override
         public String getTargetBeanName()
         {
            return null;
         }
         
         @Override
         public Class getTargetBeanClass()
         {
            return null;
         }
         
         @Override
         public EndpointState getState()
         {
            return null;
         }
         
         @Override
         public String getShortName()
         {
            return null;
         }
         
         @Override
         public Service getService()
         {
            return null;
         }
         
         @Override
         public SecurityDomainContext getSecurityDomainContext()
         {
            return null;
         }
         
         @Override
         public RequestHandler getRequestHandler()
         {
            return null;
         }
         
         @Override
         public List<RecordProcessor> getRecordProcessors()
         {
            return null;
         }
         
         @Override
         public ObjectName getName()
         {
            return objName;
         }
         
         @Override
         public LifecycleHandler getLifecycleHandler()
         {
            return null;
         }
         
         @Override
         public InvocationHandler getInvocationHandler()
         {
            return null;
         }
         
         @Override
         public InstanceProvider getInstanceProvider()
         {
            return null;
         }
         
         @Override
         public EndpointMetrics getEndpointMetrics()
         {
            return null;
         }
         
         @Override
         public String getAddress()
         {
            return null;
         }
      };
   }
}
