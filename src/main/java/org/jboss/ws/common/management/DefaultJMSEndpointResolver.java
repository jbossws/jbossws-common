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
package org.jboss.ws.common.management;

import java.util.Iterator;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.Topic;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.JMSEndpointResolver;

/**
 * Default resolver for JMS endpoints
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Mar-2010
 *
 */
public class DefaultJMSEndpointResolver implements JMSEndpointResolver
{
   private String fromName;

   public void setDestination(Destination destination)
   {
      if (destination instanceof Queue)
         setFromName(destination, true);
      else if (destination instanceof Topic)
         setFromName(destination, false);
   }

   protected void setFromName(Destination destination, boolean queue)
   {
      try
      {
         fromName = queue ? "queue/" + ((Queue)destination).getQueueName() : "topic/" + ((Topic)destination).getTopicName();
      }
      catch (JMSException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Endpoint query(Iterator<Endpoint> endpoints)
   {
      Endpoint endpoint = null;
      while (endpoints.hasNext())
      {
         Endpoint aux = endpoints.next();
         String jmsProp = aux.getName().getKeyProperty("jms");
         if (jmsProp != null && jmsProp.equals(fromName))
         {
            endpoint = aux;
            break;
         }
      }
      return endpoint;
   }
}
