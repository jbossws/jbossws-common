/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

   // The SOAP address path component for substitution in the existing SOAP address.
   private volatile String webServicePathRewriteRule;
   private final Object webServicePathRewriteRuleLock = new Object();
   
   //The SOAP address uri schema, http is the default value
   private volatile String webServiceUriScheme;
   private final Object webServiceUriSchemeLock = new Object();

   private volatile boolean statisticsEnabled;
   
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
         host=localHost.getHostAddress();
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
   
   
   public boolean isStatisticsEnabled()
   {
      return statisticsEnabled;
   }

   public void setStatisticsEnabled(boolean enabled)
   {
      this.statisticsEnabled = enabled;
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

   public String getWebServicePathRewriteRule()
   {
      return webServicePathRewriteRule;
   }

   public void setWebServicePathRewriteRule(String path)
   {
      setWebServicePathRewriteRule(path, null);
   }

   public void setWebServicePathRewriteRule(String path, UpdateCallbackHandler uch)
   {
      if (path != null) {
         setStackConfig();
         stackConfig.validatePathRewriteRule(path);
      }
      synchronized (webServicePathRewriteRuleLock)
      {
         if (uch != null)
         {
            uch.onBeforeUpdate();
         }
         this.webServicePathRewriteRule = path;
      }
   }
   
   public String getWebServiceUriScheme()
   {
      return this.webServiceUriScheme;
   }

   public void setWebServiceUriScheme(String scheme)
   {
      setWebServiceUriScheme(scheme, null);
   }

   public void setWebServiceUriScheme(String scheme, UpdateCallbackHandler uch)
   {
      synchronized (webServiceUriSchemeLock)
      {
         if (uch != null)
         {
            uch.onBeforeUpdate();
         }
         this.webServiceUriScheme = scheme;
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
      setStackConfig();
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
   
   private void setStackConfig() {
      if (stackConfig == null) {
         synchronized (this)
         {
            if (stackConfig == null) {
               //Retrieve the stackConfig using SPIProvider
               final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
               this.stackConfig = SPIProvider.getInstance().getSPI(StackConfigFactory.class, cl).getStackConfig();
            }
         }
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

   public Integer getVirtualHostPort(String hostname, boolean secure)
   {
      return null;
   }

   public String getHostAlias(String virtualHost)
   {
      return "localhost";
   }
   private static void checkPermission(final Permission permission)
   {
      SecurityManager securityManager = System.getSecurityManager();
      if (securityManager != null)
      {
    	  securityManager.checkPermission(permission);
      }
   }

   public interface UpdateCallbackHandler {
      public void onBeforeUpdate();
   }
}
