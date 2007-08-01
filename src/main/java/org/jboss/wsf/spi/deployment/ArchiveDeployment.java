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
package org.jboss.wsf.spi.deployment;

//$Id: Deployment.java 3992 2007-07-25 12:48:59Z thomas.diesler@jboss.com $

import java.io.IOException;
import java.net.URL;


/**
 * A general web service deployment dep. 
 * 
 * It has no notion of J2EE deployment packages. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public interface ArchiveDeployment extends Deployment
{
   /** Get the optional parent of this deployment */
   ArchiveDeployment getParent();

   /** Set the optional parent for this deployment */
   void setParent (ArchiveDeployment parent);
   
   /** Get the root file for this deployment */
   UnifiedVirtualFile getRootFile();
   
   /** Set the root file for this deployment */
   void setRootFile(UnifiedVirtualFile root);
   
   /** The concatenated names including all parents. */
   String getCanonicalName();
   
   /** Get the URL for a given resource path */
   URL getMetaDataFileURL(String resourcePath) throws IOException;
}