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
package org.jboss.wsf.framework.deployment;

import org.jboss.wsf.framework.DefaultExtensible;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Service;

//$Id$

/**
 * A general web service deployment dep. 
 * 
 * It has no notion of J2EE deployment packages. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class DefaultDeployment extends DefaultExtensible implements Deployment
{
   // The name for this deployment
   private String simpleName;
   // A deployment has one service
   private Service service;
   // The type of this deployment
   private DeploymentType type;
   // The state for this deployment
   private DeploymentState state;
   // The deployment class loader
   private ClassLoader initialLoader;
   // The runtime class loader
   private ClassLoader runtimeLoader;

   DefaultDeployment(String name, ClassLoader classLoader)
   {
      simpleName = name;
      state = DeploymentState.UNDEFINED;
      initialLoader = classLoader;
      setService(new DefaultService());
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   public void setSimpleName(String name)
   {
      this.simpleName = name;
   }

   public void setInitialClassLoader(ClassLoader classLoader)
   {
      this.initialLoader = classLoader;
   }

   public ClassLoader getInitialClassLoader()
   {
      return initialLoader;
   }

   public void setRuntimeClassLoader(ClassLoader classLoader)
   {
      this.runtimeLoader = classLoader;
   }

   public ClassLoader getRuntimeClassLoader()
   {
      return runtimeLoader;
   }

   public Service getService()
   {
      return service;
   }

   public void setService(Service service)
   {
      service.setDeployment(this);
      this.service = service;
   }

   public DeploymentState getState()
   {
      return state;
   }

   public void setState(DeploymentState deploymentState)
   {
      this.state = deploymentState;
   }

   public DeploymentType getType()
   {
      return type;
   }

   public void setType(DeploymentType deploymentType)
   {
      this.type = deploymentType;
   }
}
