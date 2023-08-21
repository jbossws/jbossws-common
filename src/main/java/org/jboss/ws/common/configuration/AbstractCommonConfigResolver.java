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
import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;

import org.jboss.ws.common.Messages;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;


/** 
 * Base class for resolving common configs
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-Feb-2015
 */
public abstract class AbstractCommonConfigResolver {
   
   /**
    * Returns the EndpointConfig resolved for the current endpoint
    * 
    * @return the EndpointConfig resolved for the current endpoint
    */
   public EndpointConfig resolveEndpointConfig() {
      final String endpointClassName = getEndpointClassName();
      // 1) default values
      //String configName = org.jboss.wsf.spi.metadata.config.EndpointConfig.STANDARD_ENDPOINT_CONFIG;
      String configName = endpointClassName;
      String configFile = EndpointConfig.DEFAULT_ENDPOINT_CONFIG_FILE;
      boolean specifiedConfig = false;
      // 2) annotation contribution
      if (isEndpointClassAnnotated(org.jboss.ws.api.annotation.EndpointConfig.class))
      {
         final String cfgName = getEndpointConfigNameFromAnnotation();
         if (cfgName != null && !cfgName.isEmpty())
         {
            configName = cfgName;
         }
         final String cfgFile = getEndpointConfigFileFromAnnotation();
         if (cfgFile != null && !cfgFile.isEmpty())
         {
            configFile = cfgFile;
         }
         specifiedConfig = true;
      }
      // 3) descriptor overrides (jboss-webservices.xml or web.xml)
      final String epCfgNameOverride = getEndpointConfigNameOverride();
      if (epCfgNameOverride != null && !epCfgNameOverride.isEmpty())
      {
         configName = epCfgNameOverride;
         specifiedConfig = true;
      }
      final String epCfgFileOverride = getEndpointConfigFileOverride();
      if (epCfgFileOverride != null && !epCfgFileOverride.isEmpty())
      {
         configFile = epCfgFileOverride;
      }
      // 4) setup of configuration
      if (configFile != EndpointConfig.DEFAULT_ENDPOINT_CONFIG_FILE) {
         //look for provided endpoint config file
         try
         {
            ConfigRoot configRoot = ConfigMetaDataParser.parse(getConfigFile(configFile));
            EndpointConfig config = configRoot.getEndpointConfigByName(configName);
            if (config == null && !specifiedConfig) {
               config = configRoot.getEndpointConfigByName(EndpointConfig.STANDARD_ENDPOINT_CONFIG);
            }
            if (config != null) {
               return config;
            }
         }
         catch (IOException e)
         {
            throw Messages.MESSAGES.couldNotReadConfigFile(configFile);
         }
      }
      else
      {
         EndpointConfig config = null;
         URL url = getDefaultConfigFile(configFile);
         if (url != null) {
            //the default file exists
            try
            {
               ConfigRoot configRoot = ConfigMetaDataParser.parse(url);
               config = configRoot.getEndpointConfigByName(configName);
               if (config == null && !specifiedConfig) {
                  config = configRoot.getEndpointConfigByName(EndpointConfig.STANDARD_ENDPOINT_CONFIG);
               }
            }
            catch (IOException e)
            {
               throw Messages.MESSAGES.couldNotReadConfigFile(configFile);
            }
         }
         if (config == null) {
            //use endpoint configs from AS domain
            ServerConfig sc = getServerConfig();
            config = sc.getEndpointConfig(configName);
            if (config == null && !specifiedConfig) {
               config = sc.getEndpointConfig(EndpointConfig.STANDARD_ENDPOINT_CONFIG);
            }
            if (config == null && specifiedConfig) {
                throw Messages.MESSAGES.couldNotFindEndpointConfigName(configName);
            }
         }
         if (config != null) {
            return config;
         } 
      }
      return null;
   }
   
   /**
    * Returns a set of full qualified class names of the handlers from the specified endpoint config
    * 
    * @param    The config to get the handler class names of
    * @return   A set of full qualified class names of the handlers from the specified endpoint config
    */
   public Set<String> getAllHandlers(EndpointConfig config) {
      Set<String> set = new HashSet<String>();
      if (config != null) {
         for (UnifiedHandlerChainMetaData uhcmd : config.getPreHandlerChains()) {
            for (UnifiedHandlerMetaData uhmd : uhcmd.getHandlers()) {
               set.add(uhmd.getHandlerClass());
            }
         }
         for (UnifiedHandlerChainMetaData uhcmd : config.getPostHandlerChains()) {
            for (UnifiedHandlerMetaData uhmd : uhcmd.getHandlers()) {
               set.add(uhmd.getHandlerClass());
            }
         }
      }
      return set;
   }
   
   /**
    * Gets the FQN of the endpoint class whose config is to be resolved by this class
    * 
    * @return   the FQN of the endpoint class
    */
   protected abstract String getEndpointClassName();
   
   /**
    * Returns true or false depending on the current endpoint class being annotated or
    * not with the specified annotation.
    * 
    * @param annotation     The annotation to look for
    * @return               True if the endpoint is annotated with the specified annotation,
    *                       false otherwise.
    */
   protected abstract <T extends Annotation> boolean isEndpointClassAnnotated(Class<T> annotation);
   
   /**
    * Returns the config name specified on the @EndpointConfig annotation (if any) on
    * the current endpoint
    * 
    * @return   The config name in the @EndpointConfig annotation on the endpoint
    */
   protected abstract String getEndpointConfigNameFromAnnotation();
   
   /**
    * Returns the config file specified on the @EndpointConfig annotation (if any) on
    * the current endpoint
    * 
    * @return   The config file in the @EndpointConfig annotation on the endpoint
    */
   protected abstract String getEndpointConfigFileFromAnnotation();
   
   /**
    * Returns the config name value coming from deployment descriptor
    * 
    * @return   The config name value from deployment descriptor
    */
   protected abstract String getEndpointConfigNameOverride();
   
   /**
    * Returns the config file value coming from deployment descriptor
    * 
    * @return   The config file value from deployment descriptor
    */
   protected abstract String getEndpointConfigFileOverride();
   
   /**
    * Returns the URL of the config file for the given name; this can either be in the root of the deployment
    * or at the same level of the endpoint class file (a sibling of it)
    * 
    * @param configFileName     The name of the config file to look for
    * @return                   the URL of the config file or null if it's not found
    */
   protected abstract URL getConfigFile(String configFileName) throws IOException;
   
   /**
    * Returns the URL of the specified default config file; this can either be in the root of the deployment
    * or at the same level of the endpoint class file (a sibling of it)
    * 
    * @param defaultConfigFileName      The name of the config file to look for
    * @return                           the URL of the config file or null if it's not found
    */
   protected abstract URL getDefaultConfigFile(String defaultConfigFileName);
   
   protected ServerConfig getServerConfig() {
      if(System.getSecurityManager() == null) {
         return AbstractServerConfig.getServerIntegrationServerConfig();
      }
      return AccessController.doPrivileged(AbstractServerConfig.GET_SERVER_INTEGRATION_SERVER_CONFIG);
   }
}
