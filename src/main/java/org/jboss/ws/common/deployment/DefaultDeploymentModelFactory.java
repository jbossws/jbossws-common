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

import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * @author Heiko.Braun@jboss.com
 *         Created: Jul 18, 2007
 */
public class DefaultDeploymentModelFactory extends DeploymentModelFactory
{
   public Deployment newDeployment(String simpleName, ClassLoader classLoader, UnifiedVirtualFile rootFile)
   {
      return new DefaultDeployment(simpleName, classLoader);
   }

   public Endpoint newHttpEndpoint(String targetBean)
   {
      return new DefaultHttpEndpoint(targetBean);
   }
   
   public Endpoint newJMSEndpoint(String targetBean) 
   {
      return new DefaultJMSEndpoint(targetBean);
   }

   public Deployment newDeployment(ArchiveDeployment parent, String simpleName, ClassLoader classLoader, UnifiedVirtualFile rootFile)
   {
      throw new UnsupportedOperationException();
   }
}
