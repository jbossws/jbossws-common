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

// $Id$

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.wsf.common.ObjectNameFactory;

/**
 * A JBossWS test helper that deals with test deployment/undeployment, etc.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossWSTestHelper
{
   private static final String SYSPROP_JBOSSWS_INTEGRATION_TARGET = "jbossws.integration.target";
   private static final String SYSPROP_JBOSS_BIND_ADDRESS = "jboss.bind.address";
   private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";
   private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";

   private static MBeanServerConnection server;
   private static String integrationTarget;
   private static String implVendor;
   private static String implTitle;
   private static String implVersion;
   private static String testArchiveDir;
   private static String testResourcesDir;

   /** Deploy the given archive
    */
   public void deploy(String archive) throws Exception
   {
      URL url = getArchiveFile(archive).toURL();
      getDeployer().deploy(url);
   }

   /** Undeploy the given archive
    */
   public void undeploy(String archive) throws Exception
   {
      URL url = getArchiveFile(archive).toURL();
      getDeployer().undeploy(url);
   }

   /** True, if -Djbossws.integration.target=jboss50x */
   public boolean isTargetJBoss50()
   {
      String target = getIntegrationTarget();
      return target.startsWith("jboss50");
   }

   /** True, if -Djbossws.integration.target=jboss42x */
   public boolean isTargetJBoss42()
   {
      String target = getIntegrationTarget();
      return target.startsWith("jboss42");
   }

   /** True, if -Djbossws.integration.target=jboss40x */
   public boolean isTargetJBoss40()
   {
      String target = getIntegrationTarget();
      return target.startsWith("jboss40");
   }

   public boolean isIntegrationNative()
   {
      String vendor = getImplementationVendor();
      if (vendor == null)
      {
         System.out.println("FIXME: [CXF-1506] - No Implementation-Vendor in manifest");
         return false;
      }
      return vendor.indexOf("JBoss") != -1;
   }

   public boolean isIntegrationMetro()
   {
      String vendor = getImplementationVendor();
      if (vendor == null)
      {
         System.out.println("FIXME: [CXF-1506] - No Implementation-Vendor in manifest");
         return false;
      }
      return vendor.indexOf("Sun") != -1;
   }

   public boolean isIntegrationCXF()
   {
      String vendor = getImplementationVendor();
      if (vendor == null)
      {
         System.out.println("FIXME: [CXF-1506] - No Implementation-Vendor in manifest");
         return true;
      }
      return vendor.indexOf("Apache") != -1;
   }

   private String getImplementationVendor()
   {
      if (implVendor == null)
      {
         Service service = Service.create(new QName("dummyService"));
         Object obj = service.getHandlerResolver();
         if (obj == null)
         {
            service.addPort(new QName("dummyPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://dummy-address");
            obj = service.createDispatch(new QName("dummyPort"), Source.class, Mode.PAYLOAD);
         }
         implVendor = obj.getClass().getPackage().getImplementationVendor();
         implTitle = obj.getClass().getPackage().getImplementationTitle();
         implVersion = obj.getClass().getPackage().getImplementationVersion();
         System.out.println(implVendor + ", " + implTitle + ", " + implVersion);
      }
      return implVendor;
   }

   /**
    * Get the JBoss server host from system property "jboss.bind.address"
    * This defaults to "localhost"
    */
   public static String getServerHost()
   {
      return System.getProperty(SYSPROP_JBOSS_BIND_ADDRESS, "localhost");
   }

   @SuppressWarnings("unchecked")
   public static MBeanServerConnection getServer()
   {
      if (server == null)
      {
         Hashtable jndiEnv = null;
         try
         {
            InitialContext iniCtx = new InitialContext();
            jndiEnv = iniCtx.getEnvironment();
            server = (MBeanServerConnection)iniCtx.lookup("jmx/invoker/RMIAdaptor");
         }
         catch (NamingException ex)
         {
            throw new RuntimeException("Cannot obtain MBeanServerConnection using jndi props: " + jndiEnv, ex);
         }
      }
      return server;
   }

   private TestDeployer getDeployer()
   {
      return new TestDeployerJBoss(getServer());
   }

   public String getIntegrationTarget()
   {
      if (integrationTarget == null)
      {
         integrationTarget = System.getProperty(SYSPROP_JBOSSWS_INTEGRATION_TARGET);

         if (integrationTarget == null)
            throw new IllegalStateException("Cannot obtain system property: " + SYSPROP_JBOSSWS_INTEGRATION_TARGET);

         // Read the JBoss SpecificationVersion
         String jbossVersion = null;
         try
         {
            ObjectName oname = ObjectNameFactory.create("jboss.system:type=ServerConfig");
            jbossVersion = (String)getServer().getAttribute(oname, "SpecificationVersion");
            if (jbossVersion.startsWith("5.0"))
               jbossVersion = "jboss50";
            else if (jbossVersion.startsWith("4.2"))
               jbossVersion = "jboss42";
            else if (jbossVersion.startsWith("4.0"))
               jbossVersion = "jboss40";
            else throw new RuntimeException("Unsupported jboss version: " + jbossVersion);
         }
         catch (Throwable th)
         {
            // ignore, we are not running on jboss-4.2 or greater
         }

         if (integrationTarget.startsWith(jbossVersion) == false)
            throw new IllegalStateException("Integration target mismatch: " + integrationTarget + ".startsWith(" + jbossVersion + ")");
      }
      return integrationTarget;
   }

   /** Try to discover the URL for the deployment archive */
   public URL getArchiveURL(String archive) throws MalformedURLException
   {
      return getArchiveFile(archive).toURL();
   }
   
   /** Try to discover the File for the deployment archive */
   public File getArchiveFile(String archive)
   {
      File file = new File(archive);
      if (file.exists())
         return file;

      file = new File(getTestArchiveDir() + "/" + archive);
      if (file.exists())
         return file;

      String notSet = (getTestArchiveDir() == null ? " System property '" + SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set." : "");
      throw new IllegalArgumentException("Cannot obtain '" + getTestArchiveDir() + "/" + archive + "'." + notSet);
   }

   /** Try to discover the URL for the test resource */
   public URL getResourceURL(String resource) throws MalformedURLException
   {
      return getResourceFile(resource).toURL();
   }
   
   /** Try to discover the File for the test resource */
   public File getResourceFile(String resource)
   {
      File file = new File(resource);
      if (file.exists())
         return file;

      file = new File(getTestResourcesDir() + "/" + resource);
      if (file.exists())
         return file;

      String notSet = (getTestResourcesDir() == null ? " System property '" + SYSPROP_TEST_RESOURCES_DIRECTORY + "' not set." : "");
      throw new IllegalArgumentException("Cannot obtain '" + getTestResourcesDir() + "/" + resource + "'." + notSet);
   }

   public static String getTestArchiveDir()
   {
      if (testArchiveDir == null)
         testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY);

      return testArchiveDir;
   }

   public static String getTestResourcesDir()
   {
      if (testResourcesDir == null)
         testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY);

      return testResourcesDir;
   }
}
