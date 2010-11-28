/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.spi.tools.ant;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Security actions for this package
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Jun-2009
 *
 */
class SecurityActions
{
   /**
    * Get context classloader.
    * 
    * @return the current context classloader
    */
   static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }

   /**
    * Set context classloader.
    *
    * @param cl the classloader
    * @return previous context classloader
    * @throws Throwable for any error
    */
   static ClassLoader setContextClassLoader(final ClassLoader cl)
   {
      if (System.getSecurityManager() == null)
      {
         ClassLoader result = Thread.currentThread().getContextClassLoader();
         if (cl != null)
            Thread.currentThread().setContextClassLoader(cl);
         return result;
      }
      else
      {
         try
         {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<ClassLoader>() {
               public ClassLoader run() throws Exception
               {
                  try
                  {
                     ClassLoader result = Thread.currentThread().getContextClassLoader();
                     if (cl != null)
                        Thread.currentThread().setContextClassLoader(cl);
                     return result;
                  }
                  catch (Exception e)
                  {
                     throw e;
                  }
                  catch (Error e)
                  {
                     throw e;
                  }
                  catch (Throwable e)
                  {
                     throw new RuntimeException("Error setting context classloader", e);
                  }
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException("Error running privileged action", e.getCause());
         }
      }
   }
   
   /**
    * Get classloader from class.
    *
    * @param clazz the class
    * @return class's classloader
    */
   static ClassLoader getClassLoader(final Class<?> clazz)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return clazz.getClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return clazz.getClassLoader();
            }
         });
      }
   }

}
