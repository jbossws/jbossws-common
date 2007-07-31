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

// $Id$

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentState;

/**
 * A general service deployment manger.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class DeploymentAspectManagerImpl implements DeploymentAspectManager
{
   // provide logging
   private static final Logger log = Logger.getLogger(DeploymentAspectManagerImpl.class);

   private String name;
   private List<DeploymentAspect> sortedAspects = new ArrayList<DeploymentAspect>();
   private long deploymentCount;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public List<DeploymentAspect> getDeploymentAspects()
   {
      return Collections.unmodifiableList(sortedAspects);
   }

   public void setDeploymentAspects(List<DeploymentAspect> aspects)
   {
      if (deploymentCount > 0)
         throw new IllegalStateException("Cannot add deployment aspects");

      sortedAspects.clear();
      sortedAspects.addAll(aspects);
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
      Set<String> providedConditions = new HashSet<String>();
      for (int i = 0; i < getDeploymentAspects().size(); i++)
      {
         DeploymentAspect aspect = getDeploymentAspects().get(i);

         // Check that all required aspects are met 
         Set<String> requiredSet = aspect.getRequiresAsSet();
         requiredSet.remove(DeploymentAspect.LAST_DEPLOYMENT_ASPECT);
         if (providedConditions.containsAll(requiredSet) == false)
            throw new IllegalStateException("Required conditions '" + aspect.getRequires() + "' not satisfied by '" + providedConditions + "' for: " + aspect);

         logInvocation(aspect, "Create");
         aspect.create(dep);

         providedConditions.addAll(aspect.getProvidesAsSet());
      }

      dep.setState(DeploymentState.CREATED);

      // start the deployment
      for (int i = 0; i < getDeploymentAspects().size(); i++)
      {
         DeploymentAspect aspect = getDeploymentAspects().get(i);
         try
         {
            logInvocation(aspect, "Start");
            aspect.start(dep);
         }
         catch (RuntimeException rte)
         {
            while (i-- >= 0)
            {
               // destroy the deployment
               failsafeDestroy(aspect, dep);
            }
            throw rte;
         }
      }

      dep.setState(DeploymentState.STARTED);
      
      deploymentCount++;
   }

   public void undeploy(Deployment dep)
   {
      // stop the deployment
      for (int i = getDeploymentAspects().size(); 0 < i; i--)
      {
         DeploymentAspect aspect = getDeploymentAspects().get(i - 1);
         failsafeStop(aspect, dep);
      }

      dep.setState(DeploymentState.STOPPED);

      // destroy the deployment
      for (int i = getDeploymentAspects().size(); 0 < i; i--)
      {
         DeploymentAspect aspect = getDeploymentAspects().get(i - 1);
         failsafeDestroy(aspect, dep);
      }

      dep.setState(DeploymentState.DESTROYED);
   }

   private void failsafeStop(DeploymentAspect aspect, Deployment dep)
   {
      try
      {
         logInvocation(aspect, "Stop");
         aspect.stop(dep);
      }
      catch (RuntimeException rte)
      {
         WSFDeploymentException.rethrow(rte);
      }
   }

   private void failsafeDestroy(DeploymentAspect aspect, Deployment dep)
   {
      try
      {
         logInvocation(aspect, "Destroy");
         aspect.destroy(dep);
      }
      catch (RuntimeException rte)
      {
         WSFDeploymentException.rethrow(rte);
      }
   }

   private void logInvocation(DeploymentAspect aspect, String method)
   {
      String name = aspect.getClass().getName();
      name = name.substring(name.lastIndexOf(".") + 1);
      log.debug(name + ":" + method);
   }
}
