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

   @SuppressWarnings("unchecked")
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
