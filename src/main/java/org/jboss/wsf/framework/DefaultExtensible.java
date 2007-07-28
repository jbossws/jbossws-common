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
package org.jboss.wsf.framework;

//$Id: BasicDeploymentContext.java 3959 2007-07-20 14:44:19Z heiko.braun@jboss.com $

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.wsf.spi.Extensible;

/**
 * A general extendible artifact 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class DefaultExtensible implements Extensible
{
   private Map<Class, Object> attachments = new HashMap<Class, Object>();
   private Map<String, Object> properties = new HashMap<String, Object>();
   
   public Collection<Object> getAttachments()
   {
      return attachments.values();
   }
   
   public <T> T getAttachment(Class<T> clazz)
   {
      return (T)attachments.get(clazz);
   }
   
   public <T> T addAttachment(Class<T> clazz, Object obj)
   {
      return (T)attachments.put(clazz, obj);
   }

   public <T> T removeAttachment(Class<T> key)
   {
      return (T)attachments.remove(key);
   }
   
   public Set<String> getProperties()
   {
      return properties.keySet();
   }

   public Object getProperty(String key)
   {
      return properties.get(key);
   }

   public void removeProperty(String key)
   {
      properties.remove(key);
   }

   public void setProperty(String key, Object value)
   {
      properties.put(key, value);
   }

   public void setProperties(Map<String, Object> props)
   {
      properties.putAll(props);
   }
}
