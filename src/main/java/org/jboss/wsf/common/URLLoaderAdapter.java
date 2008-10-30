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
package org.jboss.wsf.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
 * Load resources through a URLClassLoader.<br>
 * NOTE: The associated classloader doesn't do parent delegation.
 *
 *
 * @author Heiko.Braun@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 25.01.2007
 */
public class URLLoaderAdapter implements UnifiedVirtualFile
{
   private URL rootURL;
   private URL resourceURL;
   private transient URLClassLoader loader;
   private static Logger log = Logger.getLogger(URLLoaderAdapter.class);
   private static final String jarFileSeparator = "/";

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
         loader = new URLClassLoader(new URL[] { rootURL });
      }
      return loader;
   }
   
   public List<UnifiedVirtualFile> getChildren() throws IOException
   {
      List<UnifiedVirtualFile> list = new LinkedList<UnifiedVirtualFile>();

      URL url = toURL();
      
      if (url.getProtocol().equals("jar"))
      {
         String urlString = url.toExternalForm();
         String jarRoot = urlString.substring(4, urlString.indexOf("ar!") + 2);
         String path = urlString.contains("!") ? urlString.substring(urlString.lastIndexOf("!") + 2) : "";
         if (path.endsWith(jarFileSeparator))
            path = path.substring(0, path.lastIndexOf(jarFileSeparator));
         
         try
         {
            String folder = jarRoot.substring(5,jarRoot.lastIndexOf(File.separator));
            String filename = jarRoot.substring(jarRoot.lastIndexOf(File.separator)+1);
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
                  list.add(new URLLoaderAdapter(rootURL, loader, sUrl));
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            log.error("Cannot get children for resource: " + url);
         }
      }
      else //std file/dir
      {
         try
         {
            File file = new File(url.toURI());
            if (file.exists() && file.isDirectory())
            {
               File[] files = file.listFiles();
               if (files != null)
               {
                  for (File f : files)
                  {
                     list.add(new URLLoaderAdapter(rootURL, loader, f.toURL()));
                  }
               }
            }
         }
         catch (Exception e)
         {
            log.error("Cannot get children for resource: " + url, e);
         }
      }
      return list;
   }

   public String getName()
   {
      String name = null;
      try
      {
         String filename = toURL().getFile();
         File f = new File(filename);
         name = f.getName();
         if (f.isDirectory() || (toURL().getProtocol().equals("jar") && filename.endsWith(jarFileSeparator)))
            name = name + jarFileSeparator;
      }
      catch (Exception e)
      {
         log.error("Cannot get name for resource: " + toURL(), e);
      }
      return name;
   }
}
