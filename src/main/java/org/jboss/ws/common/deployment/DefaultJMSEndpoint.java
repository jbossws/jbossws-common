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
package org.jboss.ws.common.deployment;

import java.net.URI;

import javax.naming.Context;

import org.jboss.wsf.spi.deployment.EndpointType;
import org.jboss.wsf.spi.deployment.JMSEndpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * Default JMSEndpoint implementation
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class DefaultJMSEndpoint extends AbstractDefaultEndpoint implements JMSEndpoint
{
   
   private String targetDestination;
   private String replyDestination;
   private URI requestURI;
   
   DefaultJMSEndpoint(String targetBean)
   {
      super(targetBean);
   }
   
   public String getTargetDestination()
   {
      return targetDestination;
   }

   public void setTargetDestination(String targetDestination)
   {
      this.targetDestination = targetDestination;
   }

   public String getReplyDestination()
   {
      return replyDestination;
   }

   public void setReplyDestination(String replyDestination)
   {
      this.replyDestination = replyDestination;
   }

   public URI getRequestURI()
   {
      return this.requestURI;
   }

   public void setRequestURI(URI requestURI)
   {
      this.requestURI = requestURI;
   }
   
   public String getAddress() 
   {
      if (targetDestination != null)
      {
         StringBuffer address = new StringBuffer();
         address.append("jms:jndi:" + targetDestination);
         if (this.getReplyDestination() != null)
         {
            address.append("?replyToName =" + this.getReplyDestination());
         }
         return address.toString();
      }
      return super.getAddress();  
   }

   @Override
   public EndpointMetrics getEndpointMetrics()
   {
      return null;
   }
   @Override
   public Context getJNDIContext()
   {
      return null;
   }
   
   @Override
   public void setEndpointMetrics(EndpointMetrics metrics)
   {
         
   }

}
