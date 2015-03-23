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

import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.DeploymentType;

/**
 * A general web service deployment dep. 
 * 
 * It has no notion of J2EE deployment packages. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class DefaultDeployment extends AbstractExtensible implements Deployment
{
   // The name for this deployment
   private final String simpleName;
   // A deployment has one service
   private final Service service;
   // The runtime class loader
   private final ClassLoader classLoader;
   private DeploymentType deploymentType;

   DefaultDeployment(String name, ClassLoader classLoader)
   {
      super(12, 4);
      this.simpleName = name;
      this.classLoader = classLoader;
      this.service = new DefaultService(this);
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public Service getService()
   {
      return service;
   }

   public void setType(DeploymentType deploymentType) {
      this.deploymentType = deploymentType;
   }

   public DeploymentType getType() {
      return deploymentType;
   }
}
