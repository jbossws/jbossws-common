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

import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.ResourceLoaderAdapter;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * A deployment aspect for JAXWS Endpoint API endpoints. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Jul-2007
 */
// TODO: [JBWS-2674] review this deployment aspect once AS IL is rewritten
public class EndpointAPIDeploymentAspect extends AbstractDeploymentAspect
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointAPIDeploymentAspect.class);
   @Override
   public void start(Deployment dep)
   {
      dep.setType(DeploymentType.JAXWS_JSE);

      if (dep instanceof ArchiveDeployment)
      {
         ArchiveDeployment arc = (ArchiveDeployment)dep;
         UnifiedVirtualFile rootFile = arc.getRootFile();
         if (rootFile == null)
         {
            rootFile = new ResourceLoaderAdapter(dep.getInitialClassLoader());
            arc.setRootFile(rootFile);
         }
      }
      
      ClassLoader rtcl = dep.getRuntimeClassLoader();
      if (rtcl == null)
      {
         // TODO: What's this? Look's quiet hacky...
         log.warn(BundleUtils.getMessage(bundle, "USING_INITAL_CLASS_LAODER_AS_RUNTIME_LAODER"),  new IllegalArgumentException());
         dep.setRuntimeClassLoader(dep.getInitialClassLoader());
      }

      for (Endpoint ep : dep.getService().getEndpoints())
      {
         if (ep.getShortName() == null)
         {
            String name = ep.getTargetBeanName();
            String shortName = name.substring(name.lastIndexOf('.') + 1);
            ep.setShortName(shortName);
         }
         
         if (((HttpEndpoint)ep).getURLPattern() == null)
         {
            ((HttpEndpoint)ep).setURLPattern("/*");
         }
      }
   }
}
