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
package org.jboss.wsf.spi.binding.jaxb;

// $Id: $

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jboss.wsf.spi.utils.HashCodeUtil;

/**
 * Cache JAXBContext's.
 *
 * @author Heiko.Braun@jboss.com
 * @author Thomas.Diesler@jboss.com
 * @since 26-Jun-2007
 */
public class JAXBContextCache implements JAXBHandler
{
   private Map<Integer, JAXBContext> cache = new ConcurrentHashMap<Integer, JAXBContext>();

   /**
    * Retrieve a cached JAXBContext instance.
    * If no instance is cached a new one will be created and registered.
    */
   public JAXBContext getJAXBContext(Class[] javaTypes) throws JAXBException
   {
      Integer id = buildId(javaTypes);
      JAXBContext context = cache.get(id);
      if (null == context)
      {
         context = JAXBContext.newInstance(javaTypes);
         cache.put(id, context);
      }

      return context;
   }

   private Integer buildId(Class[] classes)
   {
      int sum = HashCodeUtil.SEED;
      for (Class cls : classes)
      {
         sum = HashCodeUtil.hash(sum, cls.getName());
      }
      return new Integer(sum);
   }
}
