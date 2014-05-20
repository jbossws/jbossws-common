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
package org.jboss.ws.common.invocation;

import static org.jboss.ws.common.Loggers.MONITORING_LOGGER;

import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.Record.MessageType;
import org.jboss.ws.api.monitoring.RecordGroupAssociation;
import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.common.monitoring.RecordFactory;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;

/**
 * This handler is responsible for collecting the information about the
 * messages being exchanged and recording them on the server side. This
 * is performed delegating to the RecordProcessors installed into the
 * current endpoint.
 * 
 * @author alessio.soldano@jboss.com
 * @since 8-Dec-2007
 */
public class RecordingServerHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   @SuppressWarnings("unchecked")
   protected boolean handleInbound(SOAPMessageContext ctx)
   {
      Endpoint endpoint = EndpointAssociation.getEndpoint();
      if (endpoint != null && isRecording(endpoint))
      {
         Record record = RecordFactory.newRecord();
         RecordGroupAssociation.pushGroupID(record.getGroupID());
         record.setDate(new Date());
         HttpServletRequest httpServletRequest = (HttpServletRequest)ctx.get(MessageContext.SERVLET_REQUEST);
         if (httpServletRequest != null)
         {
            try
            {
               record.setDestinationHost(new URL(httpServletRequest.getRequestURL().toString()).getHost());
               record.setSourceHost(httpServletRequest.getRemoteHost());
            }
            catch (Exception e)
            {
               MONITORING_LOGGER.unableToReadFromHttpServletRequest(e);
            }
         }
         record.setHeaders((Map<String,List<String>>)(ctx.get(MessageContext.HTTP_REQUEST_HEADERS)));
         record.setMessageType(MessageType.INBOUND);
         record.setOperation((QName)ctx.get(MessageContext.WSDL_OPERATION));
         boolean processEnvelope = false;
         for (Iterator<RecordProcessor> it = endpoint.getRecordProcessors().iterator(); it.hasNext() && !processEnvelope; )
         {
            processEnvelope = it.next().isProcessEnvelope();
         }
         if (processEnvelope) //skip message processing if not required since it's very time-consuming
         {
            try
            {
               SOAPEnvelope soapEnv = ctx.getMessage().getSOAPPart().getEnvelope();
               if (soapEnv != null)
               {
                  record.setEnvelope(DOMWriter.printNode(soapEnv, true));
               }
            }
            catch (SOAPException ex)
            {
               MONITORING_LOGGER.cannotTraceSoapMessage(ex);
            }
         }
         endpoint.processRecord(record);
      }
      return true;
   }

   @SuppressWarnings("unchecked")
   protected boolean handleOutbound(SOAPMessageContext ctx)
   {
      Endpoint endpoint = EndpointAssociation.getEndpoint();
      if (endpoint != null && isRecording(endpoint))
      {
         String groupID = RecordGroupAssociation.popGroupID();
         Record record = RecordFactory.newRecord(groupID);
         record.setDate(new Date());
         record.setHeaders((Map<String,List<String>>)(ctx.get(MessageContext.HTTP_RESPONSE_HEADERS)));
         record.setMessageType(MessageType.OUTBOUND);
         record.setOperation((QName)ctx.get(MessageContext.WSDL_OPERATION));
         boolean processEnvelope = false;
         for (Iterator<RecordProcessor> it = endpoint.getRecordProcessors().iterator(); it.hasNext() && !processEnvelope; )
         {
            processEnvelope = it.next().isProcessEnvelope();
         }
         if (processEnvelope) //skip message processing if not required since it's very time-consuming
         {
            try
            {
               SOAPEnvelope soapEnv = ctx.getMessage().getSOAPPart().getEnvelope();
               if (soapEnv != null)
               {
                  record.setEnvelope(DOMWriter.printNode(soapEnv, true));
               }
            }
            catch (SOAPException ex)
            {
               MONITORING_LOGGER.cannotTraceSoapMessage(ex);
            }
         }
         endpoint.processRecord(record);
      }
      return true;
   }

   public boolean handleFault(SOAPMessageContext ctx)
   {
      return handleOutbound(ctx);
   }

   /**
    * Returns true if there's at least a record processor in recording mode
    * 
    * @param endpoint
    * @return
    */
   private boolean isRecording(Endpoint endpoint)
   {
      List<RecordProcessor> processors = endpoint.getRecordProcessors();
      if (processors == null || processors.isEmpty())
      {
         return false;
      }
      for (RecordProcessor processor : processors)
      {
         if (processor.isRecording())
         {
            return true;
         }
      }
      return false;
   }
   
   
}
