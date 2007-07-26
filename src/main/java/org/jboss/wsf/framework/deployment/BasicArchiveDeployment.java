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

//$Id: BasicDeployment.java 3995 2007-07-26 08:52:45Z thomas.diesler@jboss.com $

import org.jboss.ws.integration.UnifiedVirtualFile;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;


/**
 * A general web service deployment that is based on an archive. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class BasicArchiveDeployment extends BasicDeployment implements ArchiveDeployment
{
   // The root file for this deployment
   private UnifiedVirtualFile rootFile;

   BasicArchiveDeployment(ClassLoader classLoader)
   {
      super(classLoader);
   }

   public UnifiedVirtualFile getRootFile()
   {
      return rootFile;
   }

   public void setRootFile(UnifiedVirtualFile rootFile)
   {
      this.rootFile = rootFile;
   }
}
