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

import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.MDBMetaData;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.common.ObjectNameFactory;

/**
 * A deployer that assigns the complete name to the Endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class EndpointNameDeploymentAspect extends DeploymentAspect
{
   @Override
   public void create(Deployment dep)
   {
      String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null || contextRoot.startsWith("/") == false)
         throw new IllegalStateException("Context root expected to start with leading slash: " + contextRoot);

      for (Endpoint ep : dep.getService().getEndpoints())
      {
         StringBuilder name = new StringBuilder(Endpoint.SEPID_DOMAIN + ":");
         name.append(Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextRoot.substring(1) + ",");
         name.append(Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + ep.getShortName());

         // Append the JMS destination, for an MDB endpoint
         EJBArchiveMetaData uapp = dep.getAttachment(EJBArchiveMetaData.class);
         if (uapp != null)
         {
            EJBMetaData bmd = uapp.getBeanByEjbName(ep.getShortName());
            if (bmd instanceof MDBMetaData)
            {
               String destName = ((MDBMetaData)bmd).getDestinationJndiName();
               name.append(",jms=" + destName);
            }
         }

         ep.setName(ObjectNameFactory.create(name.toString()));
      }
   }
}