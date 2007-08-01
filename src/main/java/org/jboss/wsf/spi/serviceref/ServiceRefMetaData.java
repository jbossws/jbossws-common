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
package org.jboss.wsf.spi.serviceref;

// $Id$

import java.io.Serializable;

import org.w3c.dom.Element;

/**
 * An abstract service-ref meta data object.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 08-Mar-2007
 */
public abstract class ServiceRefMetaData extends ServiceRefElement implements Serializable
{
   public abstract String getServiceRefName();

   public abstract void setServiceRefName(String name);

   public abstract Object getAnnotatedElement();

   public abstract void setAnnotatedElement(Object anElement);

   public abstract boolean isProcessed();

   public abstract void setProcessed(boolean flag);

   public abstract void importStandardXml(Element element);

   public abstract void importJBossXml(Element element);

   public abstract void merge(ServiceRefMetaData targetRef);
}
