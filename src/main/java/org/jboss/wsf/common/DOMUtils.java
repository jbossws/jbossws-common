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
package org.jboss.wsf.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.jboss.ws.Constants;
import org.jboss.ws.core.utils.JBossWSEntityResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOM2 utilites
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 */
public final class DOMUtils
{
   private static Logger log = Logger.getLogger(DOMUtils.class);

   private static final String DISABLE_DEFERRED_NODE_EXPANSION = "org.jboss.ws.disable_deferred_node_expansion";
   private static final String DEFER_NODE_EXPANSION_FEATURE = "http://apache.org/xml/features/dom/defer-node-expansion";
   
   private static String documentBuilderFactoryName;
   
   private static final boolean alwaysResolveFactoryName = Boolean.getBoolean(Constants.ALWAYS_RESOLVE_DOCUMENT_BUILDER_FACTORY);
   private static final boolean disableDeferedNodeExpansion = Boolean.getBoolean(DISABLE_DEFERRED_NODE_EXPANSION);
   
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
            }
            catch (ParserConfigurationException pce)
            {
               log.error(pce);
            }

            DocumentBuilder builder = factory.newDocumentBuilder();
            setEntityResolver(builder);
            return builder;
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable to create document builder", e);
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
               log.debug("Cannot load: " + JBossWSEntityResolver.class.getCanonicalName());
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

   /** Initialize the DocumentBuilder
    */
   public static DocumentBuilder getDocumentBuilder()
   {
      return builderThreadLocal.get();
   }

   /** Parse the given XML string and return the root Element
    */
   public static Element parse(String xmlString) throws IOException
   {
      try
      {
         return parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
      }
      catch (IOException e)
      {
         log.error("Cannot parse: " + xmlString);
         throw e;
      }
   }

   /** Parse the given XML stream and return the root Element
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

   /** Parse the given input source and return the root Element
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

   /** Create an Element for a given name
    */
   public static Element createElement(String localPart)
   {
      Document doc = getOwnerDocument();
      if (log.isTraceEnabled()) log.trace("createElement {}" + localPart);
      return doc.createElement(localPart);
   }

   /** Create an Element for a given name and prefix
    */
   public static Element createElement(String localPart, String prefix)
   {
      Document doc = getOwnerDocument();
      if (log.isTraceEnabled()) log.trace("createElement {}" + prefix + ":" + localPart);
      return doc.createElement(prefix + ":" + localPart);
   }

   /** Create an Element for a given name, prefix and uri
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

   /** Create an Element for a given QName
    */
   public static Element createElement(QName qname)
   {
      return createElement(qname.getLocalPart(), qname.getPrefix(), qname.getNamespaceURI());
   }

   /** Create a org.w3c.dom.Text node
    */
   public static Text createTextNode(String value)
   {
      Document doc = getOwnerDocument();
      return doc.createTextNode(value);
   }

   /** Get the qname of the given node.
    */
   public static QName getElementQName(Element el)
   {
      String qualifiedName = el.getNodeName();
      return resolveQName(el, qualifiedName);
   }

   /** Transform the given qualified name into a QName
    */
   public static QName resolveQName(Element el, String qualifiedName)
   {
      QName qname;
      String prefix = "";
      String namespaceURI = "";
      String localPart = qualifiedName;

      int colIndex = qualifiedName.indexOf(":");
      if (colIndex > 0)
      {
         prefix = qualifiedName.substring(0, colIndex);
         localPart = qualifiedName.substring(colIndex + 1);

         if ("xmlns".equals(prefix))
         {
            namespaceURI = "URI:XML_PREDEFINED_NAMESPACE";
         }
         else
         {
            Element nsElement = el;
            while (namespaceURI.equals("") && nsElement != null)
            {
               namespaceURI = nsElement.getAttribute("xmlns:" + prefix);
               if (namespaceURI.equals(""))
                  nsElement = getParentElement(nsElement);
            }
         }
         
         if (namespaceURI.equals("") && el.getNamespaceURI() != null)
         {
            namespaceURI = el.getNamespaceURI();
         }

         if (namespaceURI.equals(""))
            throw new IllegalArgumentException("Cannot find namespace uri for: " + qualifiedName);
      }
      else
      {
         Element nsElement = el;
         while (namespaceURI.equals("") && nsElement != null)
         {
            namespaceURI = nsElement.getAttribute("xmlns");
            if (namespaceURI.equals(""))
               nsElement = getParentElement(nsElement);
         }
      }

      qname = new QName(namespaceURI, localPart, prefix);
      return qname;
   }

   /** Get the value from the given attribute
    *
    * @return null if the attribute value is empty or the attribute is not present
    */
   public static String getAttributeValue(Element el, String attrName)
   {
      return getAttributeValue(el, new QName(attrName));
   }

   /** Get the value from the given attribute
    *
    * @return null if the attribute value is empty or the attribute is not present
    */
   public static String getAttributeValue(Element el, QName attrName)
   {
      String attr = null;
      if ("".equals(attrName.getNamespaceURI()))
         attr = el.getAttribute(attrName.getLocalPart());
      else
         attr = el.getAttributeNS(attrName.getNamespaceURI(), attrName.getLocalPart());

      if ("".equals(attr))
         attr = null;

      return attr;
   }

   /** Get the qname value from the given attribute
    */
   public static QName getAttributeValueAsQName(Element el, String attrName)
   {
      return getAttributeValueAsQName(el, new QName(attrName));

   }

   /** Get the qname value from the given attribute
    */
   public static QName getAttributeValueAsQName(Element el, QName attrName)
   {
      QName qname = null;

      String qualifiedName = getAttributeValue(el, attrName);
      if (qualifiedName != null)
      {
         qname = resolveQName(el, qualifiedName);
      }

      return qname;
   }

   /** Get the boolean value from the given attribute
    */
   public static boolean getAttributeValueAsBoolean(Element el, String attrName)
   {
      return getAttributeValueAsBoolean(el, new QName(attrName));
   }

   /** Get the boolean value from the given attribute
    */
   public static boolean getAttributeValueAsBoolean(Element el, QName attrName)
   {
      String attrVal = getAttributeValue(el, attrName);
      boolean ret = "true".equalsIgnoreCase(attrVal) || "1".equalsIgnoreCase(attrVal);
      return ret;
   }

   /** Get the integer value from the given attribute
    */
   public static Integer getAttributeValueAsInteger(Element el, String attrName)
   {
      return getAttributeValueAsInteger(el, new QName(attrName));
   }

   /** Get the integer value from the given attribute
    */
   public static Integer getAttributeValueAsInteger(Element el, QName attrName)
   {
      String attrVal = getAttributeValue(el, attrName);
      return (attrVal != null ? new Integer(attrVal) : null);
   }

   /** Get the attributes as Map<QName, String>
    */
   public static Map<QName, String> getAttributes(Element el)
   {
      Map<QName, String> attmap = new HashMap<QName, String>();
      NamedNodeMap attribs = el.getAttributes();
      int len = attribs.getLength();
      for (int i = 0; i < len; i++)
      {
         Attr attr = (Attr)attribs.item(i);
         String name = attr.getName();
         QName qname = resolveQName(el, name);
         String value = attr.getNodeValue();
         attmap.put(qname, value);
      }
      return attmap;
   }

   /** Copy attributes between elements
    */
   public static void copyAttributes(Element destElement, Element srcElement)
   {
      NamedNodeMap attribs = srcElement.getAttributes();
      int len = attribs.getLength();
      for (int i = 0; i < len; i++)
      {
         Attr attr = (Attr)attribs.item(i);
         String uri = attr.getNamespaceURI();
         String qname = attr.getName();
         String value = attr.getNodeValue();

         // Prevent DOMException: NAMESPACE_ERR: An attempt is made to create or
         // change an object in a way which is incorrect with regard to namespaces.
         if (uri == null && qname.startsWith("xmlns"))
         {
            if (log.isTraceEnabled()) log.trace("Ignore attribute: [uri=" + uri + ",qname=" + qname + ",value=" + value + "]");
         }
         else
         {
            destElement.setAttributeNS(uri, qname, value);
         }
      }
   }
   
   /** True if the node has text child elements only
    */
   public static boolean hasTextChildNodesOnly(Node node)
   {
      NodeList nodeList = node.getChildNodes();
      int len = nodeList.getLength();
      if (len == 0)
         return false;

      for (int i = 0; i < len; i++)
      {
         Node acksToChildNode = nodeList.item(i);
         if (acksToChildNode.getNodeType() != Node.TEXT_NODE)
            return false;
      }

      return true;
   }

   /** True if the node has child elements
    */
   public static boolean hasChildElements(Node node)
   {
      NodeList nlist = node.getChildNodes();
      int len = nlist.getLength();
      for (int i = 0; i < len; i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
            return true;
      }
      return false;
   }

   /** Gets child elements
    */
   public static Iterator<Element> getChildElements(Node node)
   {
      List<Element> list = new LinkedList<Element>();
      NodeList nlist = node.getChildNodes();
      int len = nlist.getLength();
      for (int i = 0; i < len; i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
            list.add((Element)child);
      }
      return list.iterator();
   }

   /** Get the concatenated text content, or null.
    */
   public static String getTextContent(Node node)
   {
      boolean hasTextContent = false;
      StringBuilder buffer = new StringBuilder();
      NodeList nlist = node.getChildNodes();
      int len = nlist.getLength();
      for (int i = 0; i < len; i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.TEXT_NODE)
         {
            buffer.append(child.getNodeValue());
            hasTextContent = true;
         }
      }
      return (hasTextContent ? buffer.toString() : null);
   }

   /** Gets the first child element
    */
   public static Element getFirstChildElement(Node node)
   {
      return getFirstChildElement(node, false);
   }

   /** Gets the first child element
    */
   public static Element getFirstChildElement(Node node, boolean recursive)
   {
      return getFirstChildElementIntern(node, null, recursive);
   }

   /** Gets the first child element for a given local name without namespace
    */
   public static Element getFirstChildElement(Node node, String nodeName)
   {
      return getFirstChildElement(node, nodeName, false);
   }

   /** Gets the first child element for a given local name without namespace
    */
   public static Element getFirstChildElement(Node node, String nodeName, boolean recursive)
   {
      return getFirstChildElementIntern(node, new QName(nodeName), recursive);
   }

   /** Gets the first child element for a given qname
    */
   public static Element getFirstChildElement(Node node, QName nodeName)
   {
      return getFirstChildElement(node, nodeName, false);
   }

   /** Gets the first child element for a given qname
    */
   public static Element getFirstChildElement(Node node, QName nodeName, boolean recursive)
   {
      return getFirstChildElementIntern(node, nodeName, recursive);
   }

   private static Element getFirstChildElementIntern(Node node, QName nodeName, boolean recursive)
   {
      Element childElement = null;
      Iterator<Element> it = getChildElementsIntern(node, nodeName, recursive);
      if (it.hasNext())
      {
         childElement = (Element)it.next();
      }
      return childElement;
   }

   /** Gets the child elements for a given local name without namespace
    */
   public static Iterator<Element> getChildElements(Node node, String nodeName)
   {
      return getChildElements(node, nodeName, false);
   }

   /** Gets the child elements for a given local name without namespace
    */
   public static Iterator<Element> getChildElements(Node node, String nodeName, boolean recursive)
   {
      return getChildElementsIntern(node, new QName(nodeName), recursive);
   }

   /** Gets the child element for a given qname
    */
   public static Iterator<Element> getChildElements(Node node, QName nodeName)
   {
      return getChildElements(node, nodeName, false);
   }

   /** Gets the child element for a given qname
    */
   public static Iterator<Element> getChildElements(Node node, QName nodeName, boolean recursive)
   {
      return getChildElementsIntern(node, nodeName, recursive);
   }

   public static List<Element> getChildElementsAsList(Node node, String nodeName)
   {
      return getChildElementsAsList(node, nodeName, false);
   }

   public static List<Element> getChildElementsAsList(Node node, String nodeName, boolean recursive)
   {
      return getChildElementsAsListIntern(node, new QName(nodeName), recursive);
   }

   public static List<Element> getChildElementsAsList(Node node, QName nodeName)
   {
      return getChildElementsAsList(node, nodeName, false);
   }

   public static List<Element> getChildElementsAsList(Node node, QName nodeName, boolean recursive)
   {
      return getChildElementsAsListIntern(node, nodeName, recursive);
   }

   private static List<Element> getChildElementsAsListIntern(Node node, QName nodeName, boolean recursive)
   {
      List<Element> list = new LinkedList<Element>();

      NodeList nlist = node.getChildNodes();
      int len = nlist.getLength();
      for (int i = 0; i < len; i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            search(list, (Element)child, nodeName, recursive);
         }
      }
      return list;
   }

   private static void search(List<Element> list, Element baseElement, QName nodeName, boolean recursive)
   {
      if (nodeName == null)
      {
         list.add(baseElement);
      }
      else
      {
         QName qname;
         if (nodeName.getNamespaceURI().length() > 0)
         {
            qname = new QName(baseElement.getNamespaceURI(), baseElement.getLocalName());
         }
         else
         {
            qname = new QName(baseElement.getLocalName());
         }
         if (qname.equals(nodeName))
         {
            list.add(baseElement);
         }
      }
      if (recursive)
      {
         NodeList nlist = baseElement.getChildNodes();
         int len = nlist.getLength();
         for (int i = 0; i < len; i++)
         {
            Node child = nlist.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
               search(list, (Element)child, nodeName, recursive);
            }
         }
      }
   }

   private static Iterator<Element> getChildElementsIntern(Node node, QName nodeName, boolean recursive)
   {
      return getChildElementsAsListIntern(node, nodeName, recursive).iterator();
   }

   /** Gets parent element or null if there is none
    */
   public static Element getParentElement(Node node)
   {
      Node parent = node.getParentNode();
      return (parent instanceof Element ? (Element)parent : null);
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
         throw new RuntimeException("Source type not implemented: " + source.getClass().getName());
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
