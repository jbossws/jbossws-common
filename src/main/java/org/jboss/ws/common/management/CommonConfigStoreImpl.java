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
