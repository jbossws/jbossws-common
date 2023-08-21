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

import org.jboss.ws.common.Messages;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A deployer that assigns the complete name to the Endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class EndpointNameDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      final String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null || contextRoot.startsWith("/") == false)
         throw Messages.MESSAGES.contextRootExpectedToStartWithLeadingSlash(contextRoot);

      for (Endpoint ep : dep.getService().getEndpoints())
      {
         final StringBuilder name = new StringBuilder(Endpoint.SEPID_DOMAIN + ":");
         name.append(Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextRoot.substring(1) + ",");
         name.append(Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + ep.getShortName());
         ep.setName(ObjectNameFactory.create(name.toString()));
      }
   }

}
