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
package org.jboss.wsf.common.management;

import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

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
