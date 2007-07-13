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
package org.jboss.wsf.spi.deployment;

//$Id: EndpointHandlerDeployer.java 3828 2007-07-09 16:56:38Z heiko.braun@jboss.com $

import java.util.HashMap;
import java.util.Map;

import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedMessageDrivenMetaData;

/**
 * A deployer that assigns the handlers to the Endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class EndpointHandlerDeploymentAspect extends DeploymentAspect
{
   private String requestHandler;
   private String lifecycleHandler;

   private Map<String, String> invocationHandlerMap = new HashMap<String, String>();
   
   public void setLifecycleHandler(String handler)
   {
      this.lifecycleHandler = handler;
   }

   public void setRequestHandler(String handler)
   {
      this.requestHandler = handler;
   }

   public void setInvocationHandler(Map<String, String> handlers)
   {
      this.invocationHandlerMap = handlers;
   }

   @Override
   public void create(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         if (requestHandler != null)
            ep.setRequestHandler(getRequestHandler(dep));
         
         if (lifecycleHandler != null)
            ep.setLifecycleHandler(getLifecycleHandler(dep));
         
         if (invocationHandlerMap != null)
         {
            InvocationHandler invocationHandler = getInvocationHandler(ep);
            if (invocationHandler != null)
               ep.setInvocationHandler(invocationHandler);
         }
      }
   }

   private RequestHandler getRequestHandler(Deployment dep)
   {
      try
      {
         Class<?> handlerClass = dep.getClassLoader().loadClass(requestHandler);
         return (RequestHandler)handlerClass.newInstance();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Cannot load request handler: " + requestHandler);
      }
   }

   private LifecycleHandler getLifecycleHandler(Deployment dep)
   {
      try
      {
         Class<?> handlerClass = dep.getClassLoader().loadClass(lifecycleHandler);
         return (LifecycleHandler)handlerClass.newInstance();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Cannot load lifecycle handler: " + lifecycleHandler);
      }
   }

   private InvocationHandler getInvocationHandler(Endpoint ep)
   {
      Deployment dep = ep.getService().getDeployment();
      String key = dep.getType().toString();
      
      // Use a special key for MDB endpoints
      UnifiedApplicationMetaData uapp = dep.getContext().getAttachment(UnifiedApplicationMetaData.class);
      if (uapp != null)
      {
         UnifiedBeanMetaData bmd = uapp.getBeanByEjbName(ep.getShortName());
         if (bmd instanceof UnifiedMessageDrivenMetaData)
         {
            key = "JAXRPC_MDB21";
         }
      }
      
      InvocationHandler invocationHandler = null;
      String className = invocationHandlerMap.get(key);
      if (className != null)
      {
         try
         {
            Class<?> handlerClass = dep.getClassLoader().loadClass(className);
            invocationHandler = (InvocationHandler)handlerClass.newInstance();
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Cannot load invocation handler: " + className);
         }
      }
      return invocationHandler;
   }
}