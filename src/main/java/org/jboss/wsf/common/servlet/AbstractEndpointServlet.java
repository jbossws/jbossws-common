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

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.spi.DeploymentAspectManagerLocator;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Endpoint.EndpointState;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.management.EndpointResolver;

import javax.xml.ws.WebServiceException;

/**
 * A cross stack webservice endpoint servlet.
 * Backward compatible mode is used on JBoss AS 4.2 series.
 * Not backward compatible mode is used on JBoss AS 5.0 series
 * @author thomas.diesler@jboss.org
 * @author heiko.braun@jboss.com
 * @author richard.opalka@jboss.com
 */
public abstract class AbstractEndpointServlet extends HttpServlet
{

   private static final String PROPERTY_NAME = "org.jboss.ws.webapp.ServletAspectManagerName";
   private final SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
   protected Endpoint endpoint;
   private EndpointRegistry epRegistry;
   private DeploymentAspectManager aspectsManager;
   private boolean backwardCompatibilityMode;
   
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
    * Servlet lifecycle destroy method
    */
   public final void destroy()
   {
      try
      {
         this.stopAspectManager();
         this.stopEndpoint();
      }
      finally
      {
         super.destroy();
      }
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
         EndpointAssociation.removeEndpoint();
      }
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
    * Initializes aspect manager if not backward compatible mode
    */
   private void initAspectManager()
   {
      final String managerName = (String)this.endpoint.getService().getDeployment().getProperty(PROPERTY_NAME);
      if (managerName == null)
      {
         this.backwardCompatibilityMode = true;
      }
      
      if (!this.backwardCompatibilityMode)
      {
         final DeploymentAspectManagerLocator locator = this.spiProvider.getSPI(DeploymentAspectManagerLocator.class);
         this.aspectsManager = locator.locateDeploymentAspectManager(managerName);
      }
   }
   
   /**
    * Starts servlet related aspects if not backward compatible mode
    */
   private void startAspectManager()
   {
      if (!this.backwardCompatibilityMode)
      {
         this.assertCorrectState();

         final Deployment dep = this.endpoint.getService().getDeployment();

         if (this.endpoint.getState() != EndpointState.STARTED) // [JBWS-2338] TODO fix this hack preventing exceptions
         {
            this.aspectsManager.deploy(dep);
         }
      }
   }
   
   /**
    * Stops servlet related aspects if not backward compatible mode
    */
   private void stopAspectManager()
   {
      if (!this.backwardCompatibilityMode)
      {
         this.assertCorrectState();

         final Deployment dep = this.endpoint.getService().getDeployment();

         if (this.endpoint.getState() == EndpointState.STARTED) // [JBWS-2338] TODO fix this hack preventing exceptions
         {
            try
            {
               this.aspectsManager.undeploy(dep);
            }
            finally
            {
               this.aspectsManager = null;
            }
         }
      }
   }

   /**
    * Fires endpoint start event if not backward compatible mode
    */
   private void startEndpoint()
   {
      if (!this.backwardCompatibilityMode)
      {
         Deployment dep = this.endpoint.getService().getDeployment();
         for (Endpoint ep : dep.getService().getEndpoints())
         {
            ep.getLifecycleHandler().start(ep); // [JBWS-2338] TODO fix this hack preventing exceptions
         }
      }
   }

   /**
    * Fires endpoint stop event if not backward compatible mode
    */
   private void stopEndpoint()
   {
      if (!this.backwardCompatibilityMode)
      {
         if (this.endpoint.getState() == EndpointState.STARTED)
         {
            Deployment dep = this.endpoint.getService().getDeployment();
            for (Endpoint ep : dep.getService().getEndpoints())
            {
               ep.getLifecycleHandler().stop(ep); // [JBWS-2338] TODO fix this hack preventing exceptions
            }
         }
      }
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
      this.initAspectManager();
      this.setRuntimeLoader();
      this.startAspectManager();
      this.postInit(servletConfig);
      this.startEndpoint();
   }

   /**
    * Initializes endpoint registry
    */
   private void initRegistry()
   {
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();
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
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         dep.setRuntimeClassLoader(classLoader);
      }
   }
   
   /**
    * Asserts this object correct state
    */
   private void assertCorrectState()
   {
      if (this.endpoint == null || this.aspectsManager == null)
      {
         throw new IllegalStateException();
      }
   }
   
}
