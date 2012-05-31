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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.api.configuration.ClientConfigurer;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;

/**
 * Facility class for setting Client config
 * 
 * @author alessio.soldano@jboss.com
 * @since 29-May-2012
 *
 */
public class ConfigHelper implements ClientConfigurer
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ConfigHelper.class);
   
   private static Map<String, String> bindingIDs = new HashMap<String, String>();
   static {
      bindingIDs.put(SOAPBinding.SOAP11HTTP_BINDING, "##SOAP11_HTTP");
      bindingIDs.put(SOAPBinding.SOAP12HTTP_BINDING, "##SOAP12_HTTP");
      bindingIDs.put(SOAPBinding.SOAP11HTTP_MTOM_BINDING, "##SOAP11_HTTP_MTOM");
      bindingIDs.put(SOAPBinding.SOAP12HTTP_MTOM_BINDING, "##SOAP12_HTTP_MTOM");
      bindingIDs.put(HTTPBinding.HTTP_BINDING, "##XML_HTTP");
   }
   
   @Override
   public void addConfigHandlers(BindingProvider port, String configFile, String configName)
   {
      ClientConfig config = readConfig(configFile, configName);
      setupConfigHandlers(port.getBinding(), config);
   }
   
   private static ClientConfig readConfig(String configFile, String configName) {
      InputStream is = null;
      try
      {
         is = SecurityActions.getContextClassLoader().getResourceAsStream(configFile);
         ConfigRoot config = ConfigMetaDataParser.parse(is);
         return config.getClientConfigByName(configName);
      }
      catch (Exception e)
      {
         throw new RuntimeException(BundleUtils.getMessage(bundle, "COULD_NOT_READ_CONFIG",  configFile));
      }
      finally
      {
         if (is != null) {
            try {
               is.close();
            } catch (IOException e) { } //ignore
         }
      }
   }
   
   @SuppressWarnings("rawtypes")
   private static void setupConfigHandlers(Binding binding, CommonConfig config)
   {
      if (config != null) {
         List<Handler> handlers = convertToHandlers(config.getPreHandlerChains(), binding); //PRE
         handlers.addAll(binding.getHandlerChain()); //ENDPOINT
         handlers.addAll(convertToHandlers(config.getPostHandlerChains(), binding)); //POST
         binding.setHandlerChain(handlers);
      }
   }
   
   @SuppressWarnings("rawtypes")
   private static List<Handler> convertToHandlers(List<UnifiedHandlerChainMetaData> handlerChains, Binding binding)
   {
      List<Handler> handlers = new LinkedList<Handler>();
      if (handlerChains != null && !handlerChains.isEmpty())
      {
         final String protocolBinding = bindingIDs.get(binding.getBindingID());
         for (UnifiedHandlerChainMetaData handlerChain : handlerChains)
         {
            if (handlerChain.getPortNamePattern() != null || handlerChain.getServiceNamePattern() != null)
            {
               Logger.getLogger(ConfigHelper.class).warn(BundleUtils.getMessage(bundle, "FILTERS_NOT_SUPPORTED"));
            }
            if (matchProtocolBinding(protocolBinding, handlerChain.getProtocolBindings())) {
               for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
               {
                  if (uhmd.getInitParams() != null && !uhmd.getInitParams().isEmpty())
                  {
                     Logger.getLogger(ConfigHelper.class).warn(BundleUtils.getMessage(bundle, "INIT_PARAMS_NOT_SUPPORTED"));
                  }
                  Object h = newInstance(uhmd.getHandlerClass());
                  if (h != null)
                  {
                     if (h instanceof Handler)
                     {
                        handlers.add((Handler)h);
                     }
                     else
                     {
                        throw new RuntimeException(BundleUtils.getMessage(bundle, "NOT_HANDLER_INSTANCE", h));
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
      try
      {
         ClassLoader loader = new DelegateClassLoader(ClassLoaderProvider.getDefaultProvider()
               .getServerIntegrationClassLoader(), SecurityActions.getContextClassLoader());
         Class<?> clazz = SecurityActions.loadClass(loader, className);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         Logger.getLogger(ConfigHelper.class).warnf(e, BundleUtils.getMessage(bundle, "CAN_NOT_ADD_HANDLER" , className));
         return null;
      }
   }
}
