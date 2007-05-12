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
package org.jboss.ws.metadata.j2ee;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 * @author darran.lofthouse@jboss.com
 * @since Oct 22, 2006
 */
public class UnifiedWebSecurityMetaData
{

   /** The optional security-constraint/user-data-constraint/transport-guarantee */
   private String transportGuarantee;

   /**
    * The HashMap for the security-constraint/web-resource-collection 
    * elements.
    */
   private HashMap<String, UnifiedWebResourceCollection> webResources = new HashMap<String, UnifiedWebResourceCollection>();

   public UnifiedWebResourceCollection addWebResource(final String name)
   {
      UnifiedWebResourceCollection wrc = new UnifiedWebResourceCollection(name);
      webResources.put(name, wrc);

      return wrc;
   }

   public Collection<UnifiedWebResourceCollection> getWebResources()
   {
      return webResources.values();
   }

   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }

   public void setTransportGuarantee(String transportGuarantee)
   {
      this.transportGuarantee = transportGuarantee;
   }

   public static class UnifiedWebResourceCollection
   {
      private String name;
      /** The required url-pattern element(s) */
      private HashSet<String> urlPatterns = new HashSet<String>();

      public UnifiedWebResourceCollection(final String name)
      {
         this.name = name;
      }

      public String getName()
      {
         return name;
      }

      public void addPattern(String pattern)
      {
         urlPatterns.add(pattern);
      }

      public HashSet<String> getUrlPatterns()
      {
         return urlPatterns;
      }
   }

}
