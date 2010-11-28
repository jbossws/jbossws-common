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
package org.jboss.test.wsf.spi.tools;

import junit.framework.TestCase;

import java.security.Permission;

/**
 * @author Heiko.Braun@jboss.com
 */
public abstract class CommandlineTestBase extends TestCase
{
   SecurityManager systemDefault = System.getSecurityManager();
   SecurityManager interceptor = new InterceptedSecurity();

   protected void swapSecurityManager()
   {
      if(System.getSecurityManager() instanceof InterceptedSecurity)
         System.setSecurityManager(systemDefault);
      else
         System.setSecurityManager(interceptor);
   }

   class InterceptedSecurity extends  SecurityManager
   {
      private final SecurityManager parent = systemDefault;

      public void checkPermission(Permission perm)
      {
         if (parent != null)
         {
            parent.checkPermission(perm);
         }
      }

      public void checkExit(int status)
      {
         String msg = (status == 0) ? "Delegate did exit without errors" : "Delegate did exit with an error";
         throw new InterceptedExit(msg, status);
      }
   }

   static protected class InterceptedExit extends SecurityException
   {
      private static final long serialVersionUID = 1L;
      private int exitCode;

      public InterceptedExit(String s, int code)
      {
         super(s);
         this.exitCode = code;
      }


      public int getExitCode()
      {
         return exitCode;
      }
   }

   protected void executeCmd(String arguments,  boolean expectedException) throws Exception
   {
      swapSecurityManager();

      String[] args = arguments!=null ? arguments.split("\\s"): new String[0];
      try
      {
         runDelegate(args);
         if(expectedException)
            fail("Did expect exception on args: " +args);
      }
      catch (CommandlineTestBase.InterceptedExit e)
      {
         boolean positivStatus = (e.getExitCode() == 0);
         if( (expectedException && positivStatus)
           || (!expectedException && !positivStatus) )
         {
            String s = expectedException ? "Did expect an exception, but " : "Did not expect an exception, but ";
            String s2 = positivStatus ? "status was positiv" : "status was negativ";
            throw new Exception(s+s2);
         }

      }
      finally
      {
         swapSecurityManager();
      }
   }

   // the actual tools execution
   abstract void runDelegate(String[] args) throws Exception;
   
}
