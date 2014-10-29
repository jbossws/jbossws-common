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

import java.io.Serializable;
import java.util.Comparator;

import javax.xml.ws.handler.Handler;

/**
 * A Handler comparator properly dealing with PRE/POST ConfigDelegateHandler instances
 * 
 * @author alessio.soldano@jboss.com
 * @since 06-Jun-2012
 *
 */
@SuppressWarnings("rawtypes")
public final class ConfigDelegateHandlerComparator<T extends Handler> implements Comparator<T>, Serializable
{
   static final long serialVersionUID = 5045492270035185007L;

   @Override
   public int compare(Handler o1, Handler o2)
   {
      int i1 = 0;
      int i2 = 0;
      if (o1 instanceof ConfigDelegateHandler)
      {
         i1 = ((ConfigDelegateHandler) o1).isPre() ? -1 : 1;
      }
      if (o2 instanceof ConfigDelegateHandler)
      {
         i2 = ((ConfigDelegateHandler) o2).isPre() ? -1 : 1;
      }
      return i1 - i2;
   }
}