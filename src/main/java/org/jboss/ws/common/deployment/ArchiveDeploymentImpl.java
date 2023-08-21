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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * A general web service deployment that is based on an archive. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @author ema@redhat.com
 * @since 20-Apr-2007 
 */
public class ArchiveDeploymentImpl extends DefaultDeployment implements ArchiveDeployment
{
   // The optional parent
   private final ArchiveDeployment parent;
   // The root file for this deployment
   private final UnifiedVirtualFile rootFile;
   
   private List<UnifiedVirtualFile> metadataFiles;

   ArchiveDeploymentImpl(String simpleName, ClassLoader classLoader, UnifiedVirtualFile rootFile)
   {
      super(simpleName, classLoader);
      this.parent = null;
      this.rootFile = rootFile;
   }

   ArchiveDeploymentImpl(ArchiveDeployment parent, String simpleName, ClassLoader classLoader, UnifiedVirtualFile rootFile)
   {
      super(simpleName, classLoader);
      this.parent = parent;
      this.rootFile = rootFile;
   }

   public ArchiveDeployment getParent()
   {
      return parent;
   }

   public UnifiedVirtualFile getRootFile()
   {
      return rootFile;
   }

   public String getCanonicalName()
   {
      String name = getSimpleName();
      if (getParent() != null)
         name = getParent().getCanonicalName() + "/" + name;
      return name;
   }

   public synchronized List<UnifiedVirtualFile> getMetadataFiles()
   {
      if (metadataFiles == null) {
         return Collections.emptyList();
      } else {
         return Collections.unmodifiableList(metadataFiles);
      }
   }
   
   public synchronized void addMetadataFile(UnifiedVirtualFile file)
   {
      if (metadataFiles == null) {
         metadataFiles = new ArrayList<UnifiedVirtualFile>();
      }
      metadataFiles.add(file);
   }
   
   public ResourceResolver getResourceResolver()
   {
      return new ResourceResolverImpl(this);
   }
}
