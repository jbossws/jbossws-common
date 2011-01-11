/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.framework.deployment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * A resource resolver implementation using unified virtual files
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Nov-2009
 *  
 */
public class ResourceResolverImpl implements ResourceResolver
{
   private static Logger log = Logger.getLogger(ResourceResolverImpl.class);
   
   private UnifiedVirtualFile rootFile;
   private Collection<UnifiedVirtualFile> metadataFiles;
   
   public ResourceResolverImpl(UnifiedVirtualFile rootFile, Collection<UnifiedVirtualFile> metadataFiles)
   {
      this.rootFile = rootFile;
      this.metadataFiles = metadataFiles;
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
               if (metadataFiles == null || metadataFiles.isEmpty())
               {
                  throw e;
               }
               else
               {
                  if (log.isTraceEnabled())
                     log.trace("Cannot get " + resourcePath + " from root file, trying with additional metadata files", e);
               }
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
                  try
                  {
                     vfResource = uvf.findChild(resourcePath);
                  }
                  catch (IOException e)
                  {
                     if (log.isTraceEnabled())
                        log.trace("Cannot get " + resourcePath + " from " + uvf, e);
                  }
               }
            }
            if (vfResource == null)
               throw new IOException("Could not find " + resourcePath + " in the additional metadatafiles!");
            
            resourceURL = vfResource.toURL();
         }
      }
      return resourceURL;
   }
   
}
