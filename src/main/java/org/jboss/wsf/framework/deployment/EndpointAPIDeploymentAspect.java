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

//$Id: EndpointAddressDeploymentAspect.java 4018 2007-07-27 06:31:03Z thomas.diesler@jboss.com $

import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;

/**
 * A deployment aspect for JAXWS Endpoint API endpoints. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Jul-2007
 */
public class EndpointAPIDeploymentAspect extends DeploymentAspect
{
   @Override
   public void create(Deployment dep)
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
         dep.setRuntimeClassLoader(dep.getInitialClassLoader());

      for (Endpoint ep : dep.getService().getEndpoints())
      {
         if (ep.getShortName() == null)
         {
            String name = ep.getTargetBeanName();
            String shortName = name.substring(name.lastIndexOf('.') + 1);
            ep.setShortName(shortName);
         }
         
         if (ep.getURLPattern() == null)
         {
            ep.setURLPattern("/*");
         }
      }
   }
}