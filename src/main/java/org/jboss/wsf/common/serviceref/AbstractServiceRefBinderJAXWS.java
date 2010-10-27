/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.common.serviceref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jws.HandlerChain;
import javax.naming.Referenceable;
import javax.xml.namespace.QName;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.MTOM;

import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * Binds a JAXWS service object factory to the client's ENC.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractServiceRefBinderJAXWS extends AbstractServiceRefBinder
{
   public final Referenceable createReferenceable(final UnifiedServiceRefMetaData serviceRef, final ClassLoader loader)
   {
      this.processAddressingAnnotation(serviceRef);
      this.processMTOMAnnotation(serviceRef);
      this.processRespectBindingAnnotation(serviceRef);
      this.processHandlerChainAnnotation(serviceRef);

      final Class<?> targetClass = getTargetClass(serviceRef);
      final String targetClassName = (targetClass != null ? targetClass.getName() : null);
      final String serviceImplClassName = getServiceImplClassName(serviceRef);

      // TODO: investigate why these if conditions
      if (targetClassName != null)
      {
         serviceRef.setServiceInterface(targetClassName);
      }
      if (serviceRef.getServiceImplClass() == null)
      {
         serviceRef.setServiceImplClass(serviceImplClassName);
      }

      this.processWsdlOverride(serviceRef, loader);

      return this.createJAXWSReferenceable(serviceRef);
   }

   /**
    * Template method for creating stack specific JAXWS referenceables.
    *
    * @param serviceImplClass service implementation class name
    * @param targetClassName target class name
    * @param serviceRef service reference UMDM
    * @return stack specific JAXWS JNDI referenceable
    */
   protected abstract Referenceable createJAXWSReferenceable(final UnifiedServiceRefMetaData serviceRefMD);

   private void processAddressingAnnotation(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final Addressing addressingAnnotation = this.getAnnotation(serviceRefMD, Addressing.class);

      if (addressingAnnotation != null)
      {
         serviceRefMD.setAddressingEnabled(addressingAnnotation.enabled());
         serviceRefMD.setAddressingRequired(addressingAnnotation.required());
         serviceRefMD.setAddressingResponses(addressingAnnotation.responses().toString());
      }
   }

   private void processMTOMAnnotation(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final MTOM mtomAnnotation = this.getAnnotation(serviceRefMD, MTOM.class);

      if (mtomAnnotation != null)
      {
         serviceRefMD.setMtomEnabled(mtomAnnotation.enabled());
         serviceRefMD.setMtomThreshold(mtomAnnotation.threshold());
      }
   }

   private void processRespectBindingAnnotation(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final RespectBinding respectBindingAnnotation = this.getAnnotation(serviceRefMD, RespectBinding.class);

      if (respectBindingAnnotation != null)
      {
         serviceRefMD.setRespectBindingEnabled(respectBindingAnnotation.enabled());
      }
   }

   private void processHandlerChainAnnotation(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final HandlerChain handlerChainAnnotation = this.getAnnotation(serviceRefMD, HandlerChain.class);

      if (handlerChainAnnotation != null)
      {
         // Set the handlerChain from @HandlerChain on the annotated element
         String handlerChain = serviceRefMD.getHandlerChain();
         if (handlerChain == null && handlerChainAnnotation.file().length() > 0)
            handlerChain = handlerChainAnnotation.file();

         // Resolve path to handler chain
         if (handlerChain != null)
         {
            try
            {
               new URL(handlerChain);
            }
            catch (MalformedURLException ignored)
            {
               final AnnotatedElement annotatedElement = (AnnotatedElement) serviceRefMD.getAnnotatedElement();
               final Class<?> declaringClass = getDeclaringClass(annotatedElement);

               handlerChain = declaringClass.getPackage().getName().replace('.', '/') + "/" + handlerChain;
            }

            serviceRefMD.setHandlerChain(handlerChain);
         }
      }
   }

   private <T extends Annotation> T getAnnotation(final UnifiedServiceRefMetaData serviceRefMD, Class<T> annotationClass)
   {
      final AnnotatedElement annotatedElement = (AnnotatedElement) serviceRefMD.getAnnotatedElement();

      return annotatedElement != null ? (T) annotatedElement.getAnnotation(annotationClass) : null;
   }

   private void processWsdlOverride(final UnifiedServiceRefMetaData serviceRefMD, final ClassLoader loader)
   {
      // Set the wsdlLocation if there is no override already
      final WebServiceRef serviceRefAnnotation = this.getWebServiceRefAnnotation(serviceRefMD);
      if (serviceRefMD.getWsdlOverride() == null && serviceRefAnnotation != null
            && serviceRefAnnotation.wsdlLocation().length() > 0)
         serviceRefMD.setWsdlOverride(serviceRefAnnotation.wsdlLocation());

      // Extract service QName for target service
      if (null == serviceRefMD.getServiceQName())
      {
         try
         {
            Class<?> serviceClass = loader.loadClass(serviceRefMD.getServiceImplClass());
            if (serviceClass.getAnnotation(WebServiceClient.class) != null)
            {
               WebServiceClient clientDecl = (WebServiceClient) serviceClass.getAnnotation(WebServiceClient.class);
               serviceRefMD.setServiceQName(new QName(clientDecl.targetNamespace(), clientDecl.name()));
               //use the @WebServiceClien(wsdlLocation=...) if the service ref wsdl location returned at this time would be null
               if (clientDecl.wsdlLocation().length() > 0 && serviceRefMD.getWsdlLocation() == null)
               {
                  serviceRefMD.setWsdlOverride(clientDecl.wsdlLocation());
               }
            }
         }
         catch (ClassNotFoundException e)
         {
            WSFException.rethrow("Cannot extract service QName for target service", e);
         }
      }
   }

   private Class<?> getDeclaringClass(final AnnotatedElement annotatedElement)
   {
      Class<?> declaringClass = null;
      if (annotatedElement instanceof Field)
         declaringClass = ((Field) annotatedElement).getDeclaringClass();
      else if (annotatedElement instanceof Method)
         declaringClass = ((Method) annotatedElement).getDeclaringClass();
      else if (annotatedElement instanceof Class)
         declaringClass = (Class<?>) annotatedElement;

      return declaringClass;
   }

   private Class<?> getTargetClass(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final AnnotatedElement annotatedElement = (AnnotatedElement) serviceRefMD.getAnnotatedElement();

      Class<?> targetClass = null;

      if (annotatedElement instanceof Field)
      {
         targetClass = ((Field) annotatedElement).getType();
      }
      else if (annotatedElement instanceof Method)
      {
         targetClass = ((Method) annotatedElement).getParameterTypes()[0];
      }
      else
      {
         final WebServiceRef serviceRefAnnotation = this.getWebServiceRefAnnotation(serviceRefMD);
         if (serviceRefAnnotation != null && (serviceRefAnnotation.type() != Object.class))
            targetClass = serviceRefAnnotation.type();
      }
      
      return targetClass;
   }

   private String getServiceImplClassName(final UnifiedServiceRefMetaData serviceRefMD)
   {
      String serviceImplClass = null;

      // #1 Use the explicit @WebServiceRef.value
      final WebServiceRef serviceRefAnnotation = this.getWebServiceRefAnnotation(serviceRefMD);
      if (serviceRefAnnotation != null && serviceRefAnnotation.value() != Service.class)
         serviceImplClass = serviceRefAnnotation.value().getName();

      // #2 Use the target ref type
      final Class<?> targetClass = getTargetClass(serviceRefMD);
      if (serviceImplClass == null && targetClass != null && Service.class.isAssignableFrom(targetClass))
         serviceImplClass = targetClass.getName();

      // #3 Use <service-interface>
      if (serviceImplClass == null && serviceRefMD.getServiceInterface() != null)
         serviceImplClass = serviceRefMD.getServiceInterface();

      // #4 Use javax.xml.ws.Service
      if (serviceImplClass == null)
         serviceImplClass = Service.class.getName();

      return serviceImplClass;
   }

   private WebServiceRef getWebServiceRefAnnotation(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final WebServiceRef webServiceRefAnnotation = this.getAnnotation(serviceRefMD, WebServiceRef.class);
      final WebServiceRefs webServiceRefsAnnotation = this.getAnnotation(serviceRefMD, WebServiceRefs.class);

      if (webServiceRefAnnotation == null && webServiceRefsAnnotation == null)
      {
         return null;
      }

      // Build the list of @WebServiceRef relevant annotations
      final List<WebServiceRef> wsrefList = new ArrayList<WebServiceRef>();

      if (webServiceRefAnnotation != null)
      {
         wsrefList.add(webServiceRefAnnotation);
      }

      if (webServiceRefsAnnotation != null)
      {
         for (final WebServiceRef webServiceRefAnn : webServiceRefsAnnotation.value())
         {
            wsrefList.add(webServiceRefAnn);
         }
      }

      // Return effective @WebServiceRef annotation
      WebServiceRef returnValue = null;
      if (wsrefList.size() == 1)
      {
         returnValue = wsrefList.get(0);
      }
      else
      {
         for (WebServiceRef webServiceRefAnn : wsrefList)
         {
            if (serviceRefMD.getServiceRefName().endsWith(webServiceRefAnn.name()))
            {
               returnValue = webServiceRefAnn;
               break;
            }
         }
      }

      return returnValue;
   }
}
