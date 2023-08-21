/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jboss.ws.common.deployment;

import static org.jboss.ws.common.integration.WSHelper.isJaxwsEjbDeployment;

import org.jboss.ws.api.annotation.WebContext;
import org.jboss.ws.common.Messages;
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
                  throw Messages.MESSAGES.virtualHostMustBeTheSameForAllEndpoints(dep.getSimpleName());
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
