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
package org.jboss.wsf.common.addressing;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

/**
 * MAPBuilder is a helper used to create objects used with class MAP.
 * 
 * @author Andrew Dinn (adinn@redhat.com)
 * @author alessio.soldano@jboss.com
 * 
 */
public interface MAPBuilder
{
   public MAP newMap();

   /**
    * retrieve the inbound server message address properties attached to a message context
    * @param ctx the server message context
    * @return
    */
   public MAP inboundMap(Map<String, Object> ctx);

   /**
    * retrieve the outbound client message address properties attached to a message request map
    * @param ctx the client request properties map
    * @return
    */
   public MAP outboundMap(Map<String, Object> ctx);

   public MAPConstants newConstants();

   public MAPEndpoint newEndpoint(String address);

   public MAPRelatesTo newRelatesTo(String id, QName type);

}
