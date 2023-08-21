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
package org.jboss.ws.common.configuration;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;


/**
 * JBossWS client / endpoint configuration-contributed handler
 * 
 * @author alessio.soldano@jboss.com
 * @since 06-Jun-2012
 *
 */
public class ConfigDelegateHandler<T extends MessageContext> implements Handler<T>
{
   protected final Handler<T> delegate;
   private final boolean isPre;
   
   public ConfigDelegateHandler(Handler<T> delegate, boolean isPre) {
      this.delegate = delegate;
      this.isPre = isPre;
   }

   @Override
   public boolean handleMessage(T context)
   {
      return delegate.handleMessage(context);
   }

   @Override
   public boolean handleFault(T context)
   {
      return delegate.handleFault(context);
   }

   @Override
   public void close(MessageContext context)
   {
      delegate.close(context);
   }
   
   public boolean isPre()
   {
      return isPre;
   }
   
   public Handler<T> getDelegate()
   {
      return delegate;
   }
}
