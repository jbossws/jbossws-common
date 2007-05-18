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
package org.jboss.wsf.spi.management;

//$Id$

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.utils.ObjectNameFactory;

/**
 * JBoss specific implementation of a ServerConfig 
 *
 * @author Thomas.Diesler@jboss.org
 * @author darran.lofthouse@jboss.com
 * @since 08-May-2006
 */
public class BasicServerConfig implements ServerConfig
{
   private static final Logger log = Logger.getLogger(BasicServerConfig.class);

   // The webservice host name that will be used when updating the wsdl
   private String webServiceHost = UNDEFINED_HOSTNAME;
   // The webservice port that will be used when updating the wsdl
   private int webServicePort;
   // The webservice port that will be used when updating the wsdl
   private int webServiceSecurePort;
   // Whether we should always modify the soap address to the deployed endpoing location
   private boolean modifySOAPAddress;
   // The MBeanServer
   private MBeanServer server;

   public String getWebServiceHost()
   {
      return webServiceHost;
   }

   public void setWebServiceHost(String host) throws UnknownHostException
   {
      if (host == null || host.trim().length() == 0)
      {
         log.debug("Using undefined host: " + UNDEFINED_HOSTNAME);
         host = UNDEFINED_HOSTNAME;
      }
      if ("0.0.0.0".equals(host))
      {
         InetAddress localHost = InetAddress.getLocalHost();
         log.debug("Using local host: " + localHost.getHostName());
         host = localHost.getHostName();
      }
      this.webServiceHost = host;
   }

   public void setWebServicePort(int port)
   {
      this.webServicePort = port;
   }

   public void setWebServiceSecurePort(int port)
   {
      this.webServiceSecurePort = port;
   }

   public boolean isModifySOAPAddress()
   {
      return modifySOAPAddress;
   }

   public void setModifySOAPAddress(boolean modify)
   {
      this.modifySOAPAddress = modify;
   }

   public File getServerTempDir()
   {
      try
      {
         MBeanServer server = getMBeanServer();
         ObjectName oname = ObjectNameFactory.create("jboss.system:type=ServerConfig");
         File tmpdir = (File)server.getAttribute(oname, "ServerTempDir");
         return tmpdir;
      }
      catch (JMException e)
      {
         return null;
      }
   }

   public File getServerDataDir()
   {
      try
      {
         MBeanServer server = getMBeanServer();
         ObjectName oname = ObjectNameFactory.create("jboss.system:type=ServerConfig");
         File tmpdir = (File)server.getAttribute(oname, "ServerDataDir");
         return tmpdir;
      }
      catch (JMException e)
      {
         return null;
      }
   }

   public int getWebServicePort()
   {
      if (webServicePort <= 0)
         webServicePort = getConnectorPort("HTTP/1.1", false);

      if (webServicePort <= 0)
      {
         log.warn("Unable to calculate 'WebServicePort', using default '8080'");
         webServicePort = 8080;
      }

      return webServicePort;
   }

   public int getWebServiceSecurePort()
   {
      if (webServiceSecurePort <= 0)
         webServiceSecurePort = getConnectorPort("HTTP/1.1", true);

      if (webServiceSecurePort <= 0)
      {
         log.warn("Unable to calculate 'WebServiceSecurePort', using default '8443'");
         webServiceSecurePort = 8443;
      }

      return webServiceSecurePort;
   }

   private int getConnectorPort(final String protocol, final boolean secure)
   {
      int port = -1;

      try
      {
         MBeanServer server = getMBeanServer();
         ObjectName connectors = new ObjectName("jboss.web:type=Connector,*");

         Set connectorNames = server.queryNames(connectors, null);
         for (Object current : connectorNames)
         {
            ObjectName currentName = (ObjectName)current;

            try
            {
               int connectorPort = (Integer)server.getAttribute(currentName, "port");
               boolean connectorSecure = (Boolean)server.getAttribute(currentName, "secure");
               String connectorProtocol = (String)server.getAttribute(currentName, "protocol");

               if (protocol.equals(connectorProtocol) && secure == connectorSecure)
               {
                  if (port > -1)
                  {
                     log.warn("Found multiple connectors for protocol='" + protocol + "' and secure='" + secure + "', using first port found '" + port + "'");
                  }
                  else
                  {
                     port = connectorPort;
                  }
               }
            }
            catch (AttributeNotFoundException ignored)
            {
            }
         }

         return port;
      }
      catch (JMException e)
      {
         return -1;
      }
   }

   private MBeanServer getMBeanServer()
   {
      if (server == null)
      {
         for (Iterator i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext();)
         {
            server = (MBeanServer)i.next();
         }
      }
      return server;
   }
}
