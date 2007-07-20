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
package org.jboss.wsf.common;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 */
public final class DOMUtils
{
   private static Logger log = Logger.getLogger(DOMUtils.class);

   // All elements created by the same thread are created by the same builder and belong to the same doc
   private static ThreadLocal documentThreadLocal = new ThreadLocal();
   private static ThreadLocal builderThreadLocal = new ThreadLocal()
   {
      protected Object initialValue()
      {
         try
         {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            setEntityResolver(builder);
            return builder;
         }
         catch (ParserConfigurationException e)
         {
            throw new RuntimeException("Failed to create DocumentBuilder", e);
         }
      }

      private void setEntityResolver(DocumentBuilder builder)
      {
         String[] resolvers = new String[] { "org.jboss.ws.core.utils.JBossWSEntityResolver", "org.jboss.util.xml.JBossEntityResolver" };

         EntityResolver entityResolver = null;
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         for (String resolver : resolvers)
         {
            try
            {
               Class<?> resolverClass = loader.loadClass(resolver);
               entityResolver = (EntityResolver)resolverClass.newInstance();
            }
            catch (Exception ex)
            {
               log.debug("Cannot load: " + resolver);
            }
         }

         if (entityResolver != null)
            builder.setEntityResolver(entityResolver);
      }
   };

   // Hide the constructor
   private DOMUtils()
   {
   }

   /** Initialise the the DocumentBuilder
    */
   public static DocumentBuilder getDocumentBuilder()
   {
      DocumentBuilder builder = (DocumentBuilder)builderThreadLocal.get();
      return builder;
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
         Document doc = getDocumentBuilder().parse(xmlStream);
         Element root = doc.getDocumentElement();
         return root;
      }
      catch (SAXException e)
      {
         throw new IOException(e.toString());
      }
   }

   /** Parse the given input source and return the root Element
    */
   public static Element parse(InputSource source) throws IOException
   {
      try
      {
         Document doc = getDocumentBuilder().parse(source);
         Element root = doc.getDocumentElement();
         return root;
      }
      catch (SAXException e)
      {
         throw new IOException(e.toString());
      }
   }

   /** Create an Element for a given name
    */
   public static Element createElement(String localPart)
   {
      Document doc = getOwnerDocument();
      log.trace("createElement {}" + localPart);
      return doc.createElement(localPart);
   }

   /** Create an Element for a given name and prefix
    */
   public static Element createElement(String localPart, String prefix)
   {
      Document doc = getOwnerDocument();
      log.trace("createElement {}" + prefix + ":" + localPart);
      return doc.createElement(prefix + ":" + localPart);
   }

   /** Create an Element for a given name, prefix and uri
    */
   public static Element createElement(String localPart, String prefix, String uri)
   {
      Document doc = getOwnerDocument();
      if (prefix == null || prefix.length() == 0)
      {
         log.trace("createElement {" + uri + "}" + localPart);
         return doc.createElementNS(uri, localPart);
      }
      else
      {
         log.trace("createElement {" + uri + "}" + prefix + ":" + localPart);
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
      else attr = el.getAttributeNS(attrName.getNamespaceURI(), attrName.getLocalPart());

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
   public static Map getAttributes(Element el)
   {
      Map attmap = new HashMap();
      NamedNodeMap attribs = el.getAttributes();
      for (int i = 0; i < attribs.getLength(); i++)
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
      for (int i = 0; i < attribs.getLength(); i++)
      {
         Attr attr = (Attr)attribs.item(i);
         String uri = attr.getNamespaceURI();
         String qname = attr.getName();
         String value = attr.getNodeValue();

         // Prevent DOMException: NAMESPACE_ERR: An attempt is made to create or
         // change an object in a way which is incorrect with regard to namespaces.
         if (uri == null && qname.startsWith("xmlns"))
         {
            log.trace("Ignore attribute: [uri=" + uri + ",qname=" + qname + ",value=" + value + "]");
         }
         else
         {
            destElement.setAttributeNS(uri, qname, value);
         }
      }
   }

   /** True if the node has child elements
    */
   public static boolean hasChildElements(Node node)
   {
      NodeList nlist = node.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
            return true;
      }
      return false;
   }

   /** Gets child elements
    */
   public static Iterator getChildElements(Node node)
   {
      ArrayList list = new ArrayList();
      NodeList nlist = node.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
            list.add(child);
      }
      return list.iterator();
   }

   /** Get the concatenated text content, or null.
    */
   public static String getTextContent(Node node)
   {
      boolean hasTextContent = false;
      StringBuffer buffer = new StringBuffer();
      NodeList nlist = node.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
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
      return getFirstChildElementIntern(node, null);
   }

   /** Gets the first child element for a given local name without namespace
    */
   public static Element getFirstChildElement(Node node, String nodeName)
   {
      return getFirstChildElementIntern(node, new QName(nodeName));
   }

   /** Gets the first child element for a given qname
    */
   public static Element getFirstChildElement(Node node, QName nodeName)
   {
      return getFirstChildElementIntern(node, nodeName);
   }

   private static Element getFirstChildElementIntern(Node node, QName nodeName)
   {
      Element childElement = null;
      Iterator it = getChildElementsIntern(node, nodeName);
      if (it.hasNext())
      {
         childElement = (Element)it.next();
      }
      return childElement;
   }

   /** Gets the child elements for a given local name without namespace
    */
   public static Iterator getChildElements(Node node, String nodeName)
   {
      return getChildElementsIntern(node, new QName(nodeName));
   }

   /** Gets the child element for a given qname
    */
   public static Iterator getChildElements(Node node, QName nodeName)
   {
      return getChildElementsIntern(node, nodeName);
   }

   private static Iterator getChildElementsIntern(Node node, QName nodeName)
   {
      ArrayList list = new ArrayList();
      NodeList nlist = node.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Node child = nlist.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            if (nodeName == null)
            {
               list.add(child);
            }
            else
            {
               QName qname;
               if (nodeName.getNamespaceURI().length() > 0)
               {
                  qname = new QName(child.getNamespaceURI(), child.getLocalName());
               }
               else
               {
                  qname = new QName(child.getLocalName());
               }
               if (qname.equals(nodeName))
               {
                  list.add(child);
               }
            }
         }
      }
      return list.iterator();
   }

   /** Gets parent element or null if there is none
    */
   public static Element getParentElement(Node node)
   {
      Node parent = node.getParentNode();
      return (parent instanceof Element ? (Element)parent : null);
   }

   /** Get the owner document that is associated with the current thread */
   public static Document getOwnerDocument()
   {
      Document doc = (Document)documentThreadLocal.get();
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

      try
      {
         if (source instanceof StreamSource)
         {
            StreamSource streamSource = (StreamSource)source;

            InputStream ins = streamSource.getInputStream();
            if (ins != null)
            {
               retElement = DOMUtils.parse(ins);
            }
            else
            {
               Reader reader = streamSource.getReader();
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
            else
            {
               throw new RuntimeException("Unsupported Node type: " + node.getClass().getName());
            }
         }
         else if (source instanceof SAXSource)
         {
            // The fact that JAXBSource derives from SAXSource is an implementation detail.
            // Thus in general applications are strongly discouraged from accessing methods defined on SAXSource.
            // The XMLReader object obtained by the getXMLReader method shall be used only for parsing the InputSource object returned by the getInputSource method.

            TransformerFactory tf = TransformerFactory.newInstance();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(source, new StreamResult(baos));
            retElement = DOMUtils.parse(new ByteArrayInputStream(baos.toByteArray()));
         }
         else
         {
            throw new RuntimeException("Source type not implemented: " + source.getClass().getName());
         }

      }
      catch (TransformerException ex)
      {
         IOException ioex = new IOException();
         ioex.initCause(ex);
         throw ioex;
      }

      return retElement;
   }
}
