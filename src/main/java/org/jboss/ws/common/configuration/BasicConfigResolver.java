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
