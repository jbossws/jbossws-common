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
package org.jboss.ws.common.utils;

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
   private final URL targetURL;

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
