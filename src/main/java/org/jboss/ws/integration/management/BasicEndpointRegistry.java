/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.jboss.ws.integration.management;

// $Id$

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.integration.Endpoint;

/**
 * A general endpoint registry.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public class BasicEndpointRegistry implements EndpointRegistry
{
   // provide logging
   private static final Logger log = Logger.getLogger(BasicEndpointRegistry.class);
   
   private Map<ObjectName, Endpoint> endpoints = new HashMap<ObjectName, Endpoint>();

   public Endpoint getEndpoint(ObjectName epName)
   {
      if (epName == null)
         throw new IllegalArgumentException("Endpoint name cannot be null");
      
      if (isRegistered(epName) == false)
         throw new IllegalStateException("Endpoint not registered: " + epName);
      
      Endpoint endpoint = endpoints.get(epName);
      return endpoint;
   }

   public Endpoint resolvePortComponentLink(String pcLink)
   {
      throw new NotImplementedException();
   }
   
   public boolean isRegistered(ObjectName epName)
   {
      if (epName == null)
         throw new IllegalArgumentException("Endpoint name cannot be null");
      
      return endpoints.get(epName) != null;
   }

   public Set<ObjectName> getEndpoints()
   {
      return endpoints.keySet();
   }

   public void register(Endpoint endpoint)
   {
      if (endpoint == null)
         throw new IllegalArgumentException("Endpoint cannot be null");
      
      ObjectName epName = endpoint.getName();
      if (epName == null)
         throw new IllegalStateException("Endpoint name cannot be null for: " + endpoint);
      
      if (isRegistered(epName))
         throw new IllegalStateException("Endpoint already registered: " + epName);
      
      log.info("register: " + epName);
      endpoints.put(epName, endpoint);
   }

   public void unregister(Endpoint endpoint)
   {
      if (endpoint == null)
         throw new IllegalArgumentException("Endpoint cannot be null");
      
      ObjectName epName = endpoint.getName();
      if (isRegistered(epName) == false)
         throw new IllegalStateException("Endpoint not registered: " + epName);
      
      log.info("remove: " + epName);
      endpoints.remove(epName);
   }
}
