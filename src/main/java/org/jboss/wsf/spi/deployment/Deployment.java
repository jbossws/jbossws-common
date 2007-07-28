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

import org.jboss.wsf.spi.Extensible;

// $Id$

/**
 * A general web service deployment dep. 
 * 
 * It has no notion of J2EE deployment packages. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public interface Deployment extends Extensible
{
   public enum DeploymentType
   {
      JAXRPC_CLIENT, JAXRPC_JSE, JAXRPC_EJB21, JAXRPC_EJB3, JAXWS_JSE, JAXWS_EJB3
   };
   
   public enum DeploymentState
   {
      UNDEFINED, CREATED, STARTED, STOPPED, DESTROYED
   };
   
   /** Get the identifier for this deployment */
   String getSimpleName();

   /** Set the identifier for this deployment */
   void setSimpleName(String name);

   /** Get the class loader for this deployment */
   ClassLoader getInitialClassLoader();
   
   /** Set the class loader for this deployment */
   void setInitialClassLoader(ClassLoader loader);
   
   /** Get the runtime class loader for this deployment */
   ClassLoader getRuntimeClassLoader();
   
   /** Set the runtime class loader for this deployment */
   void setRuntimeClassLoader(ClassLoader loader);
   
   /** Get the deployment type */
   DeploymentType getType();
   
   /** Set the deployment type */
   void setType(DeploymentType type);
   
   /** Get the current deployment state */
   DeploymentState getState();
   
   /** Set the current deployment state */
   void setState(DeploymentState type);

   /** Get the service assiated with this deployment */
   Service getService();

   /** Set the service assiated with this deployment */
   void setService(Service service);
}