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
package org.jboss.wsf.framework.deployment;

// $Id: ResourceLoaderAdapter.java 3137 2007-05-18 13:41:57Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;


/**
 * The default file adapter loads resources through an associated classloader.
 * If no classload is set, the the thread context classloader will be used.
 *
 * @author Heiko.Braun@jboss.org
 * @since 25.01.2007
 */
public class ResourceLoaderAdapter implements UnifiedVirtualFile
{
   private URL resourceURL;
   private ClassLoader loader;

   public ResourceLoaderAdapter()
   {
      this(Thread.currentThread().getContextClassLoader());
   }

   public ResourceLoaderAdapter(ClassLoader loader)
   {
      this.loader = loader;
   }

   private ResourceLoaderAdapter(ClassLoader loader, URL resourceURL)
   {
      this.resourceURL = resourceURL;
      this.loader = loader;
   }

   public UnifiedVirtualFile findChild(String resourcePath) throws IOException
   {
      URL resourceURL = null;
      if (resourcePath != null)
      {
         // Try the child as URL
         try
         {
            resourceURL = new URL(resourcePath);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         // Try the filename as File
         if (resourceURL == null)
         {
            try
            {
               File file = new File(resourcePath);
               if (file.exists())
                  resourceURL = file.toURL();
            }
            catch (MalformedURLException e)
            {
               // ignore
            }
         }

         // Try the filename as Resource
         if (resourceURL == null)
         {
            try
            {
               resourceURL = loader.getResource(resourcePath);
            }
            catch (Exception ex)
            {
               // ignore
            }
         }
      }

      if (resourceURL == null)
         throw new IOException("Cannot get URL for: " + resourcePath);

      return new ResourceLoaderAdapter(loader, resourceURL);
   }

   public URL toURL()
   {
      if (null == this.resourceURL)
         throw new IllegalStateException("UnifiedVirtualFile not initialized");
      return resourceURL;
   }
}
