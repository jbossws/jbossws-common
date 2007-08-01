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
package org.jboss.wsf.framework.serviceref;

// $Id$

import javax.xml.namespace.QName;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedCallPropertyMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedInitParamMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedStubPropertyMetaData;
import org.jboss.wsf.spi.serviceref.ServiceRefElement;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * A object model factory for <service-ref>
 * 
 * @author Thomas.Diesler@jboss.com
 */
public class ServiceRefObjectFactory
{
   public Object newChild(ServiceRefElement ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (ref instanceof UnifiedHandlerChainsMetaData)
         child = newChild((UnifiedHandlerChainsMetaData)ref, navigator, namespaceURI, localName, attrs);
      else if (ref instanceof UnifiedHandlerMetaData)
         child = newChild((UnifiedHandlerMetaData)ref, navigator, namespaceURI, localName, attrs);
      else if (ref instanceof UnifiedPortComponentRefMetaData)
         child = newChild((UnifiedPortComponentRefMetaData)ref, navigator, namespaceURI, localName, attrs);
      else if (ref instanceof UnifiedServiceRefMetaData)
         child = newChild((UnifiedServiceRefMetaData)ref, navigator, namespaceURI, localName, attrs);
      return child;
   }

   public void setValue(ServiceRefElement ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (ref instanceof UnifiedCallPropertyMetaData)
         setValue((UnifiedCallPropertyMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedHandlerChainMetaData)
         setValue((UnifiedHandlerChainMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedHandlerMetaData)
         setValue((UnifiedHandlerMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedInitParamMetaData)
         setValue((UnifiedInitParamMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedPortComponentRefMetaData)
         setValue((UnifiedPortComponentRefMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedServiceRefMetaData)
         setValue((UnifiedServiceRefMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedCallPropertyMetaData)
         setValue((UnifiedCallPropertyMetaData)ref, navigator, namespaceURI, localName, value);
      else if (ref instanceof UnifiedStubPropertyMetaData)
         setValue((UnifiedStubPropertyMetaData)ref, navigator, namespaceURI, localName, value);
   }

   private void setValue(UnifiedServiceRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      /* Standard properties */
      if (localName.equals("service-ref-name"))
      {
         ref.setServiceRefName(value);
      }
      else if (localName.equals("service-interface"))
      {
         ref.setServiceInterface(value);
      }
      else if (localName.equals("service-ref-type"))
      {
         ref.setServiceRefType(value);
      }
      else if (localName.equals("wsdl-file"))
      {
         ref.setWsdlFile(value);
      }
      else if (localName.equals("jaxrpc-mapping-file"))
      {
         ref.setMappingFile(value);
      }
      else if (localName.equals("service-qname"))
      {
         ref.setServiceQName(getQNameValue(navigator, value));
      }

      /* JBoss properties */
      else if (localName.equals("service-impl-class"))
      {
         ref.setServiceImplClass(value);
      }
      else if (localName.equals("config-name"))
      {
         ref.setConfigName(value);
      }
      else if (localName.equals("config-file"))
      {
         ref.setConfigFile(value);
      }
      else if (localName.equals("wsdl-override"))
      {
         ref.setWsdlOverride(value);
      }
      else if (localName.equals("handler-chain"))
      {
         ref.setHandlerChain(value);
      }
   }

   private Object newChild(UnifiedServiceRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("port-component-ref"))
      {
         child = new UnifiedPortComponentRefMetaData(ref);
         ref.addPortComponentRef((UnifiedPortComponentRefMetaData)child);
      }
      else if (localName.equals("handler"))
      {
         child = new UnifiedHandlerMetaData();
         ref.addHandler((UnifiedHandlerMetaData)child);
      }
      else if (localName.equals("handler-chains"))
      {
         child = new UnifiedHandlerChainsMetaData();
         ref.setHandlerChains((UnifiedHandlerChainsMetaData)child);
      }
      else if (localName.equals("call-property"))
      {
         child = new UnifiedCallPropertyMetaData();
         ref.addCallProperty((UnifiedCallPropertyMetaData)child);
      }
      return child;
   }

   private Object newChild(UnifiedHandlerChainsMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("handler-chain"))
      {
         child = new UnifiedHandlerChainMetaData();
         ref.addHandlerChain((UnifiedHandlerChainMetaData)child);
      }
      return child;
   }

   private void setValue(UnifiedPortComponentRefMetaData pcref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("service-endpoint-interface"))
      {
         pcref.setServiceEndpointInterface(value);
      }
      else if (localName.equals("enable-mtom"))
      {
         pcref.setEnableMTOM(Boolean.valueOf(value));
      }
      else if (localName.equals("port-component-link"))
      {
         pcref.setPortComponentLink(value);
      }
      else if (localName.equals("port-qname"))
      {
         pcref.setPortQName(getQNameValue(navigator, value));
      }
      else if (localName.equals("config-name"))
      {
         pcref.setConfigName(value);
      }
      else if (localName.equals("config-file"))
      {
         pcref.setConfigFile(value);
      }
   }

   private Object newChild(UnifiedPortComponentRefMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("call-property"))
      {
         child = new UnifiedCallPropertyMetaData();
         ref.addCallProperty((UnifiedCallPropertyMetaData)child);
      }
      if (localName.equals("stub-property"))
      {
         child = new UnifiedStubPropertyMetaData();
         ref.addStubProperty((UnifiedStubPropertyMetaData)child);
      }
      return child;
   }

   private void setValue(UnifiedHandlerChainMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("service-name-pattern"))
      {
         ref.setServiceNamePattern(getQNameValue(navigator, value));
      }
      else if (localName.equals("port-name-pattern"))
      {
         ref.setPortNamePattern(getQNameValue(navigator, value));
      }
      else if (localName.equals("protocol-binding"))
      {
         ref.setProtocolBindings(value);
      }
   }

   private void setValue(UnifiedHandlerMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("handler-name"))
      {
         ref.setHandlerName(value);
      }
      else if (localName.equals("handler-class"))
      {
         ref.setHandlerClass(value);
      }
      else if (localName.equals("soap-header"))
      {
         ref.addSoapHeader(getQNameValue(navigator, value));
      }
      else if (localName.equals("soap-role"))
      {
         ref.addSoapRole(value);
      }
      else if (localName.equals("port-name"))
      {
         ref.addPortName(value);
      }
   }

   private Object newChild(UnifiedHandlerMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      Object child = null;
      if (localName.equals("init-param"))
      {
         child = new UnifiedInitParamMetaData();
         ref.addInitParam((UnifiedInitParamMetaData)child);
      }
      return child;
   }

   private void setValue(UnifiedInitParamMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("param-name"))
      {
         ref.setParamName(value);
      }
      else if (localName.equals("param-value"))
      {
         ref.setParamValue(value);
      }
   }

   private void setValue(UnifiedCallPropertyMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("prop-name"))
      {
         ref.setPropName(value);
      }
      else if (localName.equals("prop-value"))
      {
         ref.setPropValue(value);
      }
   }

   private void setValue(UnifiedStubPropertyMetaData ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (localName.equals("prop-name"))
      {
         ref.setPropName(value);
      }
      else if (localName.equals("prop-value"))
      {
         ref.setPropValue(value);
      }
   }

   private QName getQNameValue(UnmarshallingContext navigator, String value)
   {
      QName qname = (value.startsWith("{") ? QName.valueOf(value) : navigator.resolveQName(value));
      return qname;
   }
}
