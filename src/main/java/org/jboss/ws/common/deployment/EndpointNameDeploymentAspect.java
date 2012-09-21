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
