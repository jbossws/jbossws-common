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
package org.jboss.test.wsf.spi.tools;

import org.jboss.wsf.spi.tools.WSContractProvider;

import java.io.File;
import java.io.PrintStream;

/**
 * @author Heiko.Braun@jboss.com
 */
public class CmdProvideTracker extends WSContractProvider
{

   public static String LAST_EVENT = "";

   public void setGenerateWsdl(boolean generateWsdl)
   {
      LAST_EVENT += "setGenerateWsdl";
   }

   public void setExtension(boolean extension)
   {
      LAST_EVENT += "setExtension";
   }

   public void setGenerateSource(boolean generateSource)
   {
      LAST_EVENT += "setGenerateSource";
   }

   public void setOutputDirectory(File directory)
   {
      LAST_EVENT += "setOutputDirectory";
   }

   public void setResourceDirectory(File directory)
   {
      LAST_EVENT += "setResourceDirectory";
   }

   public void setSourceDirectory(File directory)
   {
      LAST_EVENT += "setSourceDirectory";
   }

   public void setClassLoader(ClassLoader loader)
   {
      LAST_EVENT += "setClassLoader";
   }

   public void provide(String endpointClass)
   {
      LAST_EVENT += "provide";   
   }

   public void provide(Class<?> endpointClass)
   {
      LAST_EVENT += "provide";
   }

   public void setMessageStream(PrintStream messageStream)
   {
      LAST_EVENT += "setMessageStream";
   }
}
