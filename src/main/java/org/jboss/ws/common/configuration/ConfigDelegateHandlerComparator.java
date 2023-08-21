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

import java.io.Serializable;
import java.util.Comparator;

import jakarta.xml.ws.handler.Handler;

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
