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
package org.jboss.wsf.framework.deployment;

//$Id$

import java.util.StringTokenizer;

import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;

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
                     throw new IllegalStateException("All endpoints must share the same <context-root>: " + contextRoot + "!=" + firstToken);

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