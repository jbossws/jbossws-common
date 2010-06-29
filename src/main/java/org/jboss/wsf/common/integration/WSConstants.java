/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.common.integration;

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
