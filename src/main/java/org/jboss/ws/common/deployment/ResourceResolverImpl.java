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
package org.jboss.ws.common.deployment;

import static org.jboss.ws.common.Loggers.ROOT_LOGGER;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.jboss.ws.common.Messages;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * A resource resolver implementation using unified virtual files 
 * and classloader
 * 
 * @author alessio.soldano@jboss.com
 * @author ema@redhat.com
 * @since 19-Nov-2009
 *  
 */
public class ResourceResolverImpl implements ResourceResolver
{
   private final UnifiedVirtualFile rootFile;
   private final Collection<UnifiedVirtualFile> metadataFiles;
   private final ArchiveDeployment deployment; 
   
   public ResourceResolverImpl(final ArchiveDeployment deployment)
   {
      this.deployment = deployment;
      this.rootFile = deployment.getRootFile();
      this.metadataFiles = deployment.getMetadataFiles();
   }
   
   public URL resolve(String resourcePath) throws IOException
   {
      URL resourceURL = null;
      if (resourcePath != null && resourcePath.length() > 0)
      {
         if (resourcePath.startsWith("/"))
            resourcePath = resourcePath.substring(1);

         try
         {
            // assign an absolute URL 
            resourceURL = new URL(resourcePath);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         if (resourceURL == null && rootFile != null)
         {
            try
            {
               UnifiedVirtualFile vfResource = rootFile.findChild(resourcePath);
               resourceURL = vfResource.toURL();
            }
            catch (IOException e)
            {
               //ignore this to try metadataFiles and load it from classLoader
            }
         }
         //scan additional metadata files (for instance originally attached to a VFSDeploymentUnit)
         if (resourceURL == null && metadataFiles != null && !metadataFiles.isEmpty())
         {
            ROOT_LOGGER.cannotGetRootFileTryingWithAdditionalMetaData(resourcePath);
            UnifiedVirtualFile vfResource = null;
            for (Iterator<UnifiedVirtualFile> it = metadataFiles.iterator(); it.hasNext() && vfResource == null;)
            {
               UnifiedVirtualFile uvf = it.next();
               URL wsdlUrl = uvf.toURL();
               String wsdlPath = wsdlUrl.getPath();
               if (wsdlPath.startsWith("/"))
                  wsdlPath = wsdlPath.substring(1);
               if (resourcePath.equals(wsdlPath))
               {
                  vfResource = uvf;
               }
               else
               {
                  try
                  {
                     vfResource = uvf.findChild(resourcePath);
                  }
                  catch (IOException e)
                  {
                     ROOT_LOGGER.cannotGetRootResourceFrom(resourcePath, uvf, e);
                  }
               }
            }
            if (vfResource != null)
            {
               resourceURL = vfResource.toURL();
            }
         }
         if (resourceURL == null && deployment.getClassLoader() != null)
         {
            resourceURL = deployment.getClassLoader().getResource(resourcePath);
         }
         if (resourceURL == null)
         {
            throw Messages.MESSAGES.cannotResolveResource(resourcePath, deployment.getSimpleName());
         }
      }
      return resourceURL;
   }
   
   public URL resolveFailSafe(String resourcePath)
   {
      final boolean traceEnabled = ROOT_LOGGER.isTraceEnabled();
      URL resourceURL = null;
      if (resourcePath != null && resourcePath.length() > 0)
      {
         if (resourcePath.startsWith("/"))
            resourcePath = resourcePath.substring(1);

         try
         {
            // assign an absolute URL 
            resourceURL = new URL(resourcePath);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         if (resourceURL == null && rootFile != null)
         {
            UnifiedVirtualFile vfResource = rootFile.findChildFailSafe(resourcePath);
            if (vfResource == null)
            {
               if (metadataFiles == null || metadataFiles.isEmpty())
               {
                  if (traceEnabled) ROOT_LOGGER.cannotGetRootResourceFrom(resourcePath, rootFile, null);
               }
               else
               {
                  if (traceEnabled) ROOT_LOGGER.cannotGetRootFileTryingWithAdditionalMetaData(resourcePath);
               }
            }
            else
            {
               resourceURL = vfResource.toURL();
            }
         }
         //scan additional metadata files (for instance originally attached to a VFSDeploymentUnit)
         if (resourceURL == null && metadataFiles != null && !metadataFiles.isEmpty())
         {
            UnifiedVirtualFile vfResource = null;
            for (Iterator<UnifiedVirtualFile> it = metadataFiles.iterator(); it.hasNext() && vfResource == null;)
            {
               UnifiedVirtualFile uvf = it.next();
               URL wsdlUrl = uvf.toURL();
               String wsdlPath = wsdlUrl.getPath();
               if (wsdlPath.startsWith("/"))
                  wsdlPath = wsdlPath.substring(1);
               if (resourcePath.equals(wsdlPath))
               {
                  vfResource = uvf;
               }
               else
               {
                  vfResource = uvf.findChildFailSafe(resourcePath);
                  if (traceEnabled && vfResource == null) {
                     ROOT_LOGGER.cannotGetRootResourceFrom(resourcePath, uvf, null);
                  }
               }
            }
            if (vfResource == null)
            {
               if (traceEnabled) ROOT_LOGGER.cannotFindInAdditionalMetaData(resourcePath);
            }
            else
            {
               resourceURL = vfResource.toURL();
            }
         }
         if (resourceURL == null && deployment.getClassLoader() != null)
         {
            resourceURL = deployment.getClassLoader().getResource(resourcePath);
         }
      }
      return resourceURL;
   }
   
}
