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

import org.jboss.ws.integration.UnifiedVirtualFile;


// $Id$


/**
 * A general web service deployment dep. 
 * 
 * It has no notion of J2EE deployment packages. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public interface Deployment
{
   public enum DeploymentType
   {
      JAXRPC_CLIENT, JAXRPC_JSE, JAXRPC_EJB21, JAXRPC_EJB3, JAXWS_JSE, JAXWS_EJB3
   };
   
   public enum DeploymentState
   {
      UNDEFINED, CREATED, STARTED, STOPED, DESTROYED
   };
   
   /** Get the root file for this deployment */
   UnifiedVirtualFile getRootFile();
   
   /** Set the root file for this deployment */
   void setRootFile(UnifiedVirtualFile root);
   
   /** Get the class loader for this deployment */
   ClassLoader getClassLoader();
   
   /** Set the class loader for this deployment */
   void setClassLoader(ClassLoader loader);
   
   /** Get the deployment context */
   DeploymentContext getContext();
   
   /** Set the deployment context */
   void setContext(DeploymentContext context);
   
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