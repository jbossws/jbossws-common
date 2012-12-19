/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.jaxb.intros.BindingCustomizationFactory;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.ws.api.binding.JAXBBindingCustomization;
import org.jboss.ws.common.Loggers;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * Installs jaxbintro binding customization into the deployment
 * 
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 */
public class JAXBIntroDeploymentAspect extends AbstractDeploymentAspect
{
   private static final String META_INF_JAXB_INTROS_XML = "META-INF/jaxb-intros.xml";
   private static final String WEB_INF_JAXB_INTROS_XML = "WEB-INF/jaxb-intros.xml";

   public void start(Deployment deployment)
   {
      // assert ArchiveDeployment
      if(! (deployment instanceof ArchiveDeployment) )
      {
         if (Loggers.DEPLOYMENT_LOGGER.isTraceEnabled())
            Loggers.DEPLOYMENT_LOGGER.aspectDoesNotWorkOnDeployment(this.getClass(), deployment.getClass());
         return;
      }

      ArchiveDeployment archive = (ArchiveDeployment)deployment;
      InputStream introsConfigStream = null;

      URL url = null;
      try
      {
         // META-INF first
         UnifiedVirtualFile vfs = archive.getRootFile().findChildFailSafe(META_INF_JAXB_INTROS_XML);
         if (vfs != null) {
            url = vfs.toURL();
            introsConfigStream = url.openStream();
         }
      } catch (Exception e) {}

      if(null == introsConfigStream)
      {
         try 
         {
            // WEB-INF second
            UnifiedVirtualFile vfs = archive.getRootFile().findChildFailSafe(WEB_INF_JAXB_INTROS_XML);
            if (vfs != null) {
               url = vfs.toURL();
               introsConfigStream = url.openStream();
            }
         } catch (Exception e) {
            return;
         }
      }
      
      try
      {

         if(introsConfigStream != null)
         {
            BindingCustomization jaxbCustomizations = new JAXBBindingCustomization();
            BindingCustomizationFactory.populateBindingCustomization(introsConfigStream, jaxbCustomizations);
            
            // Add the customizations to the deployment too; later consumed by BusDeploymentAspect in CXF stack
            deployment.addAttachment(BindingCustomization.class, jaxbCustomizations);
            // JBossWSBeanConfigurer#configureService becomes the consumer later on
            for(Endpoint endpoint : deployment.getService().getEndpoints())
            {
               endpoint.addAttachment(BindingCustomization.class, jaxbCustomizations);
            }

         }

      }
      finally
      {
         if(introsConfigStream != null)
         {
            try {
               introsConfigStream.close();
            } catch (IOException e) {
               Loggers.DEPLOYMENT_LOGGER.errorClosingJAXBIntroConf(url, e);
            }
         }
      }
   }
}
