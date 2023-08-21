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
package org.jboss.ws.common.deployment;

import java.net.URI;

import org.jboss.wsf.spi.deployment.JMSEndpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * Default JMSEndpoint implementation
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class DefaultJMSEndpoint extends AbstractDefaultEndpoint implements JMSEndpoint
{
   private volatile String targetDestination;
   private volatile String replyDestination;
   private volatile URI requestURI;
   
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
   public void setEndpointMetrics(EndpointMetrics metrics)
   {
         
   }

}
