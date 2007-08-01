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
package org.jboss.wsf.spi.metadata.j2ee.serviceref;

// $Id$

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.serviceref.ServiceRefMetaData;
import org.w3c.dom.Element;

/**
 * The metdata data from service-ref element in web.xml, ejb-jar.xml, and
 * application-client.xml.
 * 
 * @author Thomas.Diesler@jboss.org
 */
public class UnifiedServiceRefMetaData extends ServiceRefMetaData
{
   // provide logging
   private static Logger log = Logger.getLogger(UnifiedServiceRefMetaData.class);

   private UnifiedVirtualFile vfsRoot;

   // Standard properties 

   // The required <service-ref-name> element
   private String serviceRefName;
   // The JAXRPC required <service-interface> element
   private String serviceInterface;
   // service-res-type
   private String serviceRefType;
   // The optional <wsdl-file> element
   private String wsdlFile;
   // The optional <jaxrpc-mapping-file> element
   private String mappingFile;
   // The optional <service-qname> element
   private QName serviceQName;
   // The list <port-component-ref> elements
   private List<UnifiedPortComponentRefMetaData> portComponentRefs = new ArrayList<UnifiedPortComponentRefMetaData>();
   // The optional <handler> elements. JAX-RPC handlers declared in the standard J2EE1.4 descriptor
   private List<UnifiedHandlerMetaData> handlers = new ArrayList<UnifiedHandlerMetaData>();
   // The optional <handler-chains> elements. JAX-WS handlers declared in the standard JavaEE5 descriptor
   private UnifiedHandlerChainsMetaData handlerChains;

   // JBoss properties 

   // The optional <service-impl-class> element
   private String serviceImplClass;
   // The optional JBossWS config-name
   private String configName;
   // The optional JBossWS config-file
   private String configFile;
   // The optional URL of the actual WSDL to use, <wsdl-override> 
   private String wsdlOverride;
   // The optional <handler-chain> element. JAX-WS handler chain declared in the JBoss JavaEE5 descriptor
   private String handlerChain;
   // Arbitrary proxy properties given by <call-property> 
   private List<UnifiedCallPropertyMetaData> callProperties = new ArrayList<UnifiedCallPropertyMetaData>();

   // The JAXWS annotated element. JDK1.4 does not have java.lang.reflect.AnnotatedElement so we use an untyped Object
   private transient Object anElement;
   // A flag that should be set when this service-ref has been bound.
   private transient boolean processed;

   public UnifiedServiceRefMetaData(UnifiedVirtualFile vfRoot)
   {
      this.vfsRoot = vfRoot;
   }

   public UnifiedServiceRefMetaData()
   {
   }

   public void merge(ServiceRefMetaData sref)
   {
      UnifiedServiceRefMetaData sourceRef = (UnifiedServiceRefMetaData)sref;
      serviceImplClass = sourceRef.serviceImplClass;
      configName = sourceRef.configName;
      configFile = sourceRef.configFile;
      wsdlOverride = sourceRef.wsdlOverride;
      handlerChain = sourceRef.handlerChain;
      callProperties = sourceRef.callProperties;

      if (serviceQName == null && sourceRef.serviceQName != null)
         serviceQName = sourceRef.serviceQName;

      for (UnifiedPortComponentRefMetaData pcref : sourceRef.getPortComponentRefs())
      {
         String seiName = pcref.getServiceEndpointInterface();
         QName portQName = pcref.getPortQName();
         UnifiedPortComponentRefMetaData targetPCRef = getPortComponentRef(seiName, portQName);

         if (targetPCRef == null)
         {
            log.warn("Cannot find port component ref: [sei=" + seiName + ",port=" + portQName + "]");
            if (seiName != null)
               addPortComponentRef(pcref);
            else
               log.warn("Ingore port component ref without SEI declaration: " + pcref);

            targetPCRef = pcref;
         }

         targetPCRef.merge(pcref);
      }
   }

   public UnifiedVirtualFile getVfsRoot()
   {
      return vfsRoot;
   }

   public void setVfsRoot(UnifiedVirtualFile vfsRoot)
   {
      this.vfsRoot = vfsRoot;
   }

   public String getServiceRefName()
   {
      return serviceRefName;
   }

   public void setServiceRefName(String serviceRefName)
   {
      this.serviceRefName = serviceRefName;
   }

   public String getMappingFile()
   {
      return mappingFile;
   }

   public void setMappingFile(String mappingFile)
   {
      this.mappingFile = mappingFile;
   }

   public URL getMappingLocation()
   {
      URL mappingURL = null;
      if (mappingFile != null)
      {
         try
         {
            mappingURL = vfsRoot.findChild(mappingFile).toURL();
         }
         catch (Exception e)
         {
            throw new WebServiceException("Cannot find jaxrcp-mapping-file: " + mappingFile, e);
         }
      }
      return mappingURL;
   }

   public Collection<UnifiedPortComponentRefMetaData> getPortComponentRefs()
   {
      return portComponentRefs;
   }

   public UnifiedPortComponentRefMetaData getPortComponentRef(String seiName, QName portName)
   {
      UnifiedPortComponentRefMetaData matchingRef = null;
      for (UnifiedPortComponentRefMetaData ref : portComponentRefs)
      {
         if (ref.matches(seiName, portName))
         {
            if (matchingRef != null)
               log.warn("Multiple matching port component ref: [sei=" + seiName + ",port=" + portName + "]");

            matchingRef = ref;
         }
      }
      return matchingRef;
   }

   public void addPortComponentRef(UnifiedPortComponentRefMetaData pcRef)
   {
      portComponentRefs.add(pcRef);
   }

   public List<UnifiedHandlerMetaData> getHandlers()
   {
      return handlers;
   }

   public void addHandler(UnifiedHandlerMetaData handler)
   {
      handlers.add(handler);
   }

   public String getServiceInterface()
   {
      return serviceInterface;
   }

   public void setServiceInterface(String serviceInterface)
   {
      this.serviceInterface = serviceInterface;
   }

   public String getServiceImplClass()
   {
      return serviceImplClass;
   }

   public void setServiceImplClass(String serviceImplClass)
   {
      this.serviceImplClass = serviceImplClass;
   }

   public QName getServiceQName()
   {
      return serviceQName;
   }

   public void setServiceQName(QName serviceQName)
   {
      this.serviceQName = serviceQName;
   }

   public String getServiceRefType()
   {
      return serviceRefType;
   }

   public void setServiceRefType(String serviceResType)
   {
      this.serviceRefType = serviceResType;
   }

   public String getWsdlFile()
   {
      return wsdlFile;
   }

   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }

   public URL getWsdlLocation()
   {
      URL wsdlLocation = null;
      if (wsdlOverride != null)
      {
         try
         {
            wsdlLocation = new URL(wsdlOverride);
         }
         catch (MalformedURLException e1)
         {
            try
            {
               wsdlLocation = vfsRoot.findChild(wsdlOverride).toURL();
            }
            catch (Exception e)
            {
               throw new WebServiceException("Cannot find wsdl-override: " + wsdlOverride, e);
            }
         }
      }

      if (wsdlLocation == null && wsdlFile != null)
      {
         try
         {
            wsdlLocation = vfsRoot.findChild(wsdlFile).toURL();
         }
         catch (Exception e)
         {
            throw new WebServiceException("Cannot find wsdl-file: " + wsdlFile, e);
         }
      }

      return wsdlLocation;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String getWsdlOverride()
   {
      return wsdlOverride;
   }

   public void setWsdlOverride(String wsdlOverride)
   {
      this.wsdlOverride = wsdlOverride;
   }

   public List<UnifiedCallPropertyMetaData> getCallProperties()
   {
      return callProperties;
   }

   public void setCallProperties(List<UnifiedCallPropertyMetaData> callProps)
   {
      callProperties = callProps;
   }

   public void addCallProperty(UnifiedCallPropertyMetaData callProp)
   {
      callProperties.add(callProp);
   }

   public UnifiedHandlerChainsMetaData getHandlerChains()
   {
      return handlerChains;
   }

   public void setHandlerChains(UnifiedHandlerChainsMetaData handlerChains)
   {
      this.handlerChains = handlerChains;
   }

   public String getHandlerChain()
   {
      return handlerChain;
   }

   public void setHandlerChain(String handlerChain)
   {
      this.handlerChain = handlerChain;
   }

   public Object getAnnotatedElement()
   {
      return anElement;

   }

   public boolean isProcessed()
   {
      return processed;
   }

   public void setProcessed(boolean flag)
   {
      this.processed = flag;
   }

   public void setAnnotatedElement(Object anElement)
   {
      this.anElement = anElement;
   }

   @Override
   public void importStandardXml(Element root)
   {
      new ServiceRefMetaDataParser().importStandardXml(root, this);
   }

   @Override
   public void importJBossXml(Element root)
   {
      new ServiceRefMetaDataParser().importJBossXml(root, this);
   }

   public String toString()
   {
      StringBuilder str = new StringBuilder();
      str.append("\nUnifiedServiceRef");
      str.append("\n serviceRefName=" + serviceRefName);
      str.append("\n serviceInterface=" + serviceInterface);
      str.append("\n serviceImplClass=" + serviceImplClass);
      str.append("\n serviceRefType=" + serviceRefType);
      str.append("\n serviceQName=" + serviceQName);
      str.append("\n anElement=" + anElement);
      str.append("\n wsdlFile=" + wsdlFile);
      str.append("\n wsdlOverride=" + wsdlOverride);
      str.append("\n mappingFile=" + mappingFile);
      str.append("\n configName=" + configName);
      str.append("\n configFile=" + configFile);
      str.append("\n callProperties=" + callProperties);
      str.append("\n processed=" + processed);
      str.append("\n handlerChains=" + handlerChains);
      str.append("\n handlerChain=" + handlerChain);
      for (UnifiedHandlerMetaData uhmd : handlers)
         str.append(uhmd.toString());
      for (UnifiedPortComponentRefMetaData pcref : portComponentRefs)
         str.append(pcref.toString());
      return str.toString();
   }
}
