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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.wsf.common.IOUtils;
import org.jboss.wsf.common.JavaUtils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Generic mime utility class.
 *
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 */
public class MimeUtils
{

   private static Map<String, Class> mime2class = new HashMap<String, Class>();
   private static Map<Class, String> class2mime = new HashMap<Class, String>();

   static {
      mime2class.put("text/plain", java.lang.String.class);
      mime2class.put("image/jpeg", java.awt.Image.class);
      mime2class.put("text/xml", javax.xml.transform.Source.class);
      mime2class.put("application/xml", javax.xml.transform.Source.class);
      mime2class.put("application/octet-stream", javax.activation.DataHandler.class);

      class2mime.put(java.awt.Image.class, "image/jpeg");
      class2mime.put(javax.xml.transform.Source.class, "text/xml");
      class2mime.put(java.lang.String.class, "text/plain");
   }

   /**
    * Converts a MIME type into a proprietary JBossWS attachment xml type.
    *
    * @param mimeType the MIME type string to convert
    * @return the xml type that this mime type corresponds to
    */
   public static QName convertMimeTypeToXmlType(String mimeType)
   {
      StringBuilder mimeName = new StringBuilder(mimeType);

      int pos = mimeName.indexOf("/");

      if (pos == -1)
         return null;

      mimeName.setCharAt(pos, '_');

      return new QName(Constants.NS_ATTACHMENT_MIME_TYPE, mimeName.toString());
   }

   /**
    * Gets the base portion of a MIME type string. This basically just strips
    * off any type parameter elements.
    *
    * @param mimeType any MIME type string
    * @return a reduced MIME string containing no type parameters
    */
   public static String getBaseMimeType(String mimeType)
   {
      ContentType contentType;

      if (mimeType == null)
         return null;
      try
      {
         contentType = new ContentType(mimeType);
      }
      catch (ParseException e)
      {
         return null;
      }

      return contentType.getBaseType();
   }

   /**
    * Checks if there is a matching mime pattern for mimeType in mimeTypes. This
    * will return true if there is an exact match (for example text/plain =
    * text/plain), or if there is a wildcard subtype match (text/plain =
    * text/*).
    *
    * @param mimeType the mime type to search for
    * @param mimeTypes the set of mime types to search
    * @return true if there is a match, false if not
    */
   public static boolean isMemberOf(String mimeType, Set mimeTypes)
   {
      if (mimeTypes.contains(mimeType))
         return true;

      try
      {
         if (mimeTypes.contains(new ContentType(mimeType).getPrimaryType() + "/*"))
            return true;
      }
      catch (ParseException e)
      {
         // eat
      }

      return false;
   }

   /**
    * Resolve the class for a mype type.
    * Defaults to <code>DataHandler</code> if no mapping could be found.
    */
   public static Class resolveClass(String mimeType) {
      Class cl = mime2class.get(mimeType);
      if(null==cl)
         cl = javax.activation.DataHandler.class;
      return cl;
   }

   /**
    * Resolve the mime type for an object.
    * Default to <code>application/octet-stream</code>
    * if no mapping could be found.
    */
   public static String resolveMimeType(Object obj) {
      String mimeType = (obj instanceof MimeMultipart) ?
          ((MimeMultipart)obj).getContentType() :
          resolveMimeType(obj.getClass());
      return mimeType;
   }

   public static String resolveMimeType(Class clazz) {
      String mimeType = "application/octet-stream";
      for(Class cl : class2mime.keySet())
      {
         if(JavaUtils.isAssignableFrom(cl, clazz))
            mimeType = class2mime.get(cl);
      }
      return mimeType;
   }

   public static ByteArrayConverter getConverterForJavaType(Class targetClazz)
   {
      ByteArrayConverter converter = null;
      if(JavaUtils.isAssignableFrom(java.awt.Image.class, targetClazz))
         converter = new ImageConverter();
      else if (JavaUtils.isAssignableFrom(javax.xml.transform.Source.class, targetClazz))
         converter = new SourceConverter();
      else if (JavaUtils.isAssignableFrom(java.lang.String.class, targetClazz))
         converter = new StringConverter();
      else if (JavaUtils.isAssignableFrom(java.io.InputStream.class, targetClazz))
         converter = new StreamConverter();

      if(null == converter)
         throw new WSException("No ByteArrayConverter for class: " + targetClazz.getName());

      return converter;
   }

    public static ByteArrayConverter getConverterForContentType(String contentType)
   {
      ByteArrayConverter converter = null;

      if(contentType != null)
      {
         if("image/jpeg".equals(contentType) || "image/jpg".equals(contentType))
            converter = new ImageConverter();
         else if("text/xml".equals(contentType) || "application/xml".equals(contentType))
            converter = new SourceConverter();
         else if("text/plain".equals(contentType))
            converter = new StringConverter();
         else if("application/octet-stream".equals(contentType))
            converter = new StreamConverter();
      }

      if(null == converter)
          throw new WSException("No ByteArrayConverter for content type: " + contentType);

      return converter;
   }
   public static class ImageConverter implements ByteArrayConverter
   {
      public Object readFrom(InputStream in) {
         Object converted = null;
         try
         {
            JPEGImageDecoder dec = JPEGCodec.createJPEGDecoder(in);
            BufferedImage bim = dec.decodeAsBufferedImage();
            converted = bim;
         }
         catch (Exception e)
         {
            // ignore
         }

         return converted;
      }

      public void writeTo(Object obj, OutputStream out) {
         if(obj instanceof BufferedImage)
         {
            JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
            try
            {
               enc.encode((BufferedImage)obj);
            }
            catch (IOException e)
            {
               throw new WSException("Failed to convert " + obj.getClass());
            }
         }
         else
         {
            throw new WSException("Unable to convert " + obj.getClass());
         }

      }

   }

   public static class SourceConverter implements ByteArrayConverter
   {
      public Object readFrom(InputStream in) {
         return new StreamSource(in);
      }

      public void writeTo(Object obj, OutputStream out) {
         if(obj instanceof StreamSource)
         {
            StreamSource s = (StreamSource)obj;
            try
            {
               IOUtils.copyStream(out, s.getInputStream());
            }
            catch (IOException e)
            {
               throw new WSException("Failed to convert " + obj.getClass());
            }
         }
         else
         {
            throw new WSException("Unable to convert " + obj.getClass());
         }
      }
   }

   public static class StringConverter implements ByteArrayConverter
   {
      public Object readFrom(InputStream in) {
         Object converted = null;
         try
         {
            StringBuilder out = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            converted = out.toString();
         }
         catch (IOException e)
         {
            throw new WSException("Failed to convert java.lang.String");
         }

         return converted;
      }

      public void writeTo(Object obj, OutputStream out) {
         if(obj instanceof String)
         {
            String s = (String)obj;
            try
            {
               out.write(s.getBytes("UTF-8"));
            }
            catch (IOException e)
            {
               throw new WSException("Failed to convert " + obj.getClass());
            }
         }
         else
         {
            throw new WSException("Unable to convert " + obj.getClass());
         }
      }
   }

   public static class StreamConverter implements ByteArrayConverter
   {
      public Object readFrom(InputStream in) {
         return in;
      }

      public void writeTo(Object obj, OutputStream out) {
         if(obj instanceof InputStream)
         {
            try
            {
               IOUtils.copyStream(out, (InputStream)obj);
            }
            catch (IOException e)
            {
               throw new WSException("Failed to convert " + obj.getClass());
            }
         }
      }
   }
   public interface ByteArrayConverter
   {
      Object readFrom(InputStream in);
      void writeTo(Object obj, OutputStream out);
   }


}
