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

//$Id: JBossHttpServer.java 1786 2007-01-04 14:30:04Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;
import org.jboss.wsf.framework.DefaultExtensible;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpContextFactory;
import org.jboss.wsf.spi.http.HttpServer;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.w3c.dom.Element;

/**
 * A Tomcat HTTP Server
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Jul-2006
 */
public class WebAppDeployingHttpServer extends DefaultExtensible implements HttpServer
{
   private static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

   /** Start an instance of this HTTP server */
   public void start()
   {
      // verify required properties
   }

   /** Create an HTTP context */
   public HttpContext createContext(String contextRoot)
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      HttpContext httpContext = spiProvider.getSPI(HttpContextFactory.class).newHttpContext(this, contextRoot);
      return httpContext;
   }

   /** Publish an JAXWS endpoint to the HTTP server */
   public void publish(HttpContext context, Endpoint endpoint)
   {
      Class implClass = getImplementorClass(endpoint);
      String implName = implClass.getName();

      try
      {
         Element webDoc = createWebAppDescriptor(context, endpoint);
         Element jbossDoc = createJBossWebAppDescriptor(context, endpoint);

         File tmpWar = null;
         try
         {
            SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
            ServerConfig serverConfig = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
            File tmpDir = new File(serverConfig.getServerTempDir().getCanonicalPath() + "/jbossws");

            String deploymentName = implName.substring(implName.lastIndexOf(".") + 1);
            tmpWar = File.createTempFile(deploymentName, ".war", tmpDir);
            tmpWar.delete();
            File webInf = new File(tmpWar, "WEB-INF");
            webInf.mkdirs();

            File webXml = new File(webInf, "web.xml");
            FileWriter fw = new FileWriter(webXml);
            new DOMWriter(fw).setPrettyprint(true).print(webDoc);
            fw.close();

            File jbossWebXml = new File(webInf, "jboss-web.xml");
            fw = new FileWriter(jbossWebXml);
            new DOMWriter(fw).setPrettyprint(true).print(jbossDoc);
            fw.close();
         }
         catch (IOException e)
         {
            throw new WebServiceException("Failed to create webservice war", e);
         }

         Map<String, Object> epProps = endpoint.getProperties();
         epProps.put("jbossws-endpoint-war-url", tmpWar);

         URL tmpURL = tmpWar.toURL();
         MBeanServerConnection server = getServer();
         server.invoke(new ObjectName(MAIN_DEPLOYER), "deploy", new Object[] { tmpURL }, new String[] { "java.net.URL" });
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException(ex);
      }
   }

   /** Destroys an JAXWS endpoint on the HTTP server */
   public void destroy(HttpContext context, Endpoint endpoint)
   {
      Map<String, Object> epProps = endpoint.getProperties();
      File tmpWar = (File)epProps.get("jbossws-endpoint-war-url");
      if (tmpWar == null)
         throw new IllegalStateException("Cannot find endpoint war property");

      try
      {
         URL tmpURL = tmpWar.toURL();
         MBeanServerConnection server = getServer();
         server.invoke(new ObjectName(MAIN_DEPLOYER), "undeploy", new Object[] { tmpURL }, new String[] { "java.net.URL" });

         tmpWar.delete();
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException(ex);
      }
   }

   private Class getImplementorClass(Endpoint endpoint)
   {
      Object implementor = endpoint.getImplementor();
      Class implClass = (implementor instanceof Class ? (Class)implementor : implementor.getClass());
      return implClass;
   }

   private MBeanServerConnection getServer() throws NamingException
   {
      InitialContext iniCtx = new InitialContext();
      MBeanServerConnection server = (MBeanServerConnection)iniCtx.lookup("jmx/invoker/RMIAdaptor");
      return server;
   }

   private Element createWebAppDescriptor(HttpContext context, Endpoint endpoint)
   {
      Class implClass = getImplementorClass(endpoint);
      String implName = implClass.getName();

      Element webApp = DOMUtils.createElement("web-app");

      /*
       <servlet>
       <servlet-name>
       <servlet-class>
       </servlet>
       */
      Element servlet = (Element)webApp.appendChild(DOMUtils.createElement("servlet"));
      Element servletName = (Element)servlet.appendChild(DOMUtils.createElement("servlet-name"));
      servletName.appendChild(DOMUtils.createTextNode("JAXWSEndpoint"));
      Element servletClass = (Element)servlet.appendChild(DOMUtils.createElement("servlet-class"));
      servletClass.appendChild(DOMUtils.createTextNode(implName));

      /*
       <servlet-mapping>
       <servlet-name>
       <url-pattern>
       </servlet-mapping>
       */
      Element servletMapping = (Element)webApp.appendChild(DOMUtils.createElement("servlet-mapping"));
      servletName = (Element)servletMapping.appendChild(DOMUtils.createElement("servlet-name"));
      servletName.appendChild(DOMUtils.createTextNode("JAXWSEndpoint"));
      Element urlPatternElement = (Element)servletMapping.appendChild(DOMUtils.createElement("url-pattern"));

      String urlPattern = "/*";
      urlPatternElement.appendChild(DOMUtils.createTextNode(urlPattern));

      // Add security-constraint in generated web.xml for Endpoint API
      // FIXME: JBWS-1069

      return webApp;
   }

   private Element createJBossWebAppDescriptor(HttpContext context, Endpoint endpoint)
   {
      /* Create a jboss-web
       <jboss-web>
       <security-domain>java:/jaas/cts</security-domain>
       <context-root>/ws/ejbN/</context-root>
       </jboss-web>
       */
      Element jbossWeb = DOMUtils.createElement("jboss-web");

      // Get the context root for this deployment
      String contextRoot = context.getContextRoot();
      if (contextRoot == null)
         throw new WebServiceException("Cannot obtain context root");

      Element root = (Element)jbossWeb.appendChild(DOMUtils.createElement("context-root"));
      root.appendChild(DOMUtils.createTextNode(contextRoot));

      // Add security-constraint in generated web.xml for Endpoint API
      // FIXME: JBWS-1069

      return jbossWeb;
   }
}

