/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.management;

import static org.jboss.ws.common.Loggers.MANAGEMENT_LOGGER;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.management.StackConfig;
import org.jboss.wsf.spi.management.StackConfigFactory;
import org.jboss.wsf.spi.management.WebServerInfo;
import org.jboss.wsf.spi.management.WebServerInfoFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;

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
   // The default endpoint configs, if any
   private final List<ClientConfig> clientConfigs = new CopyOnWriteArrayList<ClientConfig>();
   // The default endpoint configs, if any
   private final List<EndpointConfig> endpointConfigs = new CopyOnWriteArrayList<EndpointConfig>();
   
   // The server integration classloader' ServerConfig instance reference
   private static ServerConfig serverConfig;

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
         MANAGEMENT_LOGGER.usingUndefinedWebServicesHost(UNDEFINED_HOSTNAME);
         host = UNDEFINED_HOSTNAME;
      }
      if ("0.0.0.0".equals(host))
      {
         InetAddress localHost = InetAddress.getLocalHost();
         if (MANAGEMENT_LOGGER.isDebugEnabled()) MANAGEMENT_LOGGER.usingLocalHostWebServicesHost(localHost.getHostName());
         host = localHost.getHostName();
      }
      this.webServiceHost = toIPv6URLFormat("127.0.0.1".equals(host) ? "localhost" : host); // TCK workaround
   }

   private String toIPv6URLFormat(final String host)
   {
      boolean isIPv6Address = false;
      try
      {
         isIPv6Address = !UNDEFINED_HOSTNAME.equals(host) && InetAddress.getByName(host) instanceof Inet6Address;
      }
      catch (UnknownHostException e)
      {
         MANAGEMENT_LOGGER.couldNotGetAddressForHost(host, e);
         //ignore, leave isIPv6Address to false
      }
      final boolean isIPv6Formatted = isIPv6Address && host.startsWith("[");

      return isIPv6Address && !isIPv6Formatted ? "[" + host + "]" : host;
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
         webServicePort = getConnectorPort(false);

      int localPort = webServicePort;
      if (localPort <= 0)        
      {
         if (MANAGEMENT_LOGGER.isDebugEnabled()) MANAGEMENT_LOGGER.unableToCalculateWebServicesPort("8080");
         localPort = 8080;
      }

      return localPort;
   }

   public int getWebServiceSecurePort()
   {
      if (webServiceSecurePort <= 0)
         webServiceSecurePort = getConnectorPort(true);

      int localPort = webServiceSecurePort;
      if (localPort <= 0)
      {
         if (MANAGEMENT_LOGGER.isDebugEnabled()) MANAGEMENT_LOGGER.unableToCalculateWebServicesSecurePort("8443");
         localPort = 8443;
      }

      return localPort;
   }
   
   private int getConnectorPort(boolean secure) {
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      int port = 0;
      try
      {
         WebServerInfo webServerInfo = spiProvider.getSPI(WebServerInfoFactory.class, cl).newWebServerInfo();
         port = webServerInfo.getPort("HTTP/1.1", secure);
      }
      catch (WSFException e)
      {
         MANAGEMENT_LOGGER.couldNotGetPortFromConfiguredHTTPConnector();
      }
      return port;
   }

   public void create() throws Exception
   {
      //Retrieve the stackConfig using SPIProvider
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      this.stackConfig = spiProvider.getSPI(StackConfigFactory.class, cl).getStackConfig();

      MANAGEMENT_LOGGER.startingWSServerConfig(getImplementationTitle(), getImplementationVersion());
      MBeanServer mbeanServer = getMbeanServer();
      if (mbeanServer != null) {
         mbeanServer.registerMBean(this, AbstractServerConfigMBean.OBJECT_NAME);
      }
   }

   public void destroy() throws Exception
   {
      MBeanServer mbeanServer = getMbeanServer();
      if (mbeanServer != null) {
         mbeanServer.unregisterMBean(AbstractServerConfigMBean.OBJECT_NAME);
      }
   }
   
   public static synchronized ServerConfig getServerIntegrationServerConfig()
   {
       if (serverConfig == null)
       {
           final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
           SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
           serverConfig = spiProvider.getSPI(ServerConfigFactory.class, cl).getServerConfig();
       }
       return serverConfig;
   }
   
   public String getImplementationTitle()
   {
      return stackConfig.getImplementationTitle();
   }

   public String getImplementationVersion()
   {
      return stackConfig.getImplementationVersion();
   }

   public void addEndpointConfig(EndpointConfig config)
   {
      this.endpointConfigs.add(config);
   }

   public void addClientConfig(ClientConfig config)
   {
      this.clientConfigs.add(config);
   }

   public List<EndpointConfig> getEndpointConfigs()
   {
      return this.endpointConfigs;
   }

   public List<ClientConfig> getClientConfigs()
   {
      return this.clientConfigs;
   }
}