/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.integration;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;

/**
 * Cross WS stack and JBoss AS integration helper.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class WSHelper
{
   
   /** Logger. */
   private static final Logger LOG = Logger.getLogger( WSHelper.class );

   /**
    * Forbidden constructor.
    */
   private WSHelper()
   {
      super();
   }

   /**
    * Returns required attachment value from webservice deployment.
    * 
    * @param <A> expected value
    * @param dep webservice deployment
    * @param key attachment key
    * @return required attachment 
    * @throws IllegalStateException if attachment value is null
    */
   public static <A> A getRequiredAttachment( final Deployment dep, final Class< A > key )
   {
      final A value = dep.getAttachment( key );
      if ( value == null )
      {
         WSHelper.LOG.error( "Cannot find attachment in webservice deployment: " + key );
         throw new IllegalStateException();
      }
      
      return value;
   }
   
   /**
    * Returns optional attachment value from webservice deployment or null if not bound.
    * 
    * @param <A> expected value
    * @param dep webservice deployment
    * @param key attachment key
    * @return optional attachment value or null 
    */
   public static <A> A getOptionalAttachment( final Deployment dep, final Class< A > key )
   {
      return dep.getAttachment( key );
   }

   /**
    * Returns true if webservice deployment have attachment value associated with the <b>key</b>.
    *  
    * @param dep webservice deployment
    * @param key attachment key
    * @return true if contains attachment, false otherwise
    */
   public static boolean hasAttachment( final Deployment dep, final Class< ? > key )
   {
      return WSHelper.getOptionalAttachment( dep, key ) != null;
   }

   /**
    * Returns true if deployment represents JAXRPC EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXRPC EJB deployment, false otherwise
    */
   public static boolean isJaxrpcEjbDeployment( final Deployment dep )
   {
      return DeploymentType.JAXRPC_EJB21.equals( dep.getType() );
   }

   /**
    * Returns true if deployment represents JAXRPC JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXRPC JSE deployment, false otherwise
    */
   public static boolean isJaxrpcJseDeployment( final Deployment dep )
   {
      return DeploymentType.JAXRPC_JSE.equals( dep.getType() );
   }

   /**
    * Returns true if deployment represents JAXWS EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXWS EJB deployment, false otherwise
    */
   public static boolean isJaxwsEjbDeployment( final Deployment dep )
   {
      return DeploymentType.JAXWS_EJB3.equals( dep.getType() );
   }

   /**
    * Returns true if deployment represents JAXWS JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXWS JSE deployment, false otherwise
    */
   public static boolean isJaxwsJseDeployment( final Deployment dep )
   {
      return DeploymentType.JAXWS_JSE.equals( dep.getType() );
   }
   
   /**
    * Returns true if deployment represents either JAXWS JSE or JAXRPC JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXWS JSE or JAXRPC JSE deployment, false otherwise.
    */
   public static boolean isJseDeployment( final Deployment dep )
   {
      final boolean isJaxwsJse = WSHelper.isJaxwsJseDeployment( dep );
      final boolean isJaxrpcJse = WSHelper.isJaxrpcJseDeployment( dep );

      return isJaxwsJse || isJaxrpcJse;
   }

   /**
    * Returns true if deployment represents either JAXWS EJB or JAXRPC EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXWS EJB or JAXRPC EJB deployment, false otherwise
    */
   public static boolean isEjbDeployment( final Deployment dep )
   {
      final boolean isJaxwsEjb = WSHelper.isJaxwsEjbDeployment( dep );
      final boolean isJaxrpcEjb = WSHelper.isJaxrpcEjbDeployment( dep );

      return isJaxwsEjb || isJaxrpcEjb;
   }

   /**
    * Returns true if deployment represents either JAXWS EJB or JAXWS JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXWS EJB or JAXWS JSE deployment, false otherwise
    */
   public static boolean isJaxwsDeployment( final Deployment dep )
   {
      final boolean isJaxwsEjb = WSHelper.isJaxwsEjbDeployment( dep );
      final boolean isJaxwsJse = WSHelper.isJaxwsJseDeployment( dep );

      return isJaxwsEjb || isJaxwsJse;
   }

   /**
    * Returns true if deployment represents either JAXRPC EJB or JAXRPC JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXRPC EJB or JAXRPC JSE deployment, false otherwise
    */
   public static boolean isJaxrpcDeployment( final Deployment dep )
   {
      final boolean isJaxrpcEjb = WSHelper.isJaxrpcEjbDeployment( dep );
      final boolean isJaxrpcJse = WSHelper.isJaxrpcJseDeployment( dep );

      return isJaxrpcEjb || isJaxrpcJse;
   }
}
