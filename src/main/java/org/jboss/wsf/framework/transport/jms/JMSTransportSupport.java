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
package org.jboss.wsf.framework.transport.jms;

// $Id:JMSTransportSupport.java 915 2006-09-08 08:40:45Z thomas.diesler@jboss.com $

import org.jboss.logging.Logger;
import org.jboss.util.NestedRuntimeException;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.*;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.soap.SOAPException;
import java.io.*;
import java.lang.IllegalStateException;
import java.rmi.RemoteException;

/**
 * The abstract base class for MDBs that want to act as web service endpoints.
 * A subclass should only need to implement the service endpoint interface.
 *
 * @author Thomas.Diesler@jboss.org
 */
public abstract class JMSTransportSupport implements MessageDrivenBean, MessageListener
{
   // logging support
   protected Logger log = Logger.getLogger(JMSTransportSupport.class);

   //private MessageDrivenContext mdbCtx;
   private QueueConnectionFactory queueFactory;

   /**
    * All messages come in here, if it is a BytesMessage we pass it on for further processing.
    */
   public void onMessage(Message message)
   {
      try
      {
         String msgStr = null;
         if (message instanceof BytesMessage)
         {
            msgStr = getMessageStr((BytesMessage)message);
         }
         else if (message instanceof TextMessage)
         {
            msgStr = ((TextMessage)message).getText();
         }
         else
         {
            log.warn("Invalid message type: " + message);
            return;
         }

         log.debug("Incomming SOAP message: " + msgStr);

         String fromName = null;
         Destination destination = message.getJMSDestination();
         if (destination instanceof Queue)
            fromName = "queue/" + ((Queue)destination).getQueueName();
         if (destination instanceof Topic)
            fromName = "topic/" + ((Topic)destination).getTopicName();

         InputStream inputStream = new ByteArrayInputStream(msgStr.getBytes());
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
         processSOAPMessage(fromName, inputStream, outputStream);

         msgStr = new String(outputStream.toByteArray());
         log.debug("Outgoing SOAP message: " + msgStr);

         if (msgStr.length() > 0)
         {
            Queue replyQueue = getReplyQueue(message);
            if (replyQueue != null)
            {
               sendResponse(replyQueue, msgStr);
            }
            else
            {
               log.warn("No reply queue, ignore response message");
            }
         }
         else
         {
            log.debug("SOAP response message is null");
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }

   protected void processSOAPMessage(String fromName, InputStream inputStream, OutputStream outStream) throws SOAPException, IOException, RemoteException
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      EndpointRegistry epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();

      Endpoint endpoint = getEndpointForDestination(epRegistry, fromName);

      if (endpoint == null)
         throw new IllegalStateException("Cannot find endpoint for: " + fromName);

      log.debug("dipatchMessage: " + endpoint.getName());

      RequestHandler reqHandler = endpoint.getRequestHandler();

      try
      {
         InvocationContext invContext = new InvocationContext();
         invContext.setTargetBean(this);

         reqHandler.handleRequest(endpoint, inputStream, outStream, invContext);
      }
      catch (Exception ex)
      {
         throw new RemoteException("Cannot process SOAP request", ex);
      }
   }

   // The destination jndiName is encoded in the service object name under key 'jms'
   private Endpoint getEndpointForDestination(EndpointRegistry epRegistry, String fromName)
   {
      Endpoint endpoint = null;
      for (ObjectName oname : epRegistry.getEndpoints())
      {
         Endpoint aux = epRegistry.getEndpoint(oname);
         String jmsProp = aux.getName().getKeyProperty("jms");
         if (jmsProp != null && jmsProp.equals(fromName))
         {
            endpoint = aux;
            break;
         }
      }
      return endpoint;
   }

   private String getMessageStr(BytesMessage message) throws Exception
   {
      byte[] buffer = new byte[8 * 1024];
      ByteArrayOutputStream out = new ByteArrayOutputStream(buffer.length);
      int read = message.readBytes(buffer);
      while (read != -1)
      {
         out.write(buffer, 0, read);
         read = message.readBytes(buffer);
      }

      byte[] msgBytes = out.toByteArray();
      return new String(msgBytes);
   }

   /**
    * Get the reply queue.
    */
   protected Queue getReplyQueue(Message message) throws JMSException
   {
      Queue replyQueue = (Queue)message.getJMSReplyTo();
      return replyQueue;
   }

   /**
    * Respond to the call by sending a message to the reply queue
    */
   protected void sendResponse(Queue replyQueue, String msgStr) throws SOAPException, IOException, JMSException
   {
      QueueConnection qc = queueFactory.createQueueConnection();
      QueueSession session = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueSender sender = null;
      try
      {
         sender = session.createSender(replyQueue);
         TextMessage responseMessage = session.createTextMessage(msgStr);
         sender.send(responseMessage);
         log.info("Sent response");
      }
      finally
      {
         try
         {
            sender.close();
         }
         catch (JMSException ignored)
         {
         }
         try
         {
            session.close();
         }
         catch (JMSException ignored)
         {
         }
         try
         {
            qc.close();
         }
         catch (JMSException ignored)
         {
         }
      }
   }

   // MDB lifecycle methods ********************************************************************************************

   public void ejbCreate()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         queueFactory = (QueueConnectionFactory)ctx.lookup("java:/ConnectionFactory");
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException(e);
      }
   }

   /**
    * A container invokes this method before it ends the life of the message-driven object.
    */
   public void ejbRemove() throws EJBException
   {
   }

   /**
    * Set the associated message-driven context.
    */
   public void setMessageDrivenContext(MessageDrivenContext ctx) throws EJBException
   {
      //this.mdbCtx = ctx;
   }
}
