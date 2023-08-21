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

import static org.jboss.ws.common.integration.WSHelper.isEjbEndpoint;
import static org.jboss.ws.common.integration.WSHelper.isJseEndpoint;

import org.jboss.ws.common.Messages;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.utils.UrlPatternUtils;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;

/**
 * A deployer that assigns the URLPattern to endpoints. 
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class URLPatternDeploymentAspect extends AbstractDeploymentAspect
{
   @Override
   public void start(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         if (ep instanceof HttpEndpoint)
         {
            HttpEndpoint httpEp = (HttpEndpoint)ep;
            String urlPattern = httpEp.getURLPattern();
            if (urlPattern == null)
            {
               urlPattern = getExplicitPattern(dep, ep);
               if (urlPattern == null) {
                  urlPattern = ep.getShortName();
               }
               // Always prefix with '/'
               httpEp.setURLPattern(UrlPatternUtils.getUrlPattern(urlPattern));
            }
         }
      }
   }

   protected String getExplicitPattern(Deployment dep, Endpoint ep)
   {
      String urlPattern = null;

      // #1 For JSE lookup the url-pattern from the servlet mappings 
      JSEArchiveMetaData webMetaData = dep.getAttachment(JSEArchiveMetaData.class);
      if (webMetaData != null && isJseEndpoint(ep))
      {
         String epName = ep.getShortName();
         urlPattern = webMetaData.getServletMappings().get(epName);
         if (urlPattern == null)
            throw Messages.MESSAGES.cannotObtainServletMapping(epName);
      }

      // #2 Use the explicit urlPattern from port-component/port-component-uri
      EJBArchiveMetaData appMetaData = dep.getAttachment(EJBArchiveMetaData.class);
      if (appMetaData != null && appMetaData.getBeanByEjbName(ep.getShortName()) != null && isEjbEndpoint(ep))
      {
         EJBMetaData bmd = appMetaData.getBeanByEjbName(ep.getShortName());
         urlPattern = UrlPatternUtils.getUrlPatternByPortComponentURI(bmd.getPortComponentURI(),
             dep.getService().getContextRoot());
      }

      // #3 For EJB use @WebContext.urlPattern
      if (urlPattern == null)
      {
         urlPattern = UrlPatternUtils.getUrlPatternByWebContext(ep.getTargetBeanClass());
      }
      
      // #4 Use @WebService
      if (urlPattern == null)
      {
          urlPattern = UrlPatternUtils.getUrlPatternByWebService(ep.getTargetBeanClass());
      }
      // TODO: WebServiceProvider ???
      
      // #5 Use simple class name
      if (urlPattern == null) 
      {
          urlPattern = UrlPatternUtils.getUrlPatternByClassname(ep.getTargetBeanClass());
      }

      return urlPattern;
   }

}
