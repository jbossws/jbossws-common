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
package org.jboss.ws.integration;

// $Id$

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The default file adapter loads resources through an associated classloader.
 * If no classload is set, the the thread context classloader will be used.
 *
 * @author Heiko.Braun@jboss.org
 * @since 25.01.2007
 */
public class URLLoaderAdapter implements UnifiedVirtualFile
{
   private URL rootURL;
   private URL resourceURL;
   private transient URLClassLoader loader;

   public URLLoaderAdapter(URL rootURL)
   {
      this.rootURL = rootURL;
   }
   
   private URLLoaderAdapter(URL rootURL, URLClassLoader loader, URL resourceURL)
   {
      this.rootURL = rootURL;
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
               resourceURL = getResourceLoader().getResource(resourcePath);
            }
            catch (Exception ex)
            {
               // ignore
            }
         }
      }

      if (resourceURL == null)
         throw new IOException("Cannot get URL for: " + resourcePath);

      return new URLLoaderAdapter(rootURL, loader, resourceURL);
   }

   public URL toURL()
   {
      if (resourceURL != null)
         return resourceURL;
      else
         return rootURL;
   }

   private URLClassLoader getResourceLoader()
   {
      if (loader == null)
      {
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         loader = new URLClassLoader(new URL[]{rootURL}, ctxLoader);
      }
      return loader;
   }
}
