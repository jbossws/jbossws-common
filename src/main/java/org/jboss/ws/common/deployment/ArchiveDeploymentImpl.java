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
package org.jboss.ws.common.deployment;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * A general web service deployment that is based on an archive. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class ArchiveDeploymentImpl extends DefaultDeployment implements ArchiveDeployment
{
   // The optional parent
   private ArchiveDeployment parent;
   // The root file for this deployment
   private UnifiedVirtualFile rootFile;
   
   private static Logger log = Logger.getLogger(ArchiveDeploymentImpl.class);
   
   private List<UnifiedVirtualFile> metadataFiles;

   ArchiveDeploymentImpl(String simpleName, ClassLoader classLoader)
   {
      super(simpleName, classLoader);
   }

   public ArchiveDeployment getParent()
   {
      return parent;
   }

   public void setParent(ArchiveDeployment parent)
   {
      this.parent = parent;
   }

   public UnifiedVirtualFile getRootFile()
   {
      return rootFile;
   }

   public void setRootFile(UnifiedVirtualFile rootFile)
   {
      this.rootFile = rootFile;
   }

   public String getCanonicalName()
   {
      String name = getSimpleName();
      if (getParent() != null)
         name = getParent().getCanonicalName() + "/" + name;
      return name;
   }

   @Deprecated
   public URL getMetaDataFileURL(String resourcePath) throws IOException
   {
      return getResourceResolver().resolve(resourcePath);
   }
   
   public List<UnifiedVirtualFile> getMetadataFiles()
   {
      return metadataFiles;
   }
   
   public void setMetadataFiles(List<UnifiedVirtualFile> metadataFiles)
   {
      this.metadataFiles = metadataFiles;
   }
   
   public ResourceResolver getResourceResolver()
   {
      return new ResourceResolverImpl(rootFile, metadataFiles);
   }
}
