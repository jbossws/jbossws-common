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

import java.util.StringTokenizer;

import org.jboss.ws.common.Messages;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;

/**
 * A deployer that assigns the context root to the service.
 * 
 * If there is no explicit <context-root>, this deployer uses 
 * the first token from the <port-component-uri> element.  
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class BackwardCompatibleContextRootDeploymentAspect extends ContextRootDeploymentAspect
{
   @Override
   protected String getExplicitContextRoot(Deployment dep)
   {
      String contextRoot = super.getExplicitContextRoot(dep);
      if (contextRoot == null)
      {
         for (Endpoint ep : dep.getService().getEndpoints())
         {
            String urlPattern = getUrlPattern(dep, ep);
            if (urlPattern != null)
            {
               StringTokenizer st = new StringTokenizer(urlPattern, "/");
               if (st.countTokens() > 1)
               {
                  String firstToken = st.nextToken();
                  if (contextRoot != null && contextRoot.equals(firstToken) == false)
                     throw Messages.MESSAGES.allEndpointsMustShareSameContextRoot(dep.getSimpleName());

                  contextRoot = firstToken;
               }
            }
         }
      }
      return contextRoot;
   }

   private String getUrlPattern(Deployment dep, Endpoint ep)
   {
      String urlPattern = null;

      EJBArchiveMetaData appMetaData = dep.getAttachment(EJBArchiveMetaData.class);
      if (appMetaData != null && appMetaData.getBeanByEjbName(ep.getShortName()) != null)
      {
         EJBMetaData bmd = appMetaData.getBeanByEjbName(ep.getShortName());
         urlPattern = bmd.getPortComponentURI();
      }

      return urlPattern;
   }
}
