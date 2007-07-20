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
package org.jboss.wsf.framework.invocation;

// $Id$

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.invocation.ResourceInjector;

/**
 * Inject the JAXWS WebServiceContext
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Jan-2007
 */
public class WebServiceContextInjector extends ResourceInjector
{
   // provide logging
   private static Logger log = Logger.getLogger(WebServiceContextInjector.class);

   WebServiceContextInjector()
   {
   }

   public void inject(Object instance, WebServiceContext context)
   {
      try
      {
         // scan fields that are marked with @Resource
         Field[] fields = instance.getClass().getDeclaredFields();
         for (Field field : fields)
         {
            Class type = field.getType();
            if (type == WebServiceContext.class && field.isAnnotationPresent(Resource.class))
            {
               field.setAccessible(true);
               field.set(instance, context);
            }
         }

         // scan methods that are marked with @Resource
         Method[] methods = instance.getClass().getDeclaredMethods();
         for (Method method : methods)
         {
            Class[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0] == WebServiceContext.class && method.isAnnotationPresent(Resource.class))
            {
               method.setAccessible(true);
               method.invoke(instance, new Object[] { context });
            }
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         log.warn("Cannot inject WebServiceContext", ex);
      }
   }
}
