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
package org.jboss.ws.common.injection.finders;

import jakarta.annotation.PreDestroy;

/**
 * @PreDestroy method finder.
 *
 * The PreDestroy annotation is used on methods as a callback notification to signal that the instance
 * is in the process of being removed by the container. The method annotated with PreDestroy is typically
 * used to release resources that it has been holding. This annotation MUST be supported by all container
 * managed objects that support PostConstruct except the application client container in Java EE 5.
 * The method on which the PreDestroy annotation is applied MUST fulfill all of the following criteria:
 * <ul>
 *   <li>The method MUST NOT have any parameters.
 *   <li>The return type of the method MUST be void.
 *   <li>The method MUST NOT throw a checked exception.
 *   <li>The method on which PreDestroy is applied MAY be public, protected, package private or private.
 *   <li>The method MUST NOT be static.
 *   <li>The method MAY be final.
 *   <li>If the method throws an unchecked exception it is ignored.
 * </ul>
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class PreDestroyMethodFinder
extends AbstractPostConstructPreDestroyAnnotatedMethodFinder<PreDestroy>
{

   /**
    * Constructor.
    */
   public PreDestroyMethodFinder()
   {
      super(PreDestroy.class);
   }

}
