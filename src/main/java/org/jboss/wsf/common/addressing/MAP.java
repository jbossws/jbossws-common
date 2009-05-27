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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * Message Addressing Properties is a wrapper for the stack-specific JSR-261 addressing properties
 * classes implemented by JBossWS Native and CXF. It is used to localize dependence upon the WS
 * stack.
 * 
 * @author Andrew Dinn (adinn@redhat.com)
 * @author alessio.soldano@jboss.com
 * 
 */
public interface MAP
{
   public String getTo();

   public MAPEndpoint getFrom();

   public String getMessageID();

   public String getAction();

   public MAPEndpoint getFaultTo();

   public MAPEndpoint getReplyTo();

   public MAPRelatesTo getRelatesTo();

   public void setTo(String address);

   public void setFrom(MAPEndpoint epref);

   public void setMessageID(String messageID);

   public void setAction(String action);

   public void setReplyTo(MAPEndpoint epref);

   public void setFaultTo(MAPEndpoint epref);

   public void setRelatesTo(MAPRelatesTo relatesTo);

   public void addReferenceParameter(Element refParam);
   
   public List<Object> getReferenceParameters();

   public void initializeAsDestination(MAPEndpoint epref);

   public void installOutboundMapOnServerSide(Map<String, Object> requestContext, MAP map);
   
   public void installOutboundMapOnClientSide(Map<String, Object> requestContext, MAP map);

}
