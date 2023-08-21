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
package org.jboss.ws.common;

import static org.jboss.ws.common.Loggers.ROOT_LOGGER;
import static org.jboss.ws.common.Messages.MESSAGES;

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
   private static final long serialVersionUID = -8567810932195204615L;
   
   private final URL resourceURL;
   private final ClassLoader loader;
   private static final String jarFileSeparator = "/";

   public ResourceLoaderAdapter()
   {
      this(SecurityActions.getContextClassLoader());
   }

   public ResourceLoaderAdapter(ClassLoader loader)
   {
      this.resourceURL = null;
      this.loader = loader;
   }

   private ResourceLoaderAdapter(ClassLoader loader, URL resourceURL)
   {
      this.resourceURL = resourceURL;
      this.loader = loader;
   }

   private UnifiedVirtualFile findChild(String resourcePath, boolean throwExceptionIfNotFound) throws IOException
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
      {
         if (throwExceptionIfNotFound)
         {
            throw MESSAGES.cannotGetURLFor(resourcePath);
         }
         else
         {
            if (ROOT_LOGGER.isTraceEnabled()) ROOT_LOGGER.cannotGetURLFor(resourcePath);
            return null;
         }
      }

      return new ResourceLoaderAdapter(loader, resourceURL);
   }

   public UnifiedVirtualFile findChild(String child) throws IOException
   {
      return findChild(child, true);
   }

   public UnifiedVirtualFile findChildFailSafe(String child)
   {
      try
      {
         return findChild(child, false);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public URL toURL()
   {
      if (null == this.resourceURL)
         throw MESSAGES.unifiedVirtualFileNotInitialized(loader);
      return resourceURL;
   }

   public List<UnifiedVirtualFile> getChildren() throws IOException
   {
      if (null == this.resourceURL)
         throw MESSAGES.unifiedVirtualFileNotInitialized(loader);
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
            ROOT_LOGGER.cannotGetChildrenForResource(e, resourceURL);
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
            ROOT_LOGGER.cannotGetChildrenForResource(e, resourceURL);
         }
      }
      return list;
   }

   public String getName()
   {
      if (null == this.resourceURL)
         throw MESSAGES.unifiedVirtualFileNotInitialized(loader);
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
         ROOT_LOGGER.cannotGetNameForResource(e, resourceURL);
      }
      return name;
   }
}
