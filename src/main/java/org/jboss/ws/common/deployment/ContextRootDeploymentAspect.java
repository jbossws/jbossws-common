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
package org.jboss.ws.common.deployment;

import org.jboss.ws.Constants;
import org.jboss.ws.api.annotation.AuthMethod;
import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;
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
            Class implClass = ep.getTargetBeanClass();
            WebContext anWebContext = (WebContext)implClass.getAnnotation(WebContext.class);
            this.validateSecuritySettings(anWebContext);
            if (anWebContext != null && anWebContext.contextRoot().length() > 0)
            {
               if (contextRoot != null && contextRoot.equals(anWebContext.contextRoot()) == false)
                  throw new IllegalStateException("Context root must be the same for all deployed endpoints");

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
      if (dep.getParent() != null && Constants.BC_CONTEXT_MODE)
      {
         simpleName = dep.getParent().getSimpleName();
         contextRoot = simpleName.substring(0, simpleName.length() - 4) + "-" + contextRoot;
      }
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
