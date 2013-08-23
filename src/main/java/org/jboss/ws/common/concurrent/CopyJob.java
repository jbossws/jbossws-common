/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.common.concurrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sample usage:
 *
 * <blockquote><pre>
 * CopyJob copyJob = new CopyJob( inputStream, printStream );
 * new Thread( copyJob ).start();
 * try
 * {
 *    // do some other stuff
 *    ...
 * }
 * finally
 * {
 *    copyJob.kill();
 * }
 * </pre></blockquote>
 *
 * @author richard.opalka@jboss.com
 */
public final class CopyJob implements Runnable
{

   /**
    * Input stream to data read from.
    */
   private final InputStream is;
   /**
    * Output stream to write data to.
    */
   private final OutputStream os;
   /**
    * Whether this job is terminated.
    */
   private volatile boolean terminated;
   
   private final boolean closeOsOnExit;

   /**
    * Constructor; the input stream will automatically be closed on exit, while the output stream won't.
    * 
    * @param is input stream to read data from
    * @param os output stream to write data to
    */
   public CopyJob( InputStream is, OutputStream os )
   {
      this(is, os, false);
   }
   
   /**
    * 
    * Constructor.
    * @param is input stream to read data from
    * @param os output stream to write data to
    * @param closeOutputStreamOnExit whether to flush and close the output stream on exit
    */
   public CopyJob( InputStream is, OutputStream os, boolean closeOutputStreamOnExit) {
      super();

      if ( ( is == null ) || ( os == null ) )
      {
         throw new IllegalArgumentException( "Constructor parameters can't be null" );
      }

      this.is = is;
      this.os = os;
      this.closeOsOnExit = closeOutputStreamOnExit;
   }

   /**
    * Copies all data from <b>input stream</b> to <b>output stream</b> (both passed to constructor) until job is killed
    */
   public final void run()
   {
      try
      {
         copy( this.is, this.os );
      }
      catch ( IOException ioe )
      {
         ioe.printStackTrace(System.err);
      }
      finally
      {
         try
         {
            this.is.close();
         }
         catch (IOException ioe)
         {
            ioe.printStackTrace(System.err);
         }
         if (this.closeOsOnExit)
         {
            try
            {
               this.os.flush();
               this.os.close();
            }
            catch (IOException ioe)
            {
               ioe.printStackTrace(System.err);
            }
         }
      }
   }

   /**
    * Copies all data from <b>is</b> to <b>os</b> until job is killed
    * @param is input stream to read data from
    * @param os output stream to write data to
    * @throws IOException if I/O error occurs
    */
   private void copy( final InputStream is, final OutputStream os ) throws IOException
   {
      final byte[] buffer = new byte[ 512 ];
      int countOfBytes = -1;

      while ( !this.terminated )
      {
         while ( is.available() <= 0 )
         {
            synchronized( this )
            {
               try
               {
                  this.wait( 50 ); // guard
                  if ( this.terminated ) return;
               }
               catch ( InterruptedException ie )
               {
                  ie.printStackTrace( System.err );
               }
            }
         }

         countOfBytes = is.read( buffer, 0, buffer.length );
         os.write( buffer, 0, countOfBytes );
      }
   }

   /**
    * Kills this job. Calling this method also ensures that input stream passed to the constructor will be closed properly
    */
   public final void kill()
   {
      this.terminated = true;
   }

}