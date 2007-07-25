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

// $Id$

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.ObjectName;

import org.jboss.ws.integration.UnifiedVirtualFile;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;

/**
 * The container independent deployment info.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedDeploymentInfo
{
   /** Sub deployments have a parent */
   private UnifiedDeploymentInfo parent;
   /** The suffix of the deployment url */
   private String simpleName;
   /** The URL for this deployment */
   private URL url;
   /** The virtual file for the deployment root */
   private UnifiedVirtualFile vfRoot;
   /** The URL to the expanded webapp **/
   private URL webappURL;
   /** We can hold "typed" metadata */
   private Object metaData;
   /** The deployment classloader **/
   private ClassLoader classLoader;
   /** An optional ObjectName of the deployed object */
   private ObjectName deployedObject;

   public UnifiedDeploymentInfo(DeploymentType type)
   {
   }

   public void setDeployedObject(ObjectName deployedObject)
   {
      this.deployedObject = deployedObject;
   }

   public ObjectName getDeployedObject()
   {
      return deployedObject;
   }

   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   public void setMetaData(Object metaData)
   {
      this.metaData = metaData;
   }

   public Object getMetaData()
   {
      return metaData;
   }

   public void setWebappURL(URL webappURL)
   {
      this.webappURL = webappURL;
   }

   public URL getWebappURL()
   {
      return webappURL;
   }

   public void setVfRoot(UnifiedVirtualFile vfRoot)
   {
      this.vfRoot = vfRoot;
   }

   public UnifiedVirtualFile getVfRoot()
   {
      return vfRoot;
   }

   public void setUrl(URL url)
   {
      this.url = url;
   }

   public URL getUrl()
   {
      return url;
   }

   public void setSimpleName(String simpleName)
   {
      this.simpleName = simpleName;
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   public void setParent(UnifiedDeploymentInfo parent)
   {
      this.parent = parent;
   }

   public UnifiedDeploymentInfo getParent()
   {
      return parent;
   }

   /** The sortName concatenated with the canonical names of all parents. */
   public String getCanonicalName()
   {
      String name = getSimpleName();
      if (getParent() != null)
         name = getParent().getCanonicalName() + "/" + name;
      return name;
   }

   public URL getMetaDataFileURL(String resourcePath) throws IOException
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

         if (resourceURL == null && getVfRoot() != null)
         {
            UnifiedVirtualFile vfResource = getVfRoot().findChild(resourcePath);
            resourceURL = vfResource.toURL();
         }

         if (resourceURL == null)
         {
            String deploymentPath = getUrl().toExternalForm();

            if (deploymentPath.startsWith("jar:") && deploymentPath.endsWith("!/") == false)
               deploymentPath += "!/";
            
            if (deploymentPath.startsWith("war:") && deploymentPath.endsWith("!/") == false)
               deploymentPath += "!/";
            
            if (deploymentPath.endsWith("/") == false)
               deploymentPath += "/";

            // assign a relative URL
            resourceURL = new URL(deploymentPath + resourcePath);
         }
      }
      return resourceURL;
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      builder.append(",simpleName=" + simpleName);
      builder.append(",url=" + url);
      builder.append("]");
      return builder.toString();
   }
}
