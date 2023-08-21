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

import org.jboss.ws.api.annotation.AuthMethod;
import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.common.Messages;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;

/**
 * A deployer that assigns the context root to the service 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ContextRootDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null)
      {
         contextRoot = getExplicitContextRoot(dep);
         if (contextRoot == null)
            contextRoot = getImplicitContextRoot((ArchiveDeployment)dep);

         // Always prefix with '/'
         if (contextRoot.startsWith("/") == false)
            contextRoot = "/" + contextRoot;

         dep.getService().setContextRoot(contextRoot);
      }
   }

   protected String getExplicitContextRoot(Deployment dep)
   {
      String contextRoot = null;

      // #1 Use the explicit context root from the web meta data
      JSEArchiveMetaData webMetaData = dep.getAttachment(JSEArchiveMetaData.class);
      if (webMetaData != null)
         contextRoot = webMetaData.getContextRoot();

      // #2 Use the explicit context root from @WebContext.contextRoot
      if (contextRoot == null)
      {
         for (Endpoint ep : dep.getService().getEndpoints())
         {
            Class<?> implClass = ep.getTargetBeanClass();
            WebContext anWebContext = implClass.getAnnotation(WebContext.class);
            this.validateSecuritySettings(anWebContext);
            if (anWebContext != null && anWebContext.contextRoot().length() > 0)
            {
               if (contextRoot != null && contextRoot.equals(anWebContext.contextRoot()) == false)
                  throw Messages.MESSAGES.allEndpointsMustShareSameContextRoot(dep.getSimpleName());

               contextRoot = anWebContext.contextRoot();
            }
         }
      }

      // #3 Use the explicit context root from webservices/context-root
      EJBArchiveMetaData appMetaData = dep.getAttachment(EJBArchiveMetaData.class);
      if (contextRoot == null && appMetaData != null)
      {
         contextRoot = appMetaData.getWebServiceContextRoot();
      }

      return contextRoot;
   }

   /** Use the implicit context root derived from the deployment name
    */
   protected String getImplicitContextRoot(ArchiveDeployment dep)
   {
      String simpleName = dep.getSimpleName();
      String contextRoot = simpleName.substring(0, simpleName.length() - 4);
      return contextRoot;
   }
   
   private void validateSecuritySettings(WebContext webCtx)
   {
      if (webCtx != null)
      {
         TransportGuarantee.valueOf(webCtx.transportGuarantee());
         AuthMethod.valueOf(webCtx.authMethod());
      }
   }
}
