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
package org.jboss.wsf.spi.deployment;

//$Id$

import org.jboss.wsf.spi.annotation.WebContext;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedWebMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;

/**
 * A deployer that assigns the context root to the service 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ContextRootDeployer extends AbstractDeployer
{
   @Override
   public void create(Deployment dep)
   {
      String contextRoot = null;
      
      // #1 Use the explicit context root from the web meta data
      UnifiedWebMetaData webMetaData = dep.getContext().getAttachment(UnifiedWebMetaData.class);
      if (webMetaData != null)
         contextRoot = webMetaData.getContextRoot();

      // #2 Use the explicit context root from @WebContext.contextRoot
      if (contextRoot == null)
      {
         for (Endpoint ep : dep.getService().getEndpoints())
         {
            Class implClass = ep.getTargetBeanClass();
            WebContext anWebContext = (WebContext)implClass.getAnnotation(WebContext.class);
            if (anWebContext != null && anWebContext.contextRoot().length() > 0)
            {
               if (contextRoot != null && contextRoot.equals(anWebContext.contextRoot()) == false)
                  throw new IllegalStateException("Context root must be the same for all deployed endpoints");

               contextRoot = anWebContext.contextRoot();
            }
         }
      }

      // #3 Use the explicit context root from webservices/context-root
      UnifiedApplicationMetaData appMetaData = dep.getContext().getAttachment(UnifiedApplicationMetaData.class);
      if (contextRoot == null && appMetaData != null)
      {
         contextRoot = appMetaData.getWebServiceContextRoot();
      }

      // #4 Use the implicit context root derived from the deployment name
      if (contextRoot == null)
      {
         UnifiedDeploymentInfo udi = dep.getContext().getAttachment(UnifiedDeploymentInfo.class);
         String simpleName = udi.simpleName;
         contextRoot = simpleName.substring(0, simpleName.length() - 4);
         if (udi.parent != null)
         {
            simpleName = udi.parent.simpleName;
            contextRoot = simpleName.substring(0, simpleName.length() - 4) + "-" + contextRoot;
         }
      }

      if (contextRoot.startsWith("/"))
         contextRoot = contextRoot.substring(1);

      dep.getService().setContextRoot(contextRoot);
   }
}