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
package org.jboss.ws.common.security;

import java.security.Principal;

import javax.security.auth.Subject;

import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DefaultSecurityAdapterFactory extends SecurityAdaptorFactory
{

   private static SecurityAdaptor SECURITY_ADAPTOR = new DefaultSecurityAdaptor();
   
   /**
    * Constructor.
    */
   public DefaultSecurityAdapterFactory()
   {
      super();
   }

   /**
    * Creates new security adapter instance.
    * 
    * @return security adapter
    */
   public SecurityAdaptor newSecurityAdapter()
   {
      return DefaultSecurityAdapterFactory.SECURITY_ADAPTOR;
   }

   private static class DefaultSecurityAdaptor implements SecurityAdaptor
   {
      
      private static ThreadLocal<Principal> principal = new ThreadLocal<Principal>();
      private static ThreadLocal<Object> credential = new ThreadLocal<Object>();

      public Object getCredential()
      {
         return DefaultSecurityAdaptor.credential.get();
      }

      public Principal getPrincipal()
      {
         return DefaultSecurityAdaptor.principal.get();
      }

      public void pushSubjectContext(Subject subject, Principal principal, Object credential)
      {
         // does nothing
      }

      public void setCredential(Object credential)
      {
         DefaultSecurityAdaptor.credential.set(credential);
      }

      public void setPrincipal(Principal principal)
      {
         DefaultSecurityAdaptor.principal.set(principal);
      }
      
   }

}
