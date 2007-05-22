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
package org.jboss.wsf.spi.deployment;

//$Id$

import org.jboss.wsf.spi.annotation.WebContext;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedEjbPortComponentMetaData;
import org.jboss.wsf.spi.metadata.j2ee.UnifiedWebMetaData;

/**
 * A deployer that assigns the URLPattern to endpoints. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2007
 */
public class URLPatternDeployer extends AbstractDeployer
{
   @Override
   public void create(Deployment dep)
   {
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         String urlPattern = getUrlPattern(dep, ep);
         ep.setURLPattern(urlPattern);
      }
   }

   private String getUrlPattern(Deployment dep, Endpoint ep)
   {
      String urlPattern = null;

      // #1 For JSE lookup the url-pattern from the servlet mappings 
      UnifiedWebMetaData webMetaData = dep.getContext().getAttachment(UnifiedWebMetaData.class);
      if (webMetaData != null)
      {
         String epName = ep.getShortName();
         urlPattern = webMetaData.getServletMappings().get(epName);
         if (urlPattern == null)
            throw new IllegalStateException("Cannot obtain servlet mapping for: " + epName);
      }

      // #2 Use the explicit urlPattern from port-component/port-component-uri
      UnifiedApplicationMetaData appMetaData = dep.getContext().getAttachment(UnifiedApplicationMetaData.class);
      if (appMetaData != null && appMetaData.getBeanByEjbName(ep.getShortName()) != null)
      {
         UnifiedBeanMetaData bmd = appMetaData.getBeanByEjbName(ep.getShortName());
         UnifiedEjbPortComponentMetaData pcmd = bmd.getPortComponent();
         if (pcmd != null)
         {
            urlPattern = pcmd.getPortComponentURI();
         }
      }
      
      // #3 For EJB use @WebContext.urlPattern 
      if (urlPattern == null)
      {
         Class beanClass = ep.getTargetBeanClass();
         WebContext anWebContext = (WebContext)beanClass.getAnnotation(WebContext.class);
         if (anWebContext != null && anWebContext.urlPattern().length() > 0)
            urlPattern = anWebContext.urlPattern();
      }

      // #4 Fallback to the ejb-name 
      if (urlPattern == null)
      {
         urlPattern = "/" + ep.getShortName();
      }

      return urlPattern;
   }
}