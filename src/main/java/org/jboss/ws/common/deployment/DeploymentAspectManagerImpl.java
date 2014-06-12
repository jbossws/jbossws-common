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

import static org.jboss.ws.common.Loggers.DEPLOYMENT_LOGGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;

/**
 * A general service deployment manger.
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class DeploymentAspectManagerImpl implements DeploymentAspectManager
{
   private String name;
   private final List<DeploymentAspect> depAspects = new ArrayList<DeploymentAspect>();

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
      return Collections.unmodifiableList(depAspects);
   }

   public void setDeploymentAspects(List<DeploymentAspect> aspects)
   {
      depAspects.clear();
      depAspects.addAll(aspects);

      if (DEPLOYMENT_LOGGER.isTraceEnabled())
      {
         // Debug the aspect list
         StringBuilder builder = new StringBuilder("setDeploymentAspects on " + name);
         for (DeploymentAspect aspect : aspects)
            builder.append("\n  " + aspect.getClass().getName() + " provides: " + aspect.getProvidesAsSet() + " requires: " + aspect.getRequiresAsSet());
   
         DEPLOYMENT_LOGGER.trace(builder);
      }
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
      final List<DeploymentAspect> deploymentAspects = getDeploymentAspects();
      for (int i = 0; i < deploymentAspects.size(); i++)
      {
         DeploymentAspect aspect = deploymentAspects.get(i);

         // Check that all required aspects are met 
         /*
          TODO: This should done by adding all provided conditions to the Deployment
          when being executed. Otherwise we will miss the parent provided conditions here
          
          Set<String> requiredSet = aspect.getRequiresAsSet();
          requiredSet.remove(DeploymentAspect.LAST_DEPLOYMENT_ASPECT);
          if (providedConditions.containsAll(requiredSet) == false)
          throw new IllegalStateException("Required conditions '" + aspect.getRequires() + "' not satisfied by '" + providedConditions + "' for: " + aspect);
          */

         providedConditions.addAll(aspect.getProvidesAsSet());
      }

      // start the deployment
      for (int i = 0; i < deploymentAspects.size(); i++)
      {
         DeploymentAspect aspect = deploymentAspects.get(i);
         try
         {
            logInvocation(aspect, "Start");
            ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
            try
            {
               SecurityActions.setContextClassLoader(aspect.getLoader());
               aspect.start(dep);
            }
            finally
            {
               SecurityActions.setContextClassLoader(origClassLoader);
            }
         }
         catch (RuntimeException rte)
         {
            DEPLOYMENT_LOGGER.errorDuringDeployment(dep.getSimpleName(), rte);
            while (--i >= 0)
            {
               // destroy the deployment
               try
               {
                  failsafeStop(deploymentAspects.get(i), dep);
               }
               catch (RuntimeException destroyRte)
               {
                  //log previous exception in the exotic case in which also stopping already started aspects fails
                  DEPLOYMENT_LOGGER.errorDestroyingDeployment(dep.getSimpleName(), rte);
                  throw destroyRte;
               }
            }
            throw rte;
         }
      }
   }

   public void undeploy(Deployment dep)
   {
      final List<DeploymentAspect> deploymentAspects = getDeploymentAspects();
      for (int i = deploymentAspects.size(); 0 < i; i--)
      {
         DeploymentAspect aspect = deploymentAspects.get(i - 1);
         failsafeStop(aspect, dep);
      }
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

   private void logInvocation(DeploymentAspect aspect, String method)
   {
      if (DEPLOYMENT_LOGGER.isTraceEnabled())
      {
         String name = aspect.getClass().getName();
         name = name.substring(name.lastIndexOf(".") + 1);
         DEPLOYMENT_LOGGER.trace("[" + this.name + "]" + name + ":" + method);
      }
   }

}
