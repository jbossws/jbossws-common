/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.common.integration;

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
      return dep.getService().getEndpoints( JAXWS_JSE_ENDPOINT_FILTER ).size() > 0;
   }

   /**
    * Returns true if deployment represents JAXWS EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if JAXWS EJB deployment, false otherwise
    */
   public static boolean isJaxwsEjbDeployment( final Deployment dep )
   {
     return dep.getService().getEndpoints( JAXWS_EJB_ENDPOINT_FILTER ).size() > 0;
   }

   /**
    * Returns true if deployment represents a JSE deployment.
    *
    * @param dep webservice deployment
    * @return true if JSE deployment, false otherwise.
    */
   public static boolean isJseDeployment( final Deployment dep )
   {
      return isJaxwsJseDeployment( dep );
   }

   /**
    * Returns true if deployment represents an EJB deployment.
    *
    * @param dep webservice deployment
    * @return true if either EJB deployment, false otherwise
    */
   public static boolean isEjbDeployment( final Deployment dep )
   {
      return isJaxwsEjbDeployment( dep );
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
    * Returns true if endpoint represents a JSE endpoint.
    *
    * @param ep webservice endpoint
    * @return true if JSE endpoint, false otherwise
    */
   public static boolean isJseEndpoint( final Endpoint ep )
   {
      return isJaxwsJseEndpoint( ep );
   }

   /**
    * Returns true if endpoint represents either an EJB endpoint.
    *
    * @param ep webservice endpoint
    * @return true if EJB endpoint, false otherwise
    */
   public static boolean isEjbEndpoint( final Endpoint ep )
   {
      return isJaxwsEjbEndpoint( ep );
   }

   /**
    * Returns true if endpoint represents a JAXWS endpoint.
    *
    * @param ep webservice endpoint
    * @return true if either JAXWS endpoint, false otherwise
    */
   public static boolean isJaxwsEndpoint( final Endpoint ep )
   {
      return isJaxwsJseEndpoint( ep );
   }

}
