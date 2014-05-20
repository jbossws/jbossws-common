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
package org.jboss.ws.common.integration;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;

/**
 * Abstract deployment aspect every other one extends
 * 
 * @author alessio.soldano@jboss.com
 * @since 21-Jan-2010
 *
 */
public class AbstractDeploymentAspect implements DeploymentAspect
{
   private String provides;
   private String requires;
   private int relativeOrder;
   private boolean isLast;
   private WeakReference<ClassLoader> loader;

   public AbstractDeploymentAspect()
   {
      this.loader = new WeakReference<ClassLoader>(SecurityActions.getContextClassLoader());
   }
   
   public ClassLoader getLoader()
   {
      return loader != null ? loader.get() : null;
   }
   
   public void setLast(boolean isLast)
   {
      this.isLast = isLast;
   }

   public boolean isLast()
   {
      return this.isLast;
   }

   public String getProvides()
   {
      return provides;
   }

   public void setProvides(String provides)
   {
      this.provides = provides;
   }

   public String getRequires()
   {
      return requires;
   }

   public void setRequires(String requires)
   {
      this.requires = requires;
   }

   public void setRelativeOrder(int relativeOrder)
   {
      this.relativeOrder = relativeOrder;
   }

   public int getRelativeOrder()
   {
      return this.relativeOrder;
   }

   public void start(Deployment dep)
   {
   }

   public void stop(Deployment dep)
   {
   }

   public Set<String> getProvidesAsSet()
   {
      Set<String> condset = new HashSet<String>();
      if (provides != null)
      {
         StringTokenizer st = new StringTokenizer(provides, ", \r\n\t");
         while (st.hasMoreTokens())
            condset.add(st.nextToken());
      }
      return condset;
   }

   public Set<String> getRequiresAsSet()
   {
      Set<String> condset = new HashSet<String>();
      if (requires != null)
      {
         StringTokenizer st = new StringTokenizer(requires, ", \r\n\t");
         while (st.hasMoreTokens())
            condset.add(st.nextToken());
      }
      return condset;
   }
}
