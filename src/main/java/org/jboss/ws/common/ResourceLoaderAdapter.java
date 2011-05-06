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
package org.jboss.ws.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * The default file adapter loads resources through an associated classloader.
 * If no classload is set, the the thread context classloader will be used.
 *
 * @author Heiko.Braun@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 25.01.2007
 */
public class ResourceLoaderAdapter implements UnifiedVirtualFile
{
   private URL resourceURL;
   private ClassLoader loader;
   private static Logger log = Logger.getLogger(ResourceLoaderAdapter.class);
   private static final String jarFileSeparator = "/";

   public ResourceLoaderAdapter()
   {
      this(SecurityActions.getContextClassLoader());
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
                  resourceURL = file.toURI().toURL();
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

   public List<UnifiedVirtualFile> getChildren() throws IOException
   {
      if (null == this.resourceURL)
         throw new IllegalStateException("UnifiedVirtualFile not initialized");
      List<UnifiedVirtualFile> list = new LinkedList<UnifiedVirtualFile>();
      if (resourceURL.getProtocol().equals("jar"))
      {
         String urlString = resourceURL.toExternalForm();
         String jarRoot = urlString.substring(4, urlString.indexOf("ar!") + 2);
         String path = urlString.contains("!") ? urlString.substring(urlString.lastIndexOf("!") + 2) : "";
         if (path.endsWith(jarFileSeparator))
            path = path.substring(0, path.lastIndexOf(jarFileSeparator));
         
         try
         {
            //get the jar and open it
            String folder = jarRoot.substring(5,jarRoot.lastIndexOf("/"));
            String filename = jarRoot.substring(jarRoot.lastIndexOf("/")+1);
            final File jar = new File(folder, filename);
            
            PrivilegedAction<JarFile> action = new PrivilegedAction<JarFile>()
            {
               public JarFile run()
               {
                  try
                  {
                     return new JarFile(jar);
                  }
                  catch (IOException e)
                  {
                     throw new RuntimeException(e);
                  }
               }
            };
            JarFile jarFile = AccessController.doPrivileged(action);
            
            if (jar.canRead())
            {
               Enumeration<JarEntry> entries = jarFile.entries();
               List<String> pathMatch = new LinkedList<String>();
               List<String> finalMatch = new LinkedList<String>();
               while (entries.hasMoreElements())
               {
                  JarEntry entry = entries.nextElement();
                  String name = entry.getName();
                  //keep entries starting with the current resource path (exclude inner classes and the current file)
                  if (name.startsWith(path + jarFileSeparator) && (name.length() > path.length() + 1) && !name.contains("$"))
                     pathMatch.add(name.substring(path.length() + 1));
               }
               for (String s : pathMatch)
               {
                  //do not go deeper than the current dir
                  if (!s.contains(jarFileSeparator) || s.indexOf(jarFileSeparator) == s.length() - 1)
                     finalMatch.add(s);
               }
               for (String s : finalMatch)
               {
                  URL sUrl = new URL(urlString + jarFileSeparator + s);
                  list.add(new ResourceLoaderAdapter(loader, sUrl));
               }
            }
         }
         catch (Exception e)
         {
            log.error("Cannot get children for resource: " + resourceURL, e);
         }
      }
      else //std file/dir
      {
         try
         {
            File file = new File(resourceURL.toURI());
            if (file.exists() && file.isDirectory())
            {
               File[] files = file.listFiles();
               if (files != null)
               {
                  for (File f : files)
                  {
                     list.add(new ResourceLoaderAdapter(loader, f.toURI().toURL()));
                  }
               }
            }
         }
         catch (Exception e)
         {
            log.error("Cannot get children for resource: " + resourceURL, e);
         }
      }
      return list;
   }

   public String getName()
   {
      if (null == this.resourceURL)
         throw new IllegalStateException("UnifiedVirtualFile not initialized");
      String name = null;
      try
      {
         String filename = resourceURL.getFile();
         File f = new File(filename);
         name = f.getName();
         if (f.isDirectory() || (resourceURL.getProtocol().equals("jar") && filename.endsWith(jarFileSeparator)))
            name = name + jarFileSeparator;
      }
      catch (Exception e)
      {
         log.error("Cannot get name for resource: " + resourceURL);
      }
      return name;
   }
}
