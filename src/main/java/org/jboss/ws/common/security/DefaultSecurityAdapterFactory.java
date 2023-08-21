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
package org.jboss.ws.common.security;

import java.security.Principal;

import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;

/**
 * TODO: javadoc
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DefaultSecurityAdapterFactory extends SecurityAdaptorFactory
{

   private static final SecurityAdaptor SECURITY_ADAPTOR = new DefaultSecurityAdaptor();
   
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
      
      private static final ThreadLocal<Principal> principal = new ThreadLocal<Principal>();
      private static final ThreadLocal<Object> credential = new ThreadLocal<Object>();

      public Object getCredential()
      {
         return DefaultSecurityAdaptor.credential.get();
      }

      public Principal getPrincipal()
      {
         return DefaultSecurityAdaptor.principal.get();
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
