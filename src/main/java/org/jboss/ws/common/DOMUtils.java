/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.utils.JBossWSEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOM2 utilities: this extends the {@link org.jboss.wsf.util.DOMUtils} adding parse and creation methods.
 * These leverage static thread-local instances of {@link org.w3c.dom.Document} and {@link javax.xml.parsers.DocumentBuilder}.
 * The ThreadLocal attributes can be reset using the clearThreadLocals() method.
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 */
public final class DOMUtils extends org.jboss.ws.api.util.DOMUtils
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(DOMUtils.class);
   private static Logger log = Logger.getLogger(DOMUtils.class);

   private static final String DISABLE_DEFERRED_NODE_EXPANSION = "org.jboss.ws.disable_deferred_node_expansion";
   private static final String DEFER_NODE_EXPANSION_FEATURE = "http://apache.org/xml/features/dom/defer-node-expansion";
   private static final String ENABLE_DOCTYPE_DECL = "org.jboss.ws.enable_doctype_decl";
   private static final String DISALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
   
   private static String documentBuilderFactoryName;
   
   private static final boolean alwaysResolveFactoryName = Boolean.getBoolean(Constants.ALWAYS_RESOLVE_DOCUMENT_BUILDER_FACTORY);
   private static final boolean disableDeferedNodeExpansion = Boolean.getBoolean(DISABLE_DEFERRED_NODE_EXPANSION);
   private static final boolean enableDoctypeDeclaration = Boolean.getBoolean(ENABLE_DOCTYPE_DECL);
   
   // All elements created by the same thread are created by the same builder and belong to the same doc
   private static ThreadLocal<Document> documentThreadLocal = new ThreadLocal<Document>();
   private static ThreadLocal<DocumentBuilder> builderThreadLocal = new ThreadLocal<DocumentBuilder>() {
      protected DocumentBuilder initialValue()
      {
         DocumentBuilderFactory factory = null;
         try
         {
            //slow
            //factory = DocumentBuilderFactory.newInstance();
            
            //fast (requires JDK6 or greater)
            if (documentBuilderFactoryName == null || alwaysResolveFactoryName)
            {
               factory = DocumentBuilderFactory.newInstance();
               if (!alwaysResolveFactoryName)
               {
                  documentBuilderFactoryName = factory.getClass().getCanonicalName();
               }
            }
            else
            {
               factory = DocumentBuilderFactory.newInstance(documentBuilderFactoryName, SecurityActions.getContextClassLoader());
            }
            
            
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);

            try
            {
               factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
               if (disableDeferedNodeExpansion)
               {
                  factory.setFeature(DEFER_NODE_EXPANSION_FEATURE, false);
               }
               if (!enableDoctypeDeclaration)
               {
                  factory.setFeature(DISALLOW_DOCTYPE_DECL_FEATURE, true);
               }
            }
            catch (ParserConfigurationException pce)
            {
               log.fatal("Serious security risk, not able to configure parser feature", pce);
            }

            DocumentBuilder builder = factory.newDocumentBuilder();
            setEntityResolver(builder);
            return builder;
         }
         catch (Exception e)
         {
            throw new RuntimeException(BundleUtils.getMessage(bundle, "UNABLE_TO_CREATE_DOCUMENT_BUILDER"),  e);
         }
      }
      
      @SuppressWarnings("deprecation")
      private void setEntityResolver(DocumentBuilder builder)
      {
         EntityResolver entityResolver = null;
         try
         {
            entityResolver = new JBossWSEntityResolver();
         }
         catch (Throwable t)
         {
            boolean debugEnabled = log.isDebugEnabled();
            if (debugEnabled)
               log.debug("Cannot load JBossWSEntityResolver");
            String[] resolvers = new String[] { "org.jboss.util.xml.JBossEntityResolver" };
            ClassLoader loader = SecurityActions.getContextClassLoader();
            for (String resolver : resolvers)
            {
               try
               {
                  Class<?> resolverClass = SecurityActions.loadClass(loader, resolver);
                  entityResolver = (EntityResolver)resolverClass.newInstance();
                  break;
               }
               catch (Exception ex)
               {
                  if (debugEnabled)
                     log.debug("Cannot load: " + resolver);
               }
            }
         }
         if (entityResolver != null)
            builder.setEntityResolver(entityResolver);
      }
   };
   
   public static void clearThreadLocals()
   {
      documentThreadLocal.remove();
      builderThreadLocal.remove();
   }

   // Hide the constructor
   private DOMUtils()
   {
   }

   /**
    * Initialize the DocumentBuilder, set the current thread association and returns it
    */
   public static DocumentBuilder getDocumentBuilder()
   {
      return builderThreadLocal.get();
   }

   /**
    * Parse the given XML string and return the root Element
    * This uses the document builder associated with the current thread.
    */
   public static Element parse(String xmlString) throws IOException
   {
      try
      {
         return parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
      }
      catch (IOException e)
      {
         log.error(BundleUtils.getMessage(bundle, "CANNOT_PARSE",  xmlString));
         throw e;
      }
   }

   /**
    * Parse the given XML stream and return the root Element
    * This uses the document builder associated with the current thread.
    */
   public static Element parse(InputStream xmlStream) throws IOException
   {
      try
      {
         Document doc;
         DocumentBuilder builder = getDocumentBuilder();
         synchronized (builder) //synchronize to prevent concurrent parsing on the same DocumentBuilder
         {
            doc = builder.parse(xmlStream);
         }
         return doc.getDocumentElement();
      }
      catch (SAXException se)
      {
         throw new IOException(se.toString());
      }
      finally
      {
         xmlStream.close();
      }
   }

   /**
    * Parse the given input source and return the root Element.
    * This uses the document builder associated with the current thread.
    */
   public static Element parse(InputSource source) throws IOException
   {
      try
      {
         Document doc;
         DocumentBuilder builder = getDocumentBuilder();
         synchronized (builder) //synchronize to prevent concurrent parsing on the same DocumentBuilder
         {
            doc = builder.parse(source);
         }
         return doc.getDocumentElement();
      }
      catch (SAXException se)
      {
         throw new IOException(se.toString());
      }
      finally
      {
         InputStream is = source.getByteStream();
         if (is != null)
         {
            is.close();
         }
         Reader r = source.getCharacterStream();
         if (r != null)
         {
            r.close();
         }
      }
   }

   /**
    * Create an Element for a given name.
    * This uses the document builder associated with the current thread.
    */
   public static Element createElement(String localPart)
   {
      Document doc = getOwnerDocument();
      if (log.isTraceEnabled()) log.trace("createElement {}" + localPart);
      return doc.createElement(localPart);
   }

   /**
    * Create an Element for a given name and prefix.
    * This uses the document builder associated with the current thread.
    */
   public static Element createElement(String localPart, String prefix)
   {
      Document doc = getOwnerDocument();
      if (log.isTraceEnabled()) log.trace("createElement {}" + prefix + ":" + localPart);
      return doc.createElement(prefix + ":" + localPart);
   }

   /**
    * Create an Element for a given name, prefix and uri.
    * This uses the document builder associated with the current thread.
    */
   public static Element createElement(String localPart, String prefix, String uri)
   {
      Document doc = getOwnerDocument();
      if (prefix == null || prefix.length() == 0)
      {
         if (log.isTraceEnabled()) log.trace("createElement {" + uri + "}" + localPart);
         return doc.createElementNS(uri, localPart);
      }
      else
      {
         if (log.isTraceEnabled()) log.trace("createElement {" + uri + "}" + prefix + ":" + localPart);
         return doc.createElementNS(uri, prefix + ":" + localPart);
      }
   }

   /**
    * Create an Element for a given QName.
    * This uses the document builder associated with the current thread.
    */
   public static Element createElement(QName qname)
   {
      return createElement(qname.getLocalPart(), qname.getPrefix(), qname.getNamespaceURI());
   }

   /**
    * Create a org.w3c.dom.Text node.
    * This uses the document builder associated with the current thread.
    */
   public static Text createTextNode(String value)
   {
      Document doc = getOwnerDocument();
      return doc.createTextNode(value);
   }

   /** Peek at the owner document without creating a new one if not set. */
   public static Document peekOwnerDocument()
   {
      return documentThreadLocal.get();
   }
   
   public static void setOwnerDocument(Document doc)
   {
      documentThreadLocal.set(doc);
   }
   
   /** Get the owner document that is associated with the current thread */
   public static Document getOwnerDocument()
   {
      Document doc = documentThreadLocal.get();
      if (doc == null)
      {
         doc = getDocumentBuilder().newDocument();
         documentThreadLocal.set(doc);
      }
      return doc;
   }

   /**
    * Parse the contents of the provided source into an element.
    * This uses the document builder associated with the current thread.
    * 
    * @param source
    * @return
    * @throws IOException
    */
   public static Element sourceToElement(Source source) throws IOException
   {
      Element retElement = null;

      if (source instanceof StreamSource)
      {
         StreamSource streamSource = (StreamSource)source;

         InputStream ins = streamSource.getInputStream();
         if (ins != null)
         {
            retElement = DOMUtils.parse(ins);
         }
         Reader reader = streamSource.getReader();
         if (reader != null)
         {
            retElement = DOMUtils.parse(new InputSource(reader));
         }
      }
      else if (source instanceof DOMSource)
      {
         DOMSource domSource = (DOMSource)source;
         Node node = domSource.getNode();
         if (node instanceof Element)
         {
            retElement = (Element)node;
         }
         else if (node instanceof Document)
         {
            retElement = ((Document)node).getDocumentElement();
         }
      }
      else if (source instanceof SAXSource)
      {
         // The fact that JAXBSource derives from SAXSource is an implementation detail.
         // Thus in general applications are strongly discouraged from accessing methods defined on SAXSource.
         // The XMLReader object obtained by the getXMLReader method shall be used only for parsing the InputSource object returned by the getInputSource method.

         final boolean hasInputSource = ((SAXSource) source).getInputSource() != null; 
         final boolean hasXMLReader = ((SAXSource) source).getXMLReader() != null;

         if (hasInputSource || hasXMLReader)
         {
            try
            {
               TransformerFactory tf = TransformerFactory.newInstance();
               ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
               Transformer transformer = tf.newTransformer();
               transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
               transformer.setOutputProperty(OutputKeys.METHOD, "xml");
               transformer.transform(source, new StreamResult(baos));
               retElement = DOMUtils.parse(new ByteArrayInputStream(baos.toByteArray()));
            }
            catch (TransformerException ex)
            {
               throw new IOException(ex);
            }
         }
      }
      else
      {
         throw new RuntimeException(BundleUtils.getMessage(bundle, "SOURCE_TYPE_NOT_IMPLEMENTED",  source.getClass()));
      }

      return retElement;
   }

   /**
    * Converts XML node in pretty mode using UTF-8 encoding to string.
    * 
    * @param node XML document or element
    * @return XML string
    * @throws Exception if some error occurs
    */
   public static String node2String(final Node node) throws UnsupportedEncodingException
   {
      return node2String(node, true, Constants.DEFAULT_XML_CHARSET);
   }
   
   /**
    * Converts XML node in specified pretty mode using UTF-8 encoding to string.
    * 
    * @param node XML document or element
    * @param prettyPrint whether XML have to be pretty formated
    * @return XML string
    * @throws Exception if some error occurs
    */
   public static String node2String(final Node node, boolean prettyPrint) throws UnsupportedEncodingException
   {
      return node2String(node, prettyPrint, Constants.DEFAULT_XML_CHARSET);
   }
   
   /**
    * Converts XML node in specified pretty mode and encoding to string.
    * 
    * @param node XML document or element
    * @param prettyPrint whether XML have to be pretty formated
    * @param encoding to use
    * @return XML string
    * @throws UnsupportedEncodingException 
    */
   public static String node2String(final Node node, boolean prettyPrint, String encoding) throws UnsupportedEncodingException 
   {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new DOMWriter(new PrintWriter(baos), encoding).setPrettyprint(prettyPrint).print(node);
      return baos.toString(encoding);
   }

}
