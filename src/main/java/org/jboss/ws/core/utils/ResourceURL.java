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
package org.jboss.ws.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A wrapper around an URL that can handle input streams for resources in nested jars.
 * 
 * The jdk-1.5.0_10 cannot handle this type of URL
 * 
 *    jar:file://somepath/jaxws-eardeployment.ear!/jaxws-eardeployment.war!/WEB-INF/wsdl/TestEndpoint.wsdl
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Dec-2006 (Dosi's birthday)
 */
public class ResourceURL
{
   private URL targetURL;

   public ResourceURL(URL targetURL)
   {
      this.targetURL = targetURL;
   }

   public URL getTargetURL()
   {
      return targetURL;
   }

   public InputStream openStream() throws IOException
   {
      boolean isJarUrl = "jar".equals(targetURL.getProtocol());
      return isJarUrl ? new JarUrlConnection(targetURL).getInputStream() : targetURL.openStream();
   }

   public int hashCode()
   {
      return toString().hashCode();
   }
   
   public boolean equals(Object obj)
   {
      if (!(obj instanceof ResourceURL)) return false;
      ResourceURL other = (ResourceURL)obj;
      return toString().equals(other.toString());
   }
   
   public String toString()
   {
      return targetURL.toString();
   }
}
