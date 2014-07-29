/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.management;

import java.io.File;
import java.net.UnknownHostException;

import javax.management.ObjectName;

import org.jboss.ws.common.ObjectNameFactory;

public interface AbstractServerConfigMBean
{
   /** The object name in the MBean server */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ws:service=ServerConfig");
   
   String getImplementationTitle();

   String getImplementationVersion();
   
   File getHomeDir();
   
   File getServerTempDir();

   File getServerDataDir();

   String getWebServiceHost();
   
   void setWebServiceHost(String host) throws UnknownHostException;
   
   int getWebServicePort();
   
   void setWebServicePort(int port);
   
   int getWebServiceSecurePort();

   void setWebServiceSecurePort(int port);
   
   boolean isModifySOAPAddress();
   
   void setModifySOAPAddress(boolean flag);

   String getWebServicePathRewriteRule();

   void setWebServicePathRewriteRule(String path);
}
