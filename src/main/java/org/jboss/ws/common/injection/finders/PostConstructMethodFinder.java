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

import jakarta.annotation.PostConstruct;

/**
 * @PostConstruct method finder.
 *
 * The PostConstruct annotation is used on a method that needs to be executed after dependency injection is done
 * to perform any initialization. This method MUST be invoked before the class is put into service. This annotation
 * MUST be supported on all classes that support dependency injection. The method annotated with PostConstruct MUST
 * be invoked even if the class does not request any resources to be injected. Only one method can be annotated with
 * this annotation. The method on which the PostConstruct annotation is applied MUST fulfill all of the following criteria:
 * <ul>
 *   <li>The method MUST NOT have any parameters.
 *   <li>The return type of the method MUST be void.
 *   <li>The method MUST NOT throw a checked exception.
 *   <li>The method on which PostConstruct is applied MAY be public, protected, package private or private.
 *   <li>The method MUST NOT be static.
 *   <li>The method MAY be final.
 *   <li>If the method throws an unchecked exception the class MUST NOT be put into service.
 * </ul>
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class PostConstructMethodFinder
extends AbstractPostConstructPreDestroyAnnotatedMethodFinder<PostConstruct>
{

   /**
    * Constructor.
    */
   public PostConstructMethodFinder()
   {
      super(PostConstruct.class);
   }

}
