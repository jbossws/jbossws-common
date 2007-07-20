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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentState;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;

/**
 * A general service deployment manger.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class BasicDeploymentAspectManager implements DeploymentAspectManager
{
   // provide logging
   private static final Logger log = Logger.getLogger(BasicDeploymentAspectManager.class);

   private String name;
   private Set<DeploymentAspect> unsortedAspects = new HashSet<DeploymentAspect>();
   private List<DeploymentAspect> sortedAspects;

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
      if (sortedAspects == null)
      {
         sortedAspects = new ArrayList<DeploymentAspect>();
         List<DeploymentAspect> allAspects = new ArrayList<DeploymentAspect>(unsortedAspects);

         // Add aspects with no requirements first
         Iterator<DeploymentAspect> itAll = allAspects.iterator();
         while (itAll.hasNext())
         {
            DeploymentAspect aspect = itAll.next();
            if (aspect.getRequires() == null)
            {
               sortedAspects.add(aspect);
               itAll.remove();
            }
         }

         // Add aspects that have requirements that already added aspects provide
         itAll = allAspects.iterator();
         while (itAll.hasNext())
         {
            DeploymentAspect aspect = itAll.next();
            int index = getAspectIndex(aspect);
            if (index != -1)
            {
               sortedAspects.add(index, aspect);
               itAll.remove();

               itAll = allAspects.iterator();
            }
         }

         // Add LAST_DEPLOYMENT_ASPECT
         itAll = allAspects.iterator();
         while (itAll.hasNext())
         {
            DeploymentAspect aspect = itAll.next();
            if (LAST_DEPLOYMENT_ASPECT.equals(aspect.getRequires()))
            {
               sortedAspects.add(aspect);
               itAll.remove();
            }
         }

         if (allAspects.size() != 0)
         {
            Set<String> providedConditions = new HashSet<String>();
            for (int i = 0; i < sortedAspects.size(); i++)
            {
               DeploymentAspect sortedAspect = sortedAspects.get(i);
               providedConditions.addAll(sortedAspect.getProvidesAsSet());
            }

            throw new IllegalStateException("Cannot add: " + allAspects + "\n provided: " + providedConditions);
         }

         for (DeploymentAspect aspect : sortedAspects)
            log.debug(name + ": " + aspect);
      }

      return sortedAspects;
   }

   private int getAspectIndex(DeploymentAspect aspect)
   {
      int index = -1;
      Set<String> providedConditions = new HashSet<String>();
      for (int i = 0; i < sortedAspects.size(); i++)
      {
         DeploymentAspect sortedAspect = sortedAspects.get(i);
         providedConditions.addAll(sortedAspect.getProvidesAsSet());
         if (providedConditions.containsAll(aspect.getRequiresAsSet()))
         {
            index = i + 1;
            break;
         }
      }
      return index;
   }

   public void addDeploymentAspect(DeploymentAspect aspect)
   {
      if (sortedAspects != null)
         throw new IllegalStateException("Cannot add deployment aspects to an already sorted list: " + sortedAspects);

      unsortedAspects.add(aspect);
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
         requiredSet.remove(LAST_DEPLOYMENT_ASPECT);
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
