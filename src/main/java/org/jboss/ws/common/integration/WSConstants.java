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

/**
 * Cross WS stack and JBoss AS integration constants.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class WSConstants
{
   
   /**
    * Stack specific context parameters configuration property.
    */
   public static final String STACK_CONTEXT_PARAMS = "stack.context.parameters";

   /**
    * Stack specific transport class configuration property.
    */
   public static final String STACK_TRANSPORT_CLASS = "stack.transport.class";
   
   /**
    * Stack specific transport class provider configuration property.
    */
   public static final String STACK_TRANSPORT_CLASS_PROVIDER = "stack.transport.class.provider";
   
   /**
    * JBoss WS config name property.
    */
   public static final String JBOSSWS_CONFIG_NAME = "jbossws-config-name";
   
   /**
    * JBoss WS config file property.
    */
   public static final String JBOSSWS_CONFIG_FILE = "jbossws-config-file";

   /**
    * Forbidden constructor.
    */
   private WSConstants()
   {
      super();
   }

}
