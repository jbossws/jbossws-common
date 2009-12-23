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
package org.jboss.test.ws.common.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A dummy document builder factory just for testing in {@link JBossWSDocumentBuilderFactoryTestCase}.
 * 
 * @author alessio.soldano@jboss.com
 * @since 23-Dec-2009
 *
 */
public class DummyDocumentBuilderFactory extends DocumentBuilderFactory
{
   @Override
   public Object getAttribute(String name) throws IllegalArgumentException
   {
      return null;
   }

   @Override
   public boolean getFeature(String name) throws ParserConfigurationException
   {
      return false;
   }

   @Override
   public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException
   {
      return null;
   }

   @Override
   public void setAttribute(String name, Object value) throws IllegalArgumentException
   {
   }

   @Override
   public void setFeature(String name, boolean value) throws ParserConfigurationException
   {
   }
}
