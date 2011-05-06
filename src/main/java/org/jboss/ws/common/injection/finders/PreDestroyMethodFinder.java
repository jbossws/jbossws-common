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
package org.jboss.ws.common.injection.finders;

import javax.annotation.PreDestroy;

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
