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
package org.jboss.wsf.common.addressing;

import javax.xml.namespace.QName;

/**
 * TODO: see javax.xml.ws.addressing - merge it properly
 * Addressing constants.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class AddressingConstants
{
   /**
    * Constructor.
    */
   private AddressingConstants()
   {
      // forbidden inheritance
   }

   /**
    * <a href="http://www.w3.org/2005/08/addressing">WSA</a> constants.
    */
   public static final class Core
   {
      /**
       * Constructor.
       */
      private Core()
      {
         // forbidden inheritance
      }

      // WSA namespace
      public static final String NS = "http://www.w3.org/2005/08/addressing";

      // WSA prefix
      public static final String NS_PREFIX = "wsa";

      public static final class Elements
      {
         /**
          * Constructor.
          */
         private Elements()
         {
            // forbidden inheritance
         }

         // WSA 'EndpointReference' element
         public static final String ENDPOINTREFERENCE = "EndpointReference"; 
         public static final QName ENDPOINTREFERENCE_QNAME = new QName(NS, ENDPOINTREFERENCE, NS_PREFIX); 

         // WSA 'ReferenceParameters' element
         public static final String REFERENCEPARAMETERS = "ReferenceParameters"; 
         public static final QName REFERENCEPARAMETERS_QNAME = new QName(NS, REFERENCEPARAMETERS, NS_PREFIX); 

         // WSA 'Metadata' element
         public static final String METADATA = "Metadata"; 
         public static final QName METADATA_QNAME = new QName(NS, METADATA, NS_PREFIX); 

         // WSA 'Address' element
         public static final String ADDRESS = "Address"; 
         public static final QName ADDRESS_QNAME = new QName(NS, ADDRESS, NS_PREFIX); 

         // WSA 'MessageID' element
         public static final String MESSAGEID = "MessageID"; 
         public static final QName MESSAGEID_QNAME = new QName(NS, MESSAGEID, NS_PREFIX); 

         // WSA 'RelatesTo' element
         public static final String RELATESTO = "RelatesTo"; 
         public static final QName RELATESTO_QNAME = new QName(NS, RELATESTO, NS_PREFIX); 

         // WSA 'ReplyTo' element
         public static final String REPLYTO = "ReplyTo"; 
         public static final QName REPLYTO_QNAME = new QName(NS, REPLYTO, NS_PREFIX); 

         // WSA 'From' element
         public static final String FROM = "From"; 
         public static final QName FROM_QNAME = new QName(NS, FROM, NS_PREFIX); 

         // WSA 'FaultTo' element
         public static final String FAULTTO = "FaultTo"; 
         public static final QName FAULTTO_QNAME = new QName(NS, FAULTTO, NS_PREFIX); 

         // WSA 'To' element
         public static final String TO = "To"; 
         public static final QName TO_QNAME = new QName(NS, TO, NS_PREFIX); 

         // WSA 'Action' element
         public static final String ACTION = "Action"; 
         public static final QName ACTION_QNAME = new QName(NS, ACTION, NS_PREFIX); 

         // WSA 'RetryAfter' element
         public static final String RETRYAFTER = "RetryAfter"; 
         public static final QName RETRYAFTER_QNAME = new QName(NS, RETRYAFTER, NS_PREFIX); 

         // WSA 'ProblemHeaderQName' element
         public static final String PROBLEMHEADERQNAME = "ProblemHeaderQName"; 
         public static final QName PROBLEMHEADERQNAME_QNAME = new QName(NS, PROBLEMHEADERQNAME, NS_PREFIX); 

         // WSA 'ProblemIRI' element
         public static final String PROBLEMIRI = "ProblemIRI"; 
         public static final QName PROBLEMIRI_QNAME = new QName(NS, PROBLEMIRI, NS_PREFIX); 

         // WSA 'ProblemAction' element
         public static final String PROBLEMACTION = "ProblemAction"; 
         public static final QName PROBLEMACTION_QNAME = new QName(NS, PROBLEMACTION, NS_PREFIX); 

         // WSA 'SoapAction' element
         public static final String SOAPACTION = "SoapAction"; 
         public static final QName SOAPACTION_QNAME = new QName(NS, SOAPACTION, NS_PREFIX);
      }

      public static final class Attributes
      {
         /**
          * Constructor.
          */
         private Attributes()
         {
            // forbidden inheritance
         }

         // WSA 'RelationshipType' attribute
         public static final String RELATIONSHIPTYPE = "RelationshipType"; 
         public static final QName RELATIONSHIPTYPE_QNAME = new QName(NS, RELATIONSHIPTYPE, NS_PREFIX); 

         // WSA 'IsReferenceParameter' attribute
         public static final String ISREFERENCEPARAMETER = "IsReferenceParameter"; 
         public static final QName ISREFERENCEPARAMETER_QNAME = new QName(NS, ISREFERENCEPARAMETER, NS_PREFIX); 

      }

      public static final class Faults
      {
         /**
          * Constructor.
          */
         private Faults()
         {
            // forbidden inheritance
         }

         // WSA 'InvalidAddressingHeader' fault
         public static final QName INVALIDADDRESSINGHEADER_QNAME = new QName(NS, "InvalidAddressingHeader", NS_PREFIX); 

         // WSA 'InvalidAddress' fault
         public static final QName INVALIDADDRESS_QNAME = new QName(NS, "InvalidAddress", NS_PREFIX); 

         // WSA 'InvalidEPR' fault
         public static final QName INVALIDEPR_QNAME = new QName(NS, "InvalidEPR", NS_PREFIX); 

         // WSA 'InvalidCardinality' fault
         public static final QName INVALIDCARDINALITY_QNAME = new QName(NS, "InvalidCardinality", NS_PREFIX); 

         // WSA 'MissingAddressInEPR' fault
         public static final QName MISSINGADDRESSINEPR_QNAME = new QName(NS, "MissingAddressInEPR", NS_PREFIX); 

         // WSA 'DuplicateMessageID' fault
         public static final QName DUPLICATEMESSAGEID_QNAME = new QName(NS, "DuplicateMessageID", NS_PREFIX); 

         // WSA 'ActionMismatch' fault
         public static final QName ACTIONMISMATCH_QNAME = new QName(NS, "ActionMismatch", NS_PREFIX); 

         // WSA 'MessageAddressingHeaderRequired' fault
         public static final QName MESSAGEADDRESSINGHEADERREQUIRED_QNAME = new QName(NS, "MessageAddressingHeaderRequired", NS_PREFIX); 

         // WSA 'DestinationUnreachable' fault
         public static final QName DESTINATIONUNREACHABLE_QNAME = new QName(NS, "DestinationUnreachable", NS_PREFIX); 

         // WSA 'ActionNotSupported' fault
         public static final QName ACTIONNOTSUPPORTED_QNAME = new QName(NS, "ActionNotSupported", NS_PREFIX); 

         // WSA 'EndpointUnavailable' fault
         public static final QName ENDPOINTUNAVAILABLE_QNAME = new QName(NS, "EndpointUnavailable", NS_PREFIX);
      }
   }

   /**
    * <a href="http://www.w3.org/2007/05/addressing/metadata">WSAM</a> constants.
    */
   public static final class Metadata
   {
      /**
       * Constructor.
       */
      private Metadata()
      {
         // forbidden inheritance
      }

      // WSAM namespace
      public static final String NS = "http://www.w3.org/2007/05/addressing/metadata";

      // WSAM prefix
      public static final String NS_PREFIX = "wsam";

      public static final class Elements
      {
         /**
          * Constructor.
          */
         private Elements()
         {
            // forbidden inheritance
         }

         // WSAM 'ServiceName' element
         public static final String SERVICENAME = "ServiceName";
         public static final QName SERVICENAME_QNAME = new QName(NS, SERVICENAME, NS_PREFIX);

         // WSAM 'InterfaceName' element
         public static final String INTERFACENAME = "InterfaceName";
         public static final QName INTERFACENAME_QNAME = new QName(NS, INTERFACENAME, NS_PREFIX);

         // WSAM 'Addressing' element
         public static final String ADDRESSING = "Addressing"; 
         public static final QName ADDRESSING_QNAME = new QName(NS, ADDRESSING, NS_PREFIX); 

         // WSAM 'AnonymousResponses' element
         public static final String ANONYMOUSRESPONSES = "AnonymousResponses"; 
         public static final QName ANONYMOUSRESPONSES_QNAME = new QName(NS, ANONYMOUSRESPONSES, NS_PREFIX); 

         // WSAM 'NonAnonymousResponses' element
         public static final String NONANONYMOUSRESPONSES = "NonAnonymousResponses"; 
         public static final QName NONANONYMOUSRESPONSES_QNAME = new QName(NS, NONANONYMOUSRESPONSES, NS_PREFIX); 
      }

      public static final class Attributes
      {
         /**
          * Constructor.
          */
         private Attributes()
         {
            // forbidden inheritance
         }

         // WSAM 'EndpointName' attribute
         public static final String ENDPOINTNAME = "EndpointName";
         public static final QName ENDPOINTNAME_QNAME = new QName(NS, ENDPOINTNAME, NS_PREFIX);

         // WSAM 'Action' attribute
         public static final String ACTION = "Action";
         public static final QName ACTION_QNAME = new QName(NS, ACTION, NS_PREFIX);

      }
   }

}
