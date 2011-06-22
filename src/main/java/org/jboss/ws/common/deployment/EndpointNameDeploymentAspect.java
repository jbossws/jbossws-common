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
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.MDBMetaData;

/**
 * A deployer that assigns the complete name to the Endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class EndpointNameDeploymentAspect extends AbstractDeploymentAspect
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointNameDeploymentAspect.class);
   @Override
   public void start(Deployment dep)
   {
      String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null || contextRoot.startsWith("/") == false)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "CONTEXT_ROOT_EXPECTED_TO_START_WITH_LEADING_SLASH",  contextRoot));

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
