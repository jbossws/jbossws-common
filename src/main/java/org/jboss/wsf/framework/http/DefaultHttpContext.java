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
package org.jboss.wsf.framework.http;

//$Id: HttpContext.java 1757 2006-12-22 15:40:24Z thomas.diesler@jboss.com $

import org.jboss.wsf.framework.DefaultExtensible;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpServer;

/**
 * An abstract HTTP Context
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Jul-2006
 */
public class DefaultHttpContext extends DefaultExtensible implements HttpContext
{
   private HttpServer server;
   private String contextRoot;

   DefaultHttpContext(HttpServer server, String contextRoot)
   {
      this.server = server;
      this.contextRoot = contextRoot;
   }

   public HttpServer getHttpServer()
   {
      return server;
   }

   public String getContextRoot()
   {
      return contextRoot;
   }
}
