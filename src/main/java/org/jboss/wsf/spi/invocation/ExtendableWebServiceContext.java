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
package org.jboss.wsf.spi.invocation;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.util.NotImplementedException;
import org.w3c.dom.Element;

/**
 * A WebServiceContext makes it possible for a web service endpoint implementation 
 * class to access message context and security information relative to a request 
 * being served. Typically a WebServiceContext is injected into an endpoint implementation 
 * class using the Resource annotation.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 03-May-2006
 */
public abstract class ExtendableWebServiceContext implements WebServiceContext
{
   private MessageContext messageContext;

   private Map<Class, Object> attachments = new HashMap<Class, Object>();

   public ExtendableWebServiceContext(MessageContext messageContext)
   {
      this.messageContext = messageContext;
   }

   public MessageContext getMessageContext()
   {
      return messageContext;
   }

   public abstract Principal getUserPrincipal();

   public abstract boolean isUserInRole(String role);

   public EndpointReference getEndpointReference(Element... referenceParameters)
   {
      throw new IllegalArgumentException("Not implemented");
   }

   public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters)
   {
      throw new IllegalArgumentException("Not implemented");
   }

   public <T> T addAttachment(Class<T> key, Object value)
   {
      return (T)attachments.put(key, value);
   }

   public <T> T getAttachment(Class<T> key)
   {
      return (T)attachments.get(key);
   }

   public <T> T removeAttachment(Class<T> key)
   {
      return (T)attachments.get(key);
   }
}