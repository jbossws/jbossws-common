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
