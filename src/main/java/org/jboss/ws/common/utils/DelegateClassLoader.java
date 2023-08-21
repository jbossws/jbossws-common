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
package org.jboss.ws.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A delegate classloader
 * 
 * @author alessio.soldano@jboss.com
 *
 */
public class DelegateClassLoader extends SecureClassLoader
{
   private final ClassLoader delegate;

   private final ClassLoader parent;

   public DelegateClassLoader(final ClassLoader delegate, final ClassLoader parent)
   {
      super(parent);
      this.delegate = delegate;
      this.parent = parent;
   }

   /** {@inheritDoc} */
   @Override
   public Class<?> loadClass(final String className) throws ClassNotFoundException
   {
      if (parent != null)
      {
         try
         {
            return parent.loadClass(className);
         }
         catch (ClassNotFoundException cnfe)
         {
            //NOOP, use delegate
         }
      }
      return delegate.loadClass(className);
   }

   /** {@inheritDoc} */
   @Override
   public URL getResource(final String name)
   {
      URL url = null;
      if (parent != null)
      {
         url = parent.getResource(name);
      }
      return (url == null) ? delegate.getResource(name) : url;
   }

   /** {@inheritDoc} */
   @Override
   public Enumeration<URL> getResources(final String name) throws IOException
   {
      final ArrayList<Enumeration<URL>> foundResources = new ArrayList<Enumeration<URL>>();

      foundResources.add(delegate.getResources(name));
      if (parent != null)
      {
         foundResources.add(parent.getResources(name));
      }

      return new Enumeration<URL>()
      {
         private int position = foundResources.size() - 1;

         public boolean hasMoreElements()
         {
            while (position >= 0)
            {
               if (foundResources.get(position).hasMoreElements())
               {
                  return true;
               }
               position--;
            }
            return false;
         }

         public URL nextElement()
         {
            while (position >= 0)
            {
               try
               {
                  return (foundResources.get(position)).nextElement();
               }
               catch (NoSuchElementException e)
               {
               }
               position--;
            }
            throw new NoSuchElementException();
         }
      };
   }

   /** {@inheritDoc} */
   @Override
   public InputStream getResourceAsStream(final String name)
   {
      InputStream is = null;
      if (parent != null)
      {
        is = parent.getResourceAsStream(name);
      }
      return (is == null) ? delegate.getResourceAsStream(name) : is;
   }
}
