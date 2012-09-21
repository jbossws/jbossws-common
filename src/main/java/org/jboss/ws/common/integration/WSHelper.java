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

import static org.jboss.wsf.spi.deployment.DeploymentType.JAXRPC;
import static org.jboss.wsf.spi.deployment.DeploymentType.JAXWS;
import static org.jboss.wsf.spi.deployment.EndpointType.JAXRPC_EJB21;
import static org.jboss.wsf.spi.deployment.EndpointType.JAXRPC_JSE;
import static org.jboss.wsf.spi.deployment.EndpointType.JAXWS_EJB3;
import static org.jboss.wsf.spi.deployment.EndpointType.JAXWS_JSE;

import org.jboss.ws.common.Messages;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointTypeFilter;

/**
 * Cross WS stack and JBoss AS integration helper.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class WSHelper {

   private static final EndpointTypeFilter JAXRPC_EJB_ENDPOINT_FILTER = new EndpointTypeFilterImpl( JAXRPC_EJB21 );
   private static final EndpointTypeFilter JAXRPC_JSE_ENDPOINT_FILTER = new EndpointTypeFilterImpl( JAXRPC_JSE );
   private static final EndpointTypeFilter JAXWS_EJB_ENDPOINT_FILTER = new EndpointTypeFilterImpl( JAXWS_EJB3 );
   private static final EndpointTypeFilter JAXWS_JSE_ENDPOINT_FILTER = new EndpointTypeFilterImpl( JAXWS_JSE );
   private static final String WAR_EXTENSION = ".war";
   private static final String JAR_EXTENSION = ".jar";
   private static final String EAR_EXTENSION = ".ear";

   /**
    * Forbidden constructor.
    */
   private WSHelper()
   {
      // forbidden inheritance
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
         throw Messages.MESSAGES.cannotFindAttachmentInDeployment(key, dep.getSimpleName());
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
    * Returns true if deployment represents JAXWS JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXWS JSE deployment, false otherwise
    */
   public static boolean isJaxwsJseDeployment( final Deployment dep )
   {
      return isJaxwsDeployment( dep ) && dep.getService().getEndpoints( JAXWS_JSE_ENDPOINT_FILTER ).size() > 0;
   }

   /**
    * Returns true if deployment represents JAXWS EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXWS EJB deployment, false otherwise
    */
   public static boolean isJaxwsEjbDeployment( final Deployment dep )
   {
     return isJaxwsDeployment( dep ) && dep.getService().getEndpoints( JAXWS_EJB_ENDPOINT_FILTER ).size() > 0;
   }

   /**
    * Returns true if deployment represents JAXRPC JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXRPC JSE deployment, false otherwise
    */
   public static boolean isJaxrpcJseDeployment( final Deployment dep )
   {
      return isJaxrpcDeployment( dep ) && dep.getService().getEndpoints( JAXRPC_JSE_ENDPOINT_FILTER ).size() > 0;
   }

   /**
    * Returns true if deployment represents JAXRPC EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXRPC EJB deployment, false otherwise
    */
   public static boolean isJaxrpcEjbDeployment( final Deployment dep )
   {
      return isJaxrpcDeployment( dep ) && dep.getService().getEndpoints( JAXRPC_EJB_ENDPOINT_FILTER ).size() > 0;
   }

   /**
    * Returns true if deployment represents either JAXWS JSE or JAXRPC JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXWS JSE or JAXRPC JSE deployment, false otherwise.
    */
   public static boolean isJseDeployment( final Deployment dep )
   {
      return isJaxwsJseDeployment( dep ) || isJaxrpcJseDeployment( dep );
   }

   /**
    * Returns true if deployment represents either JAXWS EJB or JAXRPC EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXWS EJB or JAXRPC EJB deployment, false otherwise
    */
   public static boolean isEjbDeployment( final Deployment dep )
   {
      return isJaxwsEjbDeployment( dep ) || isJaxrpcEjbDeployment( dep );
   }

   /**
    * Returns true if deployment represents either JAXWS EJB or JAXWS JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXWS EJB or JAXWS JSE deployment, false otherwise
    */
   public static boolean isJaxwsDeployment( final Deployment dep )
   {
      return JAXWS == dep.getType();
   }

   /**
    * Returns true if deployment represents either JAXRPC EJB or JAXRPC JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if either JAXRPC EJB or JAXRPC JSE deployment, false otherwise
    */
   public static boolean isJaxrpcDeployment( final Deployment dep )
   {
      return JAXRPC == dep.getType();
   }

   /**
    * Returns true if archive name ends with '.jar' suffix.
    *
    * @param dep webservice deployment
    * @return true if archive name ends with '.jar' suffix, false otherwise
    */
   public static boolean isJarArchive( final Deployment dep )
   {
      return dep.getSimpleName().endsWith(JAR_EXTENSION);
   }

   /**
    * Returns true if archive name ends with '.war' suffix.
    *
    * @param dep webservice deployment
    * @return true if archive name ends with '.war' suffix, false otherwise
    */
   public static boolean isWarArchive( final Deployment dep )
   {
      return dep.getSimpleName().endsWith(WAR_EXTENSION);
   }

   /**
    * Returns true if archive name ends with '.ear' suffix.
    *
    * @param dep webservice deployment
    * @return true if archive name ends with '.ear' suffix, false otherwise
    */
   public static boolean isEarArchive( final Deployment dep )
   {
      return dep.getSimpleName().endsWith(EAR_EXTENSION);
   }

   /**
    * Returns true if endpoint represents JAXWS JSE endpoint.
    *
    * @param ep webservice endpoint
    * @return true if either JAXWS JSE endpoint, false otherwise
    */
   public static boolean isJaxwsJseEndpoint( final Endpoint ep )
   {
      return JAXWS_JSE == ep.getType();
   }

   /**
    * Returns true if endpoint represents JAXWS EJB3 endpoint.
    *
    * @param ep webservice endpoint
    * @return true if JAXWS EJB3 endpoint, false otherwise
    */
   public static boolean isJaxwsEjbEndpoint( final Endpoint ep )
   {
      return JAXWS_EJB3 == ep.getType();
   }

   /**
    * Returns true if endpoint represents JAXRPC JSE endpoint.
    *
    * @param ep webservice endpoint
    * @return true if JAXRPC JSE endpoint, false otherwise
    */
   public static boolean isJaxrpcJseEndpoint( final Endpoint ep )
   {
      return JAXRPC_JSE == ep.getType();
   }

   /**
    * Returns true if endpoint represents JAXRPC EJB21 endpoint.
    *
    * @param ep webservice endpoint
    * @return true if JAXRPC EJB21 endpoint, false otherwise
    */
   public static boolean isJaxrpcEjbEndpoint( final Endpoint ep )
   {
      return JAXRPC_EJB21 == ep.getType();
   }

   /**
    * Returns true if endpoint represents either JAXWS JSE or JAXRPC JSE endpoint.
    *
    * @param ep webservice endpoint
    * @return true if either JAXWS JSE or JAXRPC JSE endpoint, false otherwise
    */
   public static boolean isJseEndpoint( final Endpoint ep )
   {
      return isJaxwsJseEndpoint( ep ) || isJaxrpcJseEndpoint( ep );
   }

   /**
    * Returns true if endpoint represents either JAXWS EJB3 or JAXRPC EJB21 endpoint.
    *
    * @param ep webservice endpoint
    * @return true if either JAXWS EJB3 or JAXRPC EJB21 endpoint, false otherwise
    */
   public static boolean isEjbEndpoint( final Endpoint ep )
   {
      return isJaxwsEjbEndpoint( ep ) || isJaxrpcEjbEndpoint( ep );
   }

   /**
    * Returns true if endpoint represents either JAXWS JSE or JAXWS EJB3 endpoint.
    *
    * @param ep webservice endpoint
    * @return true if either JAXWS JSE or JAXWS EJB3 endpoint, false otherwise
    */
   public static boolean isJaxwsEndpoint( final Endpoint ep )
   {
      return isJaxwsJseEndpoint( ep ) || isJaxwsEjbEndpoint( ep );
   }

   /**
    * Returns true if endpoint represents either JAXRPC JSE or JAXRPC EJB21 endpoint.
    *
    * @param ep webservice endpoint
    * @return true if either JAXRPC JSE or JAXRPC EJB21 endpoint, false otherwise
    */
   public static boolean isJaxrpcEndpoint( final Endpoint ep )
   {
      return isJaxrpcJseEndpoint( ep ) || isJaxrpcEjbEndpoint( ep );
   }

}
