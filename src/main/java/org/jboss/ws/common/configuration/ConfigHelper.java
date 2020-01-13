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
package org.jboss.ws.common.configuration;

import static org.jboss.ws.common.Loggers.ROOT_LOGGER;
import static org.jboss.ws.common.Messages.MESSAGES;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.security.ClientConfigProvider;

/**
 * Facility class for setting Client config
 * 
 * @author alessio.soldano@jboss.com
 * @since 29-May-2012
 *
 */
public class ConfigHelper implements ClientConfigurer
{
   private static final Map<String, String> bindingIDs = new HashMap<String, String>(8);
   static {
      bindingIDs.put(SOAPBinding.SOAP11HTTP_BINDING, "##SOAP11_HTTP");
      bindingIDs.put(SOAPBinding.SOAP12HTTP_BINDING, "##SOAP12_HTTP");
      bindingIDs.put(SOAPBinding.SOAP11HTTP_MTOM_BINDING, "##SOAP11_HTTP_MTOM");
      bindingIDs.put(SOAPBinding.SOAP12HTTP_MTOM_BINDING, "##SOAP12_HTTP_MTOM");
      bindingIDs.put(HTTPBinding.HTTP_BINDING, "##XML_HTTP");
   }
   
   @Override
   public void setConfigHandlers(BindingProvider port, String configFile, String configName)
   {
      Class<?> clazz = !(port instanceof Dispatch) ? port.getClass() : null;
      ClientConfig config = readConfig(configFile, configName, clazz);
      setupConfigHandlers(port.getBinding(), config);
   }

   @Override
   public void setConfigProperties(Object client, String configFile, String configName)
   {
      throw MESSAGES.operationNotSupportedBy("setConfigProperties", this.getClass());
   }

   protected ClientConfig readConfig(String configFile, String configName, Class<?> clientProxyClass) {
      return readConfig(configFile, configName, clientProxyClass, null);
   }

   protected ClientConfig readConfig(String configFile, String configName, Class<?> clientProxyClass, BindingProvider bindingProvider) {
      ClientConfig clientConfig = null;
      if (configFile != null) {
         InputStream is = null;
         try {
            is = SecurityActions.getContextClassLoader().getResourceAsStream(configFile);
            if (is != null) {
               ConfigRoot config = ConfigMetaDataParser.parse(is);
               if (config != null) {
                  if (configName == null) {
                     for (Class<?> itf : clientProxyClass.getInterfaces()) {
                        ClientConfig cc = config.getClientConfigByName(itf.getName());
                        if (cc != null) {
                           clientConfig = cc;
                           break;
                        }
                     }
                  } else {
                     ClientConfig cc = config.getClientConfigByName(configName);
                     if (cc != null) {
                        clientConfig = cc;
                     }
                  }
               }
            }
         } catch (Exception e) {
            throw MESSAGES.couldNotReadConfiguration(configFile, e);
         } finally {
            if (is != null) {
               try {
                  is.close();
               } catch (IOException e) {
               } //ignore
            }
         }
      } else if (configName != null) {
         InputStream is = null;
         try {
            is = SecurityActions.getContextClassLoader().getResourceAsStream(ClientConfig.DEFAULT_CLIENT_CONFIG_FILE);
            if (is != null) {
               ConfigRoot config = ConfigMetaDataParser.parse(is);
               ClientConfig cc = config != null ? config.getClientConfigByName(configName) : null;
               if (cc != null) {
                  clientConfig = cc;
               }
            }
         } catch (Exception e) {
            throw MESSAGES.couldNotReadConfiguration(configFile, e);
         } finally {
            if (is != null) {
               try {
                  is.close();
               } catch (IOException e) {
               } //ignore
            }
         }
         if (clientConfig == null) {
            try {
               ServerConfig sc = getServerConfig();
               if (sc != null) {
                  ClientConfig cf = sc.getClientConfig(configName);
                  if (cf != null) {
                     return cf;
                  }
               }
            } catch (Exception e) {
               throw MESSAGES.configurationNotFound(configName);
            }
         }
      }
      if (bindingProvider != null && (configName == null || clientConfig != null)) {
         //use client configuration provider
         ClientConfigProvider configProvider = (ClientConfigProvider) ServiceLoader.loadService(
                 ClientConfigProvider.class.getName(), null, ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
         if (configProvider != null) {
            clientConfig = configProvider.configure(clientConfig, bindingProvider);
         }
      }
      if (clientConfig != null) {
         return clientConfig;
      }
      throw MESSAGES.configurationNotFound(configName);
   }
   
   /**
    * Setups a given Binding instance using a specified CommonConfig
    * 
    * @param binding    the Binding instance to setup
    * @param config     the CommonConfig with the input configuration
    */
   @SuppressWarnings("rawtypes")
   public void setupConfigHandlers(Binding binding, CommonConfig config)
   {
      if (config != null) {
         //start with the use handlers only to remove the previously set configuration
         List<Handler> userHandlers = getNonConfigHandlers(binding.getHandlerChain());
         List<Handler> handlers = convertToHandlers(config.getPreHandlerChains(), binding.getBindingID(), true); //PRE
         handlers.addAll(userHandlers); //ENDPOINT
         handlers.addAll(convertToHandlers(config.getPostHandlerChains(), binding.getBindingID(), false)); //POST
         binding.setHandlerChain(handlers);
      }
   }
   
   @SuppressWarnings("rawtypes")
   private static List<Handler> getNonConfigHandlers(List<Handler> handlerChain) {
      List<Handler> list = new LinkedList<Handler>();
      for (Handler h : handlerChain) {
         if (!(h instanceof ConfigDelegateHandler)) {
            list.add(h);
         }
      }
      return list;
   }
   
   @SuppressWarnings({"rawtypes", "unchecked"})
   private static List<Handler> convertToHandlers(List<UnifiedHandlerChainMetaData> handlerChains, String bindingID, boolean isPre)
   {
      List<Handler> handlers = new LinkedList<Handler>();
      if (handlerChains != null && !handlerChains.isEmpty())
      {
         final String protocolBinding = bindingIDs.get(bindingID);
         for (UnifiedHandlerChainMetaData handlerChain : handlerChains)
         {
            if (handlerChain.getPortNamePattern() != null || handlerChain.getServiceNamePattern() != null)
            {
               ROOT_LOGGER.filtersNotSupported();
            }
            if (matchProtocolBinding(protocolBinding, handlerChain.getProtocolBindings())) {
               for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
               {
                  if (uhmd.getInitParams() != null && !uhmd.getInitParams().isEmpty())
                  {
                     ROOT_LOGGER.initParamsNotSupported();
                  }
                  Object h = newInstance(uhmd.getHandlerClass());
                  if (h != null)
                  {
                     if (h instanceof Handler)
                     {
                        if (h instanceof LogicalHandler)
                        {
                           handlers.add(new LogicalConfigDelegateHandler((LogicalHandler)h, isPre));
                        }
                        else if (h instanceof SOAPHandler)
                        {
                           handlers.add(new SOAPConfigDelegateHandler((SOAPHandler)h, isPre));
                        }
                        else
                        {
                           handlers.add(new ConfigDelegateHandler((Handler)h, isPre));
                        }
                     }
                     else
                     {
                        throw MESSAGES.notJAXWSHandler(uhmd.getHandlerClass());
                     }
                  }
               }
            }
         }
      }
      return handlers;
   }
   
   private static boolean matchProtocolBinding(String currentProtocolBinding, String handlerChainProtocolBindings) {
      if (handlerChainProtocolBindings == null)
         return true;
      List<String> protocolBindings = new LinkedList<String>();
      if (handlerChainProtocolBindings != null) {
         StringTokenizer st = new StringTokenizer(handlerChainProtocolBindings, " ", false);
         while (st.hasMoreTokens()) {
            protocolBindings.add(st.nextToken());
         }
      }
      return protocolBindings.contains(currentProtocolBinding);
   }
   
   private static Object newInstance(String className)
   {
      ClassLoader orig = null;
      try
      {
         Class<?> clazz = null;
         //first try using the defining classloader (for out-of-container client scenarios)
         try {
            clazz = Class.forName(className);
         } catch (Throwable e) {
            //ignore
         }
         //otherwise create a DelegateClassloader with the ASIL classloader + TCCL
         //and wipe out the TCCL just before loading the handler class to prevent
         //code in static blocks of the user handler from having access to our
         //integration classes
         if (clazz == null) {
            ClassLoader loader = null;
            orig = SecurityActions.getContextClassLoader();
            if (orig != null)
            {
               final ClassLoader serverIntegrationClassLoader = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
               final SecurityManager sm = System.getSecurityManager();
               if (sm != null) {
                  final ClassLoader origCL = orig;
                  loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                     public ClassLoader run() {
                        return new DelegateClassLoader(serverIntegrationClassLoader, origCL);
                     }
                  });
               } else {
                  loader = new DelegateClassLoader(serverIntegrationClassLoader, orig);
               }
               SecurityActions.setContextClassLoader(null);
            } else {
               loader = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
            }
            clazz = SecurityActions.loadClass(loader, className);
         }
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         ROOT_LOGGER.cannotAddHandler(className, e);
         return null;
      }
      finally
      {
         if (orig != null)
         {
            SecurityActions.setContextClassLoader(orig);
         }
      }
   }
   
   private static ServerConfig getServerConfig()
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      final ServerConfigFactory scf = SPIProvider.getInstance().getSPI(ServerConfigFactory.class, cl);
      return scf != null ? scf.getServerConfig() : null;
   }
}
