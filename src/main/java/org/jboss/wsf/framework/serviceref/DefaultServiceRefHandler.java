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

// $Id: ServiceRefHandlerImpl.java 4043 2007-07-31 17:11:42Z thomas.diesler@jboss.com $

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.wsf.framework.deployment.URLLoaderAdapter;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.serviceref.ServiceRefBinder;
import org.jboss.wsf.spi.serviceref.ServiceRefBinderFactory;
import org.jboss.wsf.spi.serviceref.ServiceRefElement;
import org.jboss.wsf.spi.serviceref.ServiceRefHandler;
import org.jboss.wsf.spi.serviceref.ServiceRefMetaData;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * Bind service refs in the client's ENC
 * for every service-ref element in the deployment descriptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.com
 *
 * @since 04-Nov-2006
 */
public class DefaultServiceRefHandler implements ServiceRefHandler
{
   // logging support
   private static Logger log = Logger.getLogger(DefaultServiceRefHandler.class);

   private ServiceRefObjectFactory objectFactory = new ServiceRefObjectFactory();

   private ServiceRefBinder getJaxrpcBinder()
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      return spiProvider.getSPI(ServiceRefBinderFactory.class).newServiceRefBinder(Type.JAXRPC);
   }

   private ServiceRefBinder getJaxwsBinder()
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      return spiProvider.getSPI(ServiceRefBinderFactory.class).newServiceRefBinder(Type.JAXWS);
   }

   public ServiceRefMetaData newServiceRefMetaData()
   {
      return new UnifiedServiceRefMetaData();
   }

   public void bindServiceRef(Context encCtx, String encName, UnifiedVirtualFile vfsRoot, ClassLoader loader, ServiceRefMetaData sref) throws NamingException
   {
      if (sref.isProcessed())
      {
         log.debug("Attempt to rebind the service-ref: " + sref.getServiceRefName());
         return;
      }

      // In case of an .war deployment the associated root file doesn't point to
      // the expanded war file structure and thus breaks service-ref usage for servlet clients.
      // This needs to be fixed in org.jboss.web.AbstractWebDeployer (JBOSS_AS/server module)
      if (vfsRoot instanceof URLLoaderAdapter)
      {
         URLLoaderAdapter ula = (URLLoaderAdapter)vfsRoot;
         URL rootURL = ula.toURL();
         if ("file".equals(rootURL.getProtocol()) && rootURL.getFile().endsWith(".war"))
         {
            String fileName = rootURL.getFile();

            if (!new File(fileName).exists()) // might be an exploded directory
            {
               // There is a filename convention for exploded directories
               fileName = fileName.substring(0, fileName.indexOf(".war")) + "-exp.war";

               File expandedDirectory = new File(fileName);
               if (!expandedDirectory.exists())
                  throw new WSFException("Failed to bind service-ref, the deployment root expandedDirectory doesn't exist: " + fileName);

               // update the rootFile
               try
               {
                  vfsRoot = new URLLoaderAdapter(expandedDirectory.toURL());
               }
               catch (MalformedURLException e)
               {
               }
            }

         }
      }

      UnifiedServiceRefMetaData serviceRef = (UnifiedServiceRefMetaData)sref;
      serviceRef.setVfsRoot(vfsRoot);
      try
      {
         if (getServiceRefType(serviceRef, loader) == Type.JAXRPC)
         {
            getJaxrpcBinder().setupServiceRef(encCtx, encName, null, serviceRef, loader);
         }
         else
         {
            AnnotatedElement anElement = (AnnotatedElement)sref.getAnnotatedElement();
            getJaxwsBinder().setupServiceRef(encCtx, encName, anElement, serviceRef, loader);
         }
      }
      finally
      {
         sref.setProcessed(true);
      }
   }

   public Object newChild(ServiceRefElement ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      return objectFactory.newChild(ref, navigator, namespaceURI, localName, attrs);
   }

   public void setValue(ServiceRefElement ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      objectFactory.setValue(ref, navigator, namespaceURI, localName, value);
   }

   private Type getServiceRefType(UnifiedServiceRefMetaData serviceRef, ClassLoader loader) throws NamingException
   {
      // The service-ref-type is JAXWS specific
      String serviceRefType = serviceRef.getServiceRefType();
      if (serviceRefType != null || serviceRef.getAnnotatedElement() != null)
         return Type.JAXWS;

      // The mapping-file is JAXRPC specific
      if (serviceRef.getMappingFile() != null)
         return Type.JAXRPC;

      String siName = serviceRef.getServiceInterface();
      if (siName == null)
         throw new IllegalStateException("<service-interface> cannot be null");

      if (siName.equals("javax.xml.rpc.Service"))
         return Type.JAXRPC;

      try
      {
         Class siClass = loader.loadClass(siName);
         if (javax.xml.ws.Service.class.isAssignableFrom(siClass))
            return Type.JAXWS;
         else if (javax.xml.rpc.Service.class.isAssignableFrom(siClass))
            return Type.JAXRPC;
         else
            throw new IllegalStateException("Illegal service interface: " + siName);
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalStateException("Cannot load <service-interface>: " + siName);
      }
   }
}
