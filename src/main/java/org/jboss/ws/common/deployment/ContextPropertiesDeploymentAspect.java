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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;

/**
 * Populate deployment context properties
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2006
 */
public class ContextPropertiesDeploymentAspect extends AbstractDeploymentAspect
{
   // The configured service endpoint servlet
   private Map<String,String> contextProperties;

   public Map<String, String> getContextProperties()
   {
      return contextProperties;
   }

   /**
    * This is called once at AS boot time during deployment aspect parsing;
    * this provided map is copied.
    * 
    * @param contextProperties
    */
   public void setContextProperties(Map<String, String> contextProperties)
   {
      if (contextProperties != null) {
         this.contextProperties = new HashMap<String, String>(4);
         this.contextProperties.putAll(contextProperties);
      }
   }

   @Override
   public void start(Deployment dep)
   {
      if (contextProperties != null)
      {
         Iterator<String> it = contextProperties.keySet().iterator();
         while (it.hasNext())
         {
            String key = it.next();
            String value = contextProperties.get(key);
            dep.setProperty(key, value);
         }
      }
   }
}
