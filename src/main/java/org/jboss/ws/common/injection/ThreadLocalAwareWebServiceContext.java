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
package org.jboss.ws.common.injection;

import java.io.Serializable;
import java.security.Principal;

import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

import org.w3c.dom.Element;

/**
 * Web service context implementation that is thread local aware as required by JAX-WS spec.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class ThreadLocalAwareWebServiceContext implements WebServiceContext, Serializable
{

   private static final long serialVersionUID = 126557512266764152L;

   private static final ThreadLocalAwareWebServiceContext SINGLETON = new ThreadLocalAwareWebServiceContext();

   private final transient ThreadLocal<WebServiceContext> contexts = new InheritableThreadLocal<WebServiceContext>();

   public static ThreadLocalAwareWebServiceContext getInstance()
   {
      return SINGLETON;
   }

   public void setMessageContext(final WebServiceContext ctx)
   {
      this.contexts.set(ctx);
   }

   public EndpointReference getEndpointReference(final Element... referenceParameters)
   {
      return getWebServiceContext().getEndpointReference(referenceParameters);
   }

   public <T extends EndpointReference> T getEndpointReference(final Class<T> clazz, final Element... referenceParameters)
   {
      return getWebServiceContext().getEndpointReference(clazz, referenceParameters);
   }

   public MessageContext getMessageContext()
   {
      return getWebServiceContext().getMessageContext();
   }

   public Principal getUserPrincipal()
   {
      return getWebServiceContext().getUserPrincipal();
   }

   public boolean isUserInRole(String role)
   {
      return getWebServiceContext().isUserInRole(role);
   }
   
   private WebServiceContext getWebServiceContext()
   {
       final WebServiceContext delegate = contexts.get();

       if (delegate == null)
       {
          throw new IllegalStateException();
       }
       
       return delegate;
   }

   protected Object readResolve()
   {
       return SINGLETON;
   }

}
