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
package org.jboss.ws.common.reflection;

import java.lang.reflect.Method;

/**
 * A finder that looks for methods matching the criteria.
 * The matching methods and collection of matching methods
 * can be validated in subclasses.
 * 
 * @author ropalka@redhat.com
 */
public class MethodFinder
extends AbstractClassProcessor<Method>
{
   
   @Override
   public final Method[] getAccessibleObjects(final Class<?> clazz)
   {
      return clazz.getDeclaredMethods();
   }

}
