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

package org.jboss.wsf.framework.deployment;

import java.util.Arrays;
import java.util.List;

import org.jboss.wsf.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.annotation.WebContext;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;

/**
 * A deployer that assigns the virtual hosts to the service 
 *
 * @author darran.lofthouse@jboss.com
 * @since 10-Jul-2008
 */
public class VirtualHostDeploymentAspect extends AbstractDeploymentAspect
{

   @Override
   public void start(Deployment dep)
   {
      if ( DeploymentType.JAXWS_EJB3.equals(dep.getType()))
      {
         dep.getService().setVirtualHosts(getExplicitVirtualHosts(dep));
      }
   }

   protected List<String> getExplicitVirtualHosts(Deployment dep)
   {
      String[] virtualHosts = null;

      // Use the virtual hosts from @WebContext.virtualHosts
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         Class implClass = ep.getTargetBeanClass();
         WebContext anWebContext = (WebContext)implClass.getAnnotation(WebContext.class);
         if (anWebContext != null && anWebContext.virtualHosts() != null && anWebContext.virtualHosts().length > 0)
         {
            String[] anVirtualHosts = anWebContext.virtualHosts();
            // Avoid modifying the annotation values.
            String[] temp = new String[anVirtualHosts.length];
            System.arraycopy(anVirtualHosts, 0, temp, 0, anVirtualHosts.length);
            Arrays.sort(temp);

            if (virtualHosts == null)
            {
               virtualHosts = temp;
            }
            else
            {
               if (Arrays.equals(virtualHosts, temp) == false)
               {
                  throw new IllegalStateException("virtualHosts must be the same for all deployed endpoints");
               }
            }
         }
      }
      
      if ( virtualHosts != null )
      {
         return Arrays.asList(virtualHosts);
      }
      else
      {
         return null;
      }
   }
}
