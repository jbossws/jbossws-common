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
package org.jboss.ws.common.deployment;

import org.jboss.wsf.spi.deployment.AbstractExtensible;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Service;

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
}
