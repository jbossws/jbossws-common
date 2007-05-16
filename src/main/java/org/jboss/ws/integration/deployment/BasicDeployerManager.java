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
package org.jboss.ws.integration.deployment;

// $Id$

import java.util.LinkedList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.ws.integration.deployment.Deployment.DeploymentState;

/**
 * A general service deployment manger.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class BasicDeployerManager implements DeployerManager
{
   // provide logging
   private static final Logger log = Logger.getLogger(BasicDeployerManager.class);
   
   
   private List<Deployer> deployers = new LinkedList<Deployer>();

   public List<Deployer> getDeployers()
   {
      return deployers;
   }

   public void setDeployers(List<Deployer> deployers)
   {
      this.deployers = deployers;
   }

   /**
    * Iterate over the registered deployers calls create on each.
    * Iterate over the registered deployers again and calls start on each.
    * If start fails it automaticall calls destroy in the reverse order 
    * starting with the deployer that failed
    */
   public void deploy(Deployment dep)
   {
      // create the deployment
      for (int i = 0; i < deployers.size(); i++)
      {
         Deployer deployer = deployers.get(i);
         logInvocation(deployer, "Create");
         deployer.create(dep);
      }
      
      dep.setState(DeploymentState.CREATED);

      // start the deployment
      for (int i = 0; i < deployers.size(); i++)
      {
         Deployer deployer = deployers.get(i);
         try
         {
            logInvocation(deployer, "Start");
            deployer.start(dep);
         }
         catch (RuntimeException rte)
         {
            while (i-- >= 0)
            {
               // destroy the deployment
               failsafeDestroy(deployer, dep);
            }
            throw rte;
         }
      }
      
      dep.setState(DeploymentState.STARTED);
   }

   public void undeploy(Deployment dep)
   {
      // stop the deployment
      for (int i = deployers.size(); 0 < i; i--)
      {
         Deployer deployer = deployers.get(i - 1);
         failsafeStop(deployer, dep);
      }
      
      dep.setState(DeploymentState.STOPED);
      
      // destroy the deployment
      for (int i = deployers.size(); 0 < i; i--)
      {
         Deployer deployer = deployers.get(i - 1);
         failsafeDestroy(deployer, dep);
      }

      dep.setState(DeploymentState.DESTROYED);
   }

   private void failsafeStop(Deployer deployer, Deployment dep)
   {
      try
      {
         logInvocation(deployer, "Stop");
         deployer.stop(dep);
      }
      catch (RuntimeException rte)
      {
         WSDeploymentException.rethrow(rte);
      }
   }

   private void failsafeDestroy(Deployer deployer, Deployment dep)
   {
      try
      {
         logInvocation(deployer, "Destroy");
         deployer.destroy(dep);
      }
      catch (RuntimeException rte)
      {
         WSDeploymentException.rethrow(rte);
      }
   }

   private void logInvocation(Deployer deployer, String method)
   {
      String name = deployer.getClass().getName();
      name = name.substring(name.lastIndexOf(".") + 1);
      log.debug(name + ":" + method);
   }
}
