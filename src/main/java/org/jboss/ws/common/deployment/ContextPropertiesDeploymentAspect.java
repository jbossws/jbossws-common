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
