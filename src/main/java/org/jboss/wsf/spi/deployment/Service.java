/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.wsf.spi.deployment;

// $Id$

import java.util.List;

import org.jboss.wsf.spi.Extensible;

/**
 * A general service deployment.
 * 
 * Maintains a named set of EndpointDeployments 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 20-Apr-2007 
 */
public interface Service extends Extensible
{
   /** Get the deployment this service belongs to */
   Deployment getDeployment();
   
   /** Set the deployment this service belongs to */
   void setDeployment(Deployment dep);
   
   /** Add an endpoint to the service */
   void addEndpoint(Endpoint endpoint);
   
   /** Get the list of endpoints */
   List<Endpoint> getEndpoints();
   
   /** Get an endpoint by name */
   Endpoint getEndpointByName(String simpleName);
   
   /** Get the context root for this service */
   String getContextRoot();
   
   /** Set the context root for this service */
   void setContextRoot(String contextRoot);
}
