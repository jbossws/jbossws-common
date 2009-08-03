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
package org.jboss.wsf.common.management;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.StackConfig;
import org.jboss.wsf.spi.management.StackConfigFactory;

/**
 * Basic implementation of a ServerConfig 
 *
 * @author Thomas.Diesler@jboss.org
 * @author darran.lofthouse@jboss.com
 * @author alessio.soldano@jboss.com
 * @since 08-May-2006
 */
public abstract class AbstractServerConfig implements AbstractServerConfigMBean, ServerConfig
{
   private static final Logger log = Logger.getLogger(AbstractServerConfig.class);
   
   protected static final ObjectName OBJECT_NAME_SERVER_CONFIG;
   static
   {
      OBJECT_NAME_SERVER_CONFIG = ObjectNameFactory.create("jboss.system:type=ServerConfig");
   }

   // The MBeanServer
   private MBeanServer mbeanServer;
   // The webservice host name that will be used when updating the wsdl
   private String webServiceHost = UNDEFINED_HOSTNAME;
   // The webservice port that will be used when updating the wsdl
   private int webServicePort;
   // The webservice port that will be used when updating the wsdl
   private int webServiceSecurePort;
   // Whether we should always modify the soap address to the deployed endpoint location
   private boolean modifySOAPAddress;
   //The stack config
   protected StackConfig stackConfig;

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

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

   public int getWebServicePort()
   {
      if (webServicePort <= 0)
         webServicePort = getConnectorPort("HTTP/1.1", false);

      int localPort = webServicePort;
      if (localPort <= 0)
      {
         // Do not initialize webServicePort with the default, the connector port may become available later 
         log.debug("Unable to calculate 'WebServicePort', using default '8080'");
         localPort = 8080;
      }

      return localPort;
   }

   public int getWebServiceSecurePort()
   {
      if (webServiceSecurePort <= 0)
         webServiceSecurePort = getConnectorPort("HTTP/1.1", true);

      int localPort = webServiceSecurePort;
      if (localPort <= 0)
      {
         // Do not initialize webServiceSecurePort with the default, the connector port may become available later 
         log.debug("Unable to calculate 'WebServiceSecurePort', using default '8443'");
         localPort = 8443;
      }

      return localPort;
   }

   public void create() throws Exception
   {
      //Retrieve the stackConfig using SPIProvider
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      this.stackConfig = spiProvider.getSPI(StackConfigFactory.class).getStackConfig();
      
      log.info(getImplementationTitle());
      log.info(getImplementationVersion());
      getMbeanServer().registerMBean(this, AbstractServerConfigMBean.OBJECT_NAME);
   }

   public void destroy() throws Exception
   {
      getMbeanServer().unregisterMBean(AbstractServerConfigMBean.OBJECT_NAME);
   }

   @SuppressWarnings("unchecked")
   private int getConnectorPort(final String protocol, final boolean secure)
   {
      int port = -1;

      try
      {
         ObjectName connectors = new ObjectName("jboss.web:type=Connector,*");

         Set connectorNames = getMbeanServer().queryNames(connectors, null);
         for (Object current : connectorNames)
         {
            ObjectName currentName = (ObjectName)current;

            try
            {
               int connectorPort = (Integer)getMbeanServer().getAttribute(currentName, "port");
               boolean connectorSecure = (Boolean)getMbeanServer().getAttribute(currentName, "secure");
               String connectorProtocol = (String)getMbeanServer().getAttribute(currentName, "protocol");

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
   
   public String getImplementationTitle()
   {
      return stackConfig.getImplementationTitle();
   }

   public String getImplementationVersion()
   {
      return stackConfig.getImplementationVersion();
   }
}
