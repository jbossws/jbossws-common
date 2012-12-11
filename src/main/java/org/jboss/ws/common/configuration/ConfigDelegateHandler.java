/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.configuration;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;


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
}
