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
package org.jboss.wsf.common.javax.finders;

import javax.annotation.PostConstruct;

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
 * @author ropalka@redhat.com
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
