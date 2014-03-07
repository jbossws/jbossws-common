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
import static org.jboss.ws.common.Messages.MESSAGES;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

import javax.management.MBeanServer;

import org.jboss.ws.common.utils.AddressUtils;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.CommonConfigStore;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.StackConfig;
import org.jboss.wsf.spi.management.StackConfigFactory;
import org.jboss.wsf.spi.management.WebServerInfo;
import org.jboss.wsf.spi.management.WebServerInfoFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;

/**
 * Basic implementation of a ServerConfig.
 * 
 * Instances of AbstractServerConfig allow concurrent read and write access to their
 * member attributes using getter and setter methods.
 * A DisabledOperationException is thrown if attribute updates are temporarly or
 * permanentely disabled. The isModifiable() method can be overwridden to enable / disable
 * the attribute update.
 *
 * @author alessio.soldano@jboss.com
 * @author Thomas.Diesler@jboss.org
 * @author darran.lofthouse@jboss.com
 * @since 08-May-2006
 */
public abstract class AbstractServerConfig implements AbstractServerConfigMBean, ServerConfig
{
   private static final RuntimePermission LOOKUP_SERVER_INTEGRATION_SERVER_CONFIG = new RuntimePermission("org.jboss.ws.LOOKUP_SERVER_INTEGRATION_SERVER_CONFIG");
   
   // The MBeanServer
   private volatile MBeanServer mbeanServer;
   
   // The webservice host name that will be used when updating the wsdl
   private volatile String webServiceHost = UNDEFINED_HOSTNAME;
   private final Object webServiceHostLock = new Object();
   
   // The webservice port that will be used when updating the wsdl
   private int webServicePort;
   private final Object webServicePortLock = new Object();
   
   // The webservice port that will be used when updating the wsdl
   private int webServiceSecurePort;
   private final Object webServiceSecurePortLock = new Object();
   
   // Whether we should always modify the soap address to the deployed endpoint location
   private volatile boolean modifySOAPAddress;
   private final Object modifySOAPAddressLock = new Object();
   
   //The stack config
   protected volatile StackConfig stackConfig;
   
   protected final CommonConfigStore<ClientConfig> clientConfigStore = new CommonConfigStoreImpl<ClientConfig>();
   protected final CommonConfigStore<EndpointConfig> endpointConfigStore = new CommonConfigStoreImpl<EndpointConfig>();
   
   // The server integration classloader' ServerConfig instance reference
   private static volatile ServerConfig serverConfig;
   
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
      setWebServiceHost(host, null);
   }
   
   protected void setWebServiceHost(String host, UpdateCallbackHandler uch) throws UnknownHostException
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
      final String wsh = toIPv6URLFormat("127.0.0.1".equals(host) ? "localhost" : host); // TCK workaround
      synchronized (webServiceHostLock)
      {
         if (uch != null) {
            uch.onBeforeUpdate();
         }
         this.webServiceHost = wsh;
      }
   }

   private String toIPv6URLFormat(final String host)
   {
      String address = host;
      boolean isIPv6URLFormatted = false;
      //strip out IPv6 URL formatting if already provided...
      if (host.startsWith("[") && host.endsWith("]")) {
         isIPv6URLFormatted = true;
         address = host.substring(1, host.length() - 1);
      }
      //verify...
      if (!AddressUtils.isValidAddress(address)) {
         throw MESSAGES.invalidAddressProvided(address);
      }
      //return IPv6 URL formatted address
      if (isIPv6URLFormatted) {
         return host;
      } else {
         return AddressUtils.isValidIPv6Address(host) ? "[" + host + "]" : host;
      }
   }

   public void setWebServicePort(int port)
   {
      setWebServicePort(port, null);
   }
   
   protected void setWebServicePort(int port, UpdateCallbackHandler uch)
   {
      synchronized (webServicePortLock)
      {
         if (uch != null) {
            uch.onBeforeUpdate();
         }
         this.webServicePort = port;
      }
   }

   public void setWebServiceSecurePort(int port)
   {
      setWebServiceSecurePort(port, null);
   }
   
   protected void setWebServiceSecurePort(int port, UpdateCallbackHandler uch)
   {
      synchronized (webServiceSecurePortLock)
      {
         if (uch != null) {
            uch.onBeforeUpdate();
         }
         this.webServiceSecurePort = port;
      }
   }

   public boolean isModifySOAPAddress()
   {
      return modifySOAPAddress;
   }

   public void setModifySOAPAddress(boolean modify)
   {
      this.modifySOAPAddress = modify;
   }
   
   protected void setModifySOAPAddress(boolean modify, UpdateCallbackHandler uch)
   {
      synchronized (modifySOAPAddressLock)
      {
         if (uch != null) {
            uch.onBeforeUpdate();
         }
         this.modifySOAPAddress = modify;
      }
   }

   public int getWebServicePort()
   {
      synchronized (webServicePortLock)
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
   }

   public int getWebServiceSecurePort()
   {
      synchronized (webServiceSecurePortLock)
      {
         if (webServiceSecurePort <= 0)
         {
            final int connectorPort = getConnectorPort(true);
            //check if the returned port is valid (Undertow service returns plain HTTP port if no HTTPS connector is installed)
            if (connectorPort > 0 && connectorPort != getConnectorPort(false))
            {
               webServiceSecurePort = connectorPort;
            }
         }
         int localPort = webServiceSecurePort;
         if (localPort <= 0)
         {
            if (MANAGEMENT_LOGGER.isDebugEnabled()) MANAGEMENT_LOGGER.unableToCalculateWebServicesSecurePort("8443");
            localPort = 8443;
         }
   
         return localPort;
      }
   }
   
   private int getConnectorPort(boolean secure) {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      int port = 0;
      try
      {
         WebServerInfo webServerInfo = SPIProvider.getInstance().getSPI(WebServerInfoFactory.class, cl).newWebServerInfo();
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
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      this.stackConfig = SPIProvider.getInstance().getSPI(StackConfigFactory.class, cl).getStackConfig();

      MANAGEMENT_LOGGER.startingWSServerConfig(getImplementationTitle(), getImplementationVersion());
      MBeanServer mbeanServer = getMbeanServer();
      if (mbeanServer != null) {
         mbeanServer.registerMBean(this, AbstractServerConfigMBean.OBJECT_NAME);
      }
      
      clientConfigStore.reload();
      endpointConfigStore.reload();
      
      if (ClassLoaderProvider.isSet()) {
         serverConfig = this;
      }
   }

   public void destroy() throws Exception
   {
      MBeanServer mbeanServer = getMbeanServer();
      if (mbeanServer != null) {
         mbeanServer.unregisterMBean(AbstractServerConfigMBean.OBJECT_NAME);
      }
      
      clientConfigStore.unload();
      endpointConfigStore.unload();
   }
   
   public static ServerConfig getServerIntegrationServerConfig()
   {
      if (!ClassLoaderProvider.isSet()) {
         return null;
      }
      checkPermission(LOOKUP_SERVER_INTEGRATION_SERVER_CONFIG);
      return serverConfig;
   }
   
   public static final PrivilegedAction<ServerConfig> GET_SERVER_INTEGRATION_SERVER_CONFIG = new PrivilegedAction<ServerConfig>()
   {
      @Override
      public ServerConfig run()
      {
         return getServerIntegrationServerConfig();
      }
   };

   public String getImplementationTitle()
   {
      return stackConfig.getImplementationTitle();
   }

   public String getImplementationVersion()
   {
      return stackConfig.getImplementationVersion();
   }
   
   public void registerClientConfig(ClientConfig config)
   {
      clientConfigStore.register(config);
   }
   
   public void unregisterClientConfig(ClientConfig config)
   {
      clientConfigStore.unregister(config);
   }
   
   public void reloadClientConfigs()
   {
      clientConfigStore.reload();
   }
   
   public ClientConfig getClientConfig(String name)
   {
      return clientConfigStore.getConfig(name);
   }
   
   public void registerEndpointConfig(EndpointConfig config)
   {
      endpointConfigStore.register(config);
   }
   
   public void unregisterEndpointConfig(EndpointConfig config)
   {
      endpointConfigStore.unregister(config);
   }
   
   public void reloadEndpointConfigs()
   {
      endpointConfigStore.reload();
   }
   
   public EndpointConfig getEndpointConfig(String name)
   {
      return endpointConfigStore.getConfig(name);
   }

   private static void checkPermission(final Permission permission)
   {
      SecurityManager securityManager = System.getSecurityManager();
      if (securityManager != null)
      {
         AccessController.checkPermission(permission);
      }
   }

   public interface UpdateCallbackHandler {
      public void onBeforeUpdate();
   }
}