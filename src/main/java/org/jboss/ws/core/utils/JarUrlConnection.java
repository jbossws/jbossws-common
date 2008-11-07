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
package org.jboss.ws.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/** <code>URLConnection</code> capable of handling multiply-nested jars.
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class JarUrlConnection extends JarURLConnection
{
   // ----------------------------------------------------------------------
   //     Instance members
   // ----------------------------------------------------------------------

   /** Base resource. */
   private URL baseResource;

   /** Additional nested segments. */
   private String[] segments;

   /** Terminal input-stream. */
   private InputStream in;

   // ----------------------------------------------------------------------
   //     Constructors
   // ----------------------------------------------------------------------

   /** Construct.
    *
    *  @param url Target URL of the connections.
    *
    *  @throws java.io.IOException If an error occurs while attempting to initialize
    *          the connection.
    */
   public JarUrlConnection(URL url) throws IOException
   {
      super(url = normaliseURL(url));

      String baseText = url.getPath();

      int bangLoc = baseText.indexOf("!");

      String baseResourceText = baseText.substring(0, bangLoc);

      String extraText = "";

      if (bangLoc <= (baseText.length() - 2) && baseText.charAt(bangLoc + 1) == '/')
      {
         if (bangLoc + 2 == baseText.length())
         {
            extraText = "";
         }
         else
         {
            extraText = baseText.substring(bangLoc + 1);
         }
      }
      else
      {
         throw new MalformedURLException("No !/ in url: " + url.toExternalForm());
      }

      List segments = new ArrayList();

      StringTokenizer tokens = new StringTokenizer(extraText, "!");

      while (tokens.hasMoreTokens())
      {
         segments.add(tokens.nextToken());
      }

      this.segments = (String[])segments.toArray(new String[segments.size()]);

      this.baseResource = new URL(baseResourceText);
   }

   protected static URL normaliseURL(URL url) throws MalformedURLException
   {
      String text = normalizeUrlPath(url.toString());

      if (!text.startsWith("jar:"))
      {
         text = "jar:" + text;
      }

      if (text.indexOf('!') < 0)
      {
         text = text + "!/";
      }

      return new URL(text);
   }

   // ----------------------------------------------------------------------
   //     Instance methods
   // ----------------------------------------------------------------------

   /** Retrieve the nesting path segments.
    *
    *  @return The segments.
    */
   protected String[] getSegments()
   {
      return this.segments;
   }

   /** Retrieve the base resource <code>URL</code>.
    *
    *  @return The base resource url.
    */
   protected URL getBaseResource()
   {
      return this.baseResource;
   }

   /** @see java.net.URLConnection
    */
   public void connect() throws IOException
   {
      if (this.segments.length == 0)
      {
         setupBaseResourceInputStream();
      }
      else
      {
         setupPathedInputStream();
      }
   }

   /** Setup the <code>InputStream</code> purely from the base resource.
    *
    *  @throws java.io.IOException If an I/O error occurs.
    */
   protected void setupBaseResourceInputStream() throws IOException
   {
      this.in = getBaseResource().openStream();
   }

   /** Setup the <code>InputStream</code> for URL with nested segments.
    *
    *  @throws java.io.IOException If an I/O error occurs.
    */
   protected void setupPathedInputStream() throws IOException
   {
      InputStream curIn = getBaseResource().openStream();

      for (int i = 0; i < this.segments.length; ++i)
      {
         curIn = getSegmentInputStream(curIn, segments[i]);
      }

      this.in = curIn;
   }

   /** Retrieve the <code>InputStream</code> for the nesting
    *  segment relative to a base <code>InputStream</code>.
    *
    *  @param baseIn The base input-stream.
    *  @param segment The nesting segment path.
    *
    *  @return The input-stream to the segment.
    *
    *  @throws java.io.IOException If an I/O error occurs.
    */
   protected InputStream getSegmentInputStream(InputStream baseIn, String segment) throws IOException
   {
      JarInputStream jarIn = new JarInputStream(baseIn);
      JarEntry entry = null;

      while (jarIn.available() != 0)
      {
         entry = jarIn.getNextJarEntry();

         if (entry == null)
         {
            break;
         }

         if (("/" + entry.getName()).equals(segment))
         {
            return jarIn;
         }
      }

      throw new IOException("unable to locate segment: " + segment);
   }

   /** @see java.net.URLConnection
    */
   public InputStream getInputStream() throws IOException
   {
      if (this.in == null)
      {
         connect();
      }
      return this.in;
   }

   /**
    * @return JarFile
    * @throws java.io.IOException
    * @see java.net.JarURLConnection#getJarFile()
    */
   public JarFile getJarFile() throws IOException
   {
      String url = baseResource.toExternalForm();

      if (url.startsWith("file:/"))
      {
         url = url.substring(6);
      }

      return new JarFile(URLDecoder.decode(url, "UTF-8"));
   }

   private static String normalizeUrlPath(String name)
   {
      if (name.startsWith("/"))
      {
         name = name.substring(1);

         System.out.println("1 name = " + name);
      }

      // Looking for org/codehaus/werkflow/personality/basic/../common/core-idioms.xml
      //                                               |    i  |
      //                                               +-------+ remove
      //
      int i = name.indexOf("/..");

      // Can't be at the beginning because we have no root to refer to so
      // we start at 1.
      if (i > 0)
      {
         int j = name.lastIndexOf("/", i - 1);

         name = name.substring(0, j) + name.substring(i + 3);

         System.out.println("2 name = " + name);
      }

      return name;
   }
}
