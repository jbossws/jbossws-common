/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.jboss.wsf.test;

import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.wsf.spi.deployer.Deployer;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 * 
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DeployerJBoss6 implements Deployer
{

   private static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

   private MBeanServerConnection server;

   public DeployerJBoss6()
   {
      this.server = JBossWSTestHelper.getServer();
   }

   public void deploy(final URL url) throws Exception
   {
      invokeMainDeployer("deploy", url);
   }

   public void undeploy(final URL url) throws Exception
   {
      invokeMainDeployer("undeploy", url);
   }

   private void invokeMainDeployer(final String methodName, final URL url) throws Exception
   {
      server.invoke(new ObjectName(MAIN_DEPLOYER), methodName, new Object[]
      {url}, new String[]
      {"java.net.URL"});
   }

}
