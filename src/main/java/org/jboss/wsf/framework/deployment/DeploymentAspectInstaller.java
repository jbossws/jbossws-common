/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.framework.deployment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;

/**
 * A deployment aspect installer.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class DeploymentAspectInstaller
{
   // provide logging
   private static final Logger log = Logger.getLogger(DeploymentAspectInstaller.class);

   private DeploymentAspectManager manager;
   private Set<DeploymentAspect> aspects;
   private boolean sortAspectsOnCreate;
   private Set<String> parentProvidedConditions = new HashSet<String>();

   public void setManager(DeploymentAspectManager manager)
   {
      this.manager = manager;
   }

   public void setAspects(Set<DeploymentAspect> aspects)
   {
      this.aspects = aspects;
   }

   public void setSortAspectsOnCreate(boolean sortAspectsOnCreate)
   {
      this.sortAspectsOnCreate = sortAspectsOnCreate;
   }

   public void create()
   {
      List<DeploymentAspect> unsortedAspects = new ArrayList<DeploymentAspect>();

      unsortedAspects.addAll(manager.getDeploymentAspects());
      unsortedAspects.addAll(aspects);

      List<DeploymentAspect> sortedAspects = unsortedAspects;
      if (sortAspectsOnCreate)
      {
         // get the conditions provided by the parent
         if (manager.getParent() != null)
         {
            Iterator<DeploymentAspect> it = manager.getParent().getDeploymentAspects().iterator();
            while (it.hasNext())
            {
               DeploymentAspect aspect = it.next();
               parentProvidedConditions.addAll(aspect.getProvidesAsSet());
            }
         }

         sortedAspects = new ArrayList<DeploymentAspect>();
         List<DeploymentAspect> allAspects = new ArrayList<DeploymentAspect>(unsortedAspects);

         // Add aspects with no requirements first 
         Iterator<DeploymentAspect> itAll = allAspects.iterator();
         while (itAll.hasNext())
         {
            DeploymentAspect aspect = itAll.next();
            if (aspect.getRequires() == null || parentProvidedConditions.containsAll(aspect.getRequiresAsSet()))
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
            int index = getAspectIndex(sortedAspects, aspect);
            if (index != -1)
            {
               sortedAspects.add(index, aspect);
               itAll.remove();

               itAll = allAspects.iterator(); // Hm,...
            }
         }

         // Add LAST_DEPLOYMENT_ASPECT
         itAll = allAspects.iterator();
         while (itAll.hasNext())
         {
            DeploymentAspect aspect = itAll.next();
            if (DeploymentAspect.LAST_DEPLOYMENT_ASPECT.equals(aspect.getRequires()))
            {
               sortedAspects.add(aspect);
               itAll.remove();
            }
         }

         if (allAspects.size() != 0)
            throwSortException(sortedAspects, allAspects);
      }
      manager.setDeploymentAspects(sortedAspects);
   }

   public void destroy()
   {
      List<DeploymentAspect> managerAspects = new ArrayList<DeploymentAspect>();
      managerAspects.addAll(manager.getDeploymentAspects());
      Iterator<DeploymentAspect> it = aspects.iterator();
      while (it.hasNext())
      {
         DeploymentAspect aspect = it.next();
         managerAspects.remove(aspect);
      }
      manager.setDeploymentAspects(managerAspects);
   }
   
   private void throwSortException(List<DeploymentAspect> sortedAspects, List<DeploymentAspect> allAspects)
   {
      Set<String> providedConditions = new HashSet<String>();
      for (int i = 0; i < sortedAspects.size(); i++)
      {
         DeploymentAspect sortedAspect = sortedAspects.get(i);
         providedConditions.addAll(sortedAspect.getProvidesAsSet());
      }

      String exmsg = "Cannot add deployment aspect(s)";
      StringBuilder str = new StringBuilder(exmsg);

      if (manager.getParent() != null)
         str.append("\n" + manager.getParent().getName() + " provides: " + parentProvidedConditions);
      
      str.append("\n" + manager.getName() + " provides: " + providedConditions);

      for (DeploymentAspect da : allAspects)
      {
         str.append("\n   " + da.getClass().getName() + ", requires: " + da.getRequires());
      }

      log.error(str);
      throw new IllegalStateException(str.toString());
   }

   private int getAspectIndex(List<DeploymentAspect> sortedAspects, DeploymentAspect aspect)
   {
      int index = -1;
      Set<String> providedConditions = new HashSet<String>(parentProvidedConditions);
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
}
