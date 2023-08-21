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
package org.jboss.ws.common.management;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.wsf.spi.management.CommonConfigStore;
import org.jboss.wsf.spi.metadata.config.AbstractCommonConfig;


/**
 * A implementation of the CommonConfigStore based on HashMap;
 * configs are stored using their name as key.
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Dec-2013
 */
public class CommonConfigStoreImpl<T extends AbstractCommonConfig> implements CommonConfigStore<T>
{
   private final Map<String, T> configs = new HashMap<String, T>(4);
   private volatile T wrapper;
   private volatile Map<String, T> loadedConfigs = Collections.emptyMap();

   @Override
   public synchronized void register(T config)
   {
      configs.put(config.getConfigName(), config);
   }

   @Override
   public synchronized void unregister(T config)
   {
      configs.remove(config.getConfigName());
   }
   
   @SuppressWarnings("unchecked")
   private T newInstance(T obj, T wrapper) {
      Class<?> clazz = obj.getClass();
      try {
         return (T)clazz.getConstructor(clazz, wrapper.getClass()).newInstance(obj, wrapper);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public synchronized void reload()
   {
      Map<String, T> map = new HashMap<String, T>(configs.size(), 1);
      if (wrapper != null) {
         for (Entry<String, T> e : configs.entrySet()) {
            map.put(e.getKey(), newInstance(e.getValue(), wrapper));
         }
      } else {
         for (Entry<String, T> e : configs.entrySet()) {
            map.put(e.getKey(), e.getValue());
         }
      }
      this.loadedConfigs = Collections.unmodifiableMap(map);
   }
   
   @Override
   public synchronized void unload()
   {
      this.loadedConfigs = Collections.emptyMap();
   }

   @Override
   public synchronized void setWrapperConfig(T config, boolean reload)
   {
      this.wrapper = config;
      if (reload) {
         reload();
      }
   }

   @Override
   public synchronized T getWrapperConfig()
   {
      return this.wrapper;
   }

   @Override
   public T getConfig(String name)
   {
      return this.loadedConfigs.get(name);
   }

   @Override
   public Collection<T> getConfigs()
   {
      return this.loadedConfigs.values();
   }
   
}
