/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.invocation;

import java.security.Principal;

import javax.ejb.EJBContext;
import javax.xml.ws.WebServiceContext;

import org.jboss.wsf.spi.invocation.WebServiceContextDelegate;

/**
 * EJB web service context which security related methods delegate to EJB container.
 *
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class WebServiceContextEJB extends WebServiceContextDelegate
{

   public WebServiceContextEJB(final WebServiceContext ctx)
   {
      super(ctx);
   }

   public Principal getUserPrincipal()
   {
      final EJBContext ejbContext = getAttachment(EJBContext.class);
      return ejbContext.getCallerPrincipal();
   }

   public boolean isUserInRole(String role)
   {
      final EJBContext ejbContext = getAttachment(EJBContext.class);
      return ejbContext.isCallerInRole(role);
   }

}
