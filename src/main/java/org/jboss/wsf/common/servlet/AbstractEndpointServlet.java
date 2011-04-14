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
package org.jboss.wsf.common.servlet;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.management.EndpointResolver;

import javax.xml.ws.WebServiceException;

/**
 * A cross stack webservice endpoint servlet.
 * @author thomas.diesler@jboss.org
 * @author heiko.braun@jboss.com
 * @author richard.opalka@jboss.com
 */
public abstract class AbstractEndpointServlet extends HttpServlet
{

   protected Endpoint endpoint;
   private EndpointRegistry epRegistry;
   
   /**
    * Constructor
    */
   protected AbstractEndpointServlet() {}

   /**
    * Servlet lifecycle init method
    * @param servletConfig servlet configuration
    */
   public final void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      this.initRegistry();
      this.initServiceEndpoint(servletConfig);
   }
   
   /**
    * Serves the requests
    */
   public final void service(HttpServletRequest req, HttpServletResponse res)
   throws ServletException, IOException
   {
      try
      {
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = endpoint.getRequestHandler();
         requestHandler.handleHttpRequest(endpoint, req, res, getServletContext());
      }
      finally
      {
         this.postService();
         EndpointAssociation.removeEndpoint();
      }
   }
   
   /**
    * Template method
    */
   protected void postService()
   {
      // does nothing (because of BC)
   }
   
   /**
    * Template method
    * @param cfg servlet config
    */
   protected void postInit(ServletConfig cfg)
   {
      // does nothing (because of BC)
   }
   
   /**
    * Abstract method that must be overriden by each stack servlet endpoint
    * @param servletContext servlet context
    * @param servletName servlet name
    * @return new stack specific endpoint resolver
    */
   protected abstract EndpointResolver newEndpointResolver(String servletContext, String servletName);

   /**
    * Initialize the service endpoint
    */
   private void initServiceEndpoint(ServletConfig servletConfig)
   {
      this.initEndpoint(servletConfig.getServletContext().getContextPath(), getServletName());
      this.setRuntimeLoader();
      this.postInit(servletConfig);
   }

   /**
    * Initializes endpoint registry
    */
   private void initRegistry()
   {
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class, cl).getEndpointRegistry();
   }   

   /**
    * Initialize the service endpoint
    * @param contextPath context path
    * @param servletName servlet name
    */
   private void initEndpoint(String contextPath, String servletName)
   {
      final EndpointResolver resolver = newEndpointResolver(contextPath, servletName);
      this.endpoint = epRegistry.resolve(resolver);

      if (this.endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" +
           Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextPath + "," +
           Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + servletName
         );
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }
   }
   
   /**
    * Sets runtime classloader for JSE endpoints
    */
   private void setRuntimeLoader()
   {
      final Deployment dep = endpoint.getService().getDeployment();
      final boolean isJaxrpcJse = dep.getType() == Deployment.DeploymentType.JAXRPC_JSE;
      final boolean isJaxwsJse = dep.getType() == Deployment.DeploymentType.JAXWS_JSE;

      if (isJaxrpcJse || isJaxwsJse)
      {
         ClassLoader classLoader = getContextClassLoader();
         dep.setRuntimeClassLoader(classLoader);
      }
   }
   
   private static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
}
