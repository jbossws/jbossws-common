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
package org.jboss.wsf.test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.Principal;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class TestDeployerJBoss implements TestDeployer
{
   private static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

   private MBeanServerConnection server;
   private String username;
   private String password;

   public TestDeployerJBoss(MBeanServerConnection server)
   {
      this.server = server;

      username = System.getProperty("jmx.authentication.username");
      if ("${jmx.authentication.username}".equals(username))
         username = null;

      password = System.getProperty("jmx.authentication.password");
      if ("${jmx.authentication.password}".equals(password))
         password = null;
   }

   public void deploy(URL url) throws Exception
   {
      invokeMainDeployer("deploy", url);
   }

   public void undeploy(URL url) throws Exception
   {
      invokeMainDeployer("undeploy", url);
   }

   private void invokeMainDeployer(String methodName, URL url) throws Exception
   {
      Principal prevUsername = null;
      Object prevPassword = null;

      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      SecurityAdaptor securityAdaptor = spiProvider.getSPI(SecurityAdaptorFactory.class).newSecurityAdapter();
      if (username != null || password != null)
      {
         prevUsername = securityAdaptor.getPrincipal();
         prevPassword = securityAdaptor.getCredential();
         securityAdaptor.setPrincipal(new SimplePrincipal(username));
         securityAdaptor.setCredential(password);
      }

      try
      {
         server.invoke(new ObjectName(MAIN_DEPLOYER), methodName, new Object[] { url }, new String[] { "java.net.URL" });
      }
      finally
      {
         if (username != null || password != null)
         {
            securityAdaptor.setPrincipal(prevUsername);
            securityAdaptor.setCredential(prevPassword);
         }
      }
   }

   public static class SimplePrincipal implements Principal, Serializable
   {
      private String name;

      public SimplePrincipal(String name)
      {
         this.name = name;
      }

      public String getName()
      {
         return name;
      }
   }
}
