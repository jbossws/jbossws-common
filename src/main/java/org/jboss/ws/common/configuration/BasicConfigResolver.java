/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.configuration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;

import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;

/** 
 * A config resolver that operates on a JBossWS deployment
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Mar-2015
 */
public class BasicConfigResolver extends AbstractCommonConfigResolver
{
   private final UnifiedVirtualFile deploymentRoot;
   private final Class<?> implementorClass;
   private final String configNameOverride;
   private final String configFileOverride;
   private final EndpointConfig ann;
   
   public BasicConfigResolver(ArchiveDeployment dep, Class<?> implementorClass) {
      String epConfigName = null;
      String epConfigFile = null;
      JSEArchiveMetaData jsemd = dep.getAttachment(JSEArchiveMetaData.class);
      JBossWebservicesMetaData wsmd = dep.getAttachment(JBossWebservicesMetaData.class);
      //first check JSEArchiveMetaData as that has the actual merged value for POJO deployments
      if (jsemd != null) {
         epConfigName = jsemd.getConfigName();
         epConfigFile = jsemd.getConfigFile();
      } else if (wsmd != null) {
         epConfigName = wsmd.getConfigName();
         epConfigFile = wsmd.getConfigFile();
      }
      this.configNameOverride = epConfigName;
      this.configFileOverride = epConfigFile;
      this.implementorClass = implementorClass;
      this.deploymentRoot = dep.getRootFile();
      this.ann = implementorClass.getAnnotation(EndpointConfig.class);
   }

   @Override
   protected URL getDefaultConfigFile(String defaultConfigFileName)
   {
      URL url = implementorClass.getResource("/" + defaultConfigFileName);
      if (url == null)
      {
         UnifiedVirtualFile vf = deploymentRoot.findChildFailSafe(defaultConfigFileName);
         if (vf != null)
         {
            url = vf.toURL();
         }
      }
      return url;
   }

   @Override
   protected URL getConfigFile(String configFileName) throws IOException
   {
      UnifiedVirtualFile vf = deploymentRoot.findChild(configFileName);
      return vf.toURL();
   }

   @Override
   protected String getEndpointClassName()
   {
      return implementorClass.getName();
   }

   @Override
   protected <T extends Annotation> boolean isEndpointClassAnnotated(Class<T> annotation)
   {
      return ann != null;
   }

   @Override
   protected String getEndpointConfigNameFromAnnotation()
   {
      return ann.configName();
   }

   @Override
   protected String getEndpointConfigFileFromAnnotation()
   {
      return ann.configFile();
   }

   @Override
   protected String getEndpointConfigNameOverride()
   {
      return configNameOverride;
   }

   @Override
   protected String getEndpointConfigFileOverride()
   {
      return configFileOverride;
   }
}
