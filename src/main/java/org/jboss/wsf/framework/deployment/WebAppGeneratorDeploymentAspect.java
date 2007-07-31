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
package org.jboss.wsf.framework.deployment;

//$Id$

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.annotation.WebContext;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.SecurityHandler;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBSecurityMetaData;

/**
 * A deployer that generates a webapp for an EJB endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class WebAppGeneratorDeploymentAspect extends DeploymentAspect
{
   private SecurityHandler securityHandlerEJB21;
   private SecurityHandler securityHandlerEJB3;

   public void setSecurityHandlerEJB21(SecurityHandler handler)
   {
      this.securityHandlerEJB21 = handler;
   }

   public void setSecurityHandlerEJB3(SecurityHandler handler)
   {
      this.securityHandlerEJB3 = handler;
   }

   @Override
   public void create(Deployment dep)
   {
      String typeStr = dep.getType().toString();
      if (typeStr.endsWith("EJB21"))
      {
         URL webAppURL = generatWebDeployment((ArchiveDeployment)dep, securityHandlerEJB21);
         dep.setProperty("org.jboss.ws.webapp.url", webAppURL);
      }
      else if (typeStr.endsWith("EJB3"))
      {
         URL webAppURL = generatWebDeployment((ArchiveDeployment)dep, securityHandlerEJB3);
         dep.setProperty("org.jboss.ws.webapp.url", webAppURL);
      }
      else 
      {
         URL webAppURL = generatWebDeployment((ArchiveDeployment)dep, null);
         dep.setProperty("org.jboss.ws.webapp.url", webAppURL);
      }
   }

   private URL generatWebDeployment(ArchiveDeployment dep, SecurityHandler securityHandler)
   {
      Document webDoc = createWebAppDescriptor(dep, securityHandler);
      Document jbossDoc = createJBossWebAppDescriptor(dep, securityHandler);

      File tmpWar = null;
      try
      {
         // TODO: recursive dependency, ohoh
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         ServerConfig config = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
         File tmpdir = new File(config.getServerTempDir().getCanonicalPath() + "/deploy");

         String deploymentName = dep.getCanonicalName().replace('/', '-');
         tmpWar = File.createTempFile(deploymentName, ".war", tmpdir);
         tmpWar.delete();

         File webInf = new File(tmpWar, "WEB-INF");
         webInf.mkdirs();

         File webXml = new File(webInf, "web.xml");
         FileWriter fw = new FileWriter(webXml);
         OutputFormat format = OutputFormat.createPrettyPrint();
         XMLWriter writer = new XMLWriter(fw, format);
         writer.write(webDoc);
         writer.close();

         File jbossWebXml = new File(webInf, "jboss-web.xml");
         fw = new FileWriter(jbossWebXml);
         writer = new XMLWriter(fw, format);
         writer.write(jbossDoc);
         writer.close();

         return tmpWar.toURL();
      }
      catch (IOException e)
      {
         throw new WSFDeploymentException("Failed to create webservice.war", e);
      }
   }

   private Document createWebAppDescriptor(Deployment dep, SecurityHandler securityHandler)
   {
      Document document = DocumentHelper.createDocument();
      Element webApp = document.addElement("web-app");

      /*
       <servlet>
       <servlet-name>
       <servlet-class>
       </servlet>
       */
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         Element servlet = webApp.addElement("servlet");
         servlet.addElement("servlet-name").addText(ep.getShortName());
         servlet.addElement("servlet-class").addText(ep.getTargetBeanName());
      }

      /*
       <servlet-mapping>
       <servlet-name>
       <url-pattern>
       </servlet-mapping>
       */
      ArrayList urlPatters = new ArrayList();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         Element servletMapping = webApp.addElement("servlet-mapping");
         servletMapping.addElement("servlet-name").addText(ep.getShortName());
         servletMapping.addElement("url-pattern").addText(ep.getURLPattern());
      }

      String authMethod = null;

      // Add web-app/security-constraint for each port component
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         String ejbName = ep.getShortName();

         Boolean secureWSDLAccess = null;
         String transportGuarantee = null;
         String beanAuthMethod = null;

         WebContext anWebContext = (WebContext)ep.getTargetBeanClass().getAnnotation(WebContext.class);
         if (anWebContext != null)
         {
            if (anWebContext.authMethod().length() > 0)
               beanAuthMethod = anWebContext.authMethod();
            if (anWebContext.transportGuarantee().length() > 0)
               transportGuarantee = anWebContext.transportGuarantee();
            if (anWebContext.secureWSDLAccess())
               secureWSDLAccess = anWebContext.secureWSDLAccess();
         }

         EJBArchiveMetaData appMetaData = dep.getAttachment(EJBArchiveMetaData.class);
         if (appMetaData != null && appMetaData.getBeanByEjbName(ejbName) != null)
         {
            EJBMetaData bmd = appMetaData.getBeanByEjbName(ejbName);
            EJBSecurityMetaData smd = bmd.getSecurityMetaData();
            if (smd != null)
            {
               beanAuthMethod = smd.getAuthMethod();
               transportGuarantee = smd.getTransportGuarantee();
               secureWSDLAccess = smd.getSecureWSDLAccess();
            }
         }

         if (beanAuthMethod != null || transportGuarantee != null)
         {
            /*
             <security-constraint>
             <web-resource-collection>
             <web-resource-name>TestUnAuthPort</web-resource-name>
             <url-pattern>/HSTestRoot/TestUnAuth/*</url-pattern>
             </web-resource-collection>
             <auth-constraint>
             <role-name>*</role-name>
             </auth-constraint>
             <user-data-constraint>
             <transport-guarantee>NONE</transport-guarantee>
             </user-data-constraint>
             </security-constraint>
             */
            Element securityConstraint = webApp.addElement("security-constraint");
            Element wrc = securityConstraint.addElement("web-resource-collection");
            wrc.addElement("web-resource-name").addText(ejbName);
            wrc.addElement("url-pattern").addText(ep.getURLPattern());
            if (Boolean.TRUE.equals(secureWSDLAccess))
            {
               wrc.addElement("http-method").addText("GET");
            }
            wrc.addElement("http-method").addText("POST");

            // Optional auth-constraint
            if (beanAuthMethod != null)
            {
               // Only the first auth-method gives the war login-config/auth-method
               if (authMethod == null)
                  authMethod = beanAuthMethod;

               Element authConstraint = securityConstraint.addElement("auth-constraint").addElement("role-name").addText("*");
            }
            // Optional user-data-constraint
            if (transportGuarantee != null)
            {
               Element userData = securityConstraint.addElement("user-data-constraint");
               userData.addElement("transport-guarantee").addText(transportGuarantee);
            }
         }
      }

      // Optional login-config/auth-method
      if (authMethod != null && securityHandler != null)
      {
         Element loginConfig = webApp.addElement("login-config");
         loginConfig.addElement("auth-method").addText(authMethod);
         loginConfig.addElement("realm-name").addText("EJBServiceEndpointServlet Realm");

         securityHandler.addSecurityRoles(webApp, dep);
      }

      return document;
   }

   private Document createJBossWebAppDescriptor(Deployment dep, SecurityHandler securityHandler)
   {
      Document document = DocumentHelper.createDocument();

      /* Create a jboss-web
       <jboss-web>
       <security-domain>java:/jaas/cts</security-domain>
       <context-root>/ws/ejbN/</context-root>
       <virtual-host>some.domain.com</virtual-host>
       </jboss-web>
       */
      Element jbossWeb = document.addElement("jboss-web");

      if (securityHandler != null)
         securityHandler.addSecurityDomain(jbossWeb, dep);

      // Get the context root for this deployment
      String contextRoot = dep.getService().getContextRoot();
      if (contextRoot == null)
         throw new WSFDeploymentException("Cannot obtain context root");

      jbossWeb.addElement("context-root").addText(contextRoot);

      return document;
   }
}