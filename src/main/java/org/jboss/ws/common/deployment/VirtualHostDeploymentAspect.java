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

import static org.jboss.ws.common.integration.WSHelper.isJaxwsEjbDeployment;

import java.util.ResourceBundle;

import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A deployment aspect that assigns the virtual host to a WS service. 
 *
 * @author darran.lofthouse@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class VirtualHostDeploymentAspect extends AbstractDeploymentAspect
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(VirtualHostDeploymentAspect.class);

   @Override
   public void start(Deployment dep)
   {
      if (isJaxwsEjbDeployment(dep))
      {
         dep.getService().setVirtualHost(getExplicitVirtualHost(dep));
      }
   }

   protected String getExplicitVirtualHost(final Deployment dep)
   {
      String virtualHost = null;

      for (final Endpoint ep : dep.getService().getEndpoints())
      {
         final Class<?> implClass = ep.getTargetBeanClass();
         final WebContext webContext = implClass.getAnnotation(WebContext.class);

         if (hasVirtualHost(webContext))
         {
            final String currentVirtualHost = webContext.virtualHost().trim();
            if (virtualHost == null)
            {
                virtualHost = currentVirtualHost;
            }
            else
            {
               if (!currentVirtualHost.equals(virtualHost))
               {
                  throw new IllegalStateException(BundleUtils.getMessage(bundle, "VIRTUALHOST_MUST_BE_THE_SAME_FOR_ALL_DEPLOYED_ENDPOINTS"));
               }
            }
         }
      }
      
      return virtualHost;
   }

   private static boolean hasVirtualHost(final WebContext webContext) {
       return webContext != null && webContext.virtualHost().trim().length() > 0;
   }

}
