/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
