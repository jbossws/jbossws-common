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
package org.jboss.wsf.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * TCK Report to JUnit Report Converter Utility
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 23, 2008
 */
public final class TCK14ToJunitReportConverter
{
   
   private static final FileFilter filter = new TCKReportFileFilter();
   private static File junitReportDir = null;

   private static final class TCKReportFileFilter implements FileFilter
   {
      public boolean accept(File f)
      {
         return f.isDirectory() ? true : f.getName().endsWith(".jtr"); 
      }
   }
   
   public static void main(String[] args) throws IOException
   {
      if (args.length != 2)
      {
         throw new IllegalArgumentException();
      }
      
      File tckReportDir = new File(args[0]);
      junitReportDir = new File(args[1]);
      if (!tckReportDir.exists() && !tckReportDir.isDirectory())
      {
         throw new IllegalArgumentException("TCK report directory '" + tckReportDir.getAbsolutePath() + "' doesn't exist or is not directory");
      }
      if (!junitReportDir.mkdir() || (!junitReportDir.exists() && !junitReportDir.isDirectory()))
      {
         throw new IllegalArgumentException("JUnit report Directory '" + junitReportDir.getAbsolutePath() + "' doesn't exist or is not directory");
      }

      File[] files = tckReportDir.listFiles(filter);
      for (File f : files)
      {
         if (f.isDirectory())
         {
            convertDirectory("", f);
         }
         else
         {
            convertFile("", f);
         }
      }
   }
   
   private static void convertDirectory(String pckg, File dir) throws IOException
   {
      File[] files = dir.listFiles(filter);
      for (File f : files)
      {
         if (f.isDirectory())
         {
            convertDirectory((pckg.length() == 0 ? "" : (pckg + "/")) + f.getName(), f);
         }
         else
         {
            convertFile(pckg, f);
         }
      }
   }
   
   private static void convertFile(String pckg, File f) throws IOException
   {
      BufferedReader reader = new BufferedReader(new FileReader(f));
      StringBuilder sb = new StringBuilder();
      String line = reader.readLine();
      boolean passed = false;
      while (line != null)
      {
         if (line.trim().length() > 0)
         {
            sb.append(line);
            sb.append("\n");
            passed = false;
         }
         if (line.indexOf("test result: Passed") != -1)
         {
            passed = true;
         }
         line = reader.readLine();
      }
      createReport(sb.toString(), passed, pckg, f);
   }
   
   private static void createReport(String consoleOutput, boolean passed, String pckg, File file) throws IOException
   {
      String fileName = file.getName().substring(0, file.getName().length() - 4);
      StringBuilder sb = new StringBuilder();
      String nl = "\n";
      sb.append("<?xml version='1.0' encoding='UTF-8'?>" + nl);
      sb.append("<testsuite errors='0' failures='" + (passed ? 0 : 1) + "' name='" + pckg.replace('/', '.') + "." + fileName + "' tests='1' time='1'>" + nl);
      sb.append("  <properties/>" + nl);
      sb.append("  <testcase classname='" + pckg.replace('/', '.') + "' name='" + fileName + "' time='1'/>" + nl);
      sb.append("  <system-out><![CDATA[" + replace("]]>", "] ]>", consoleOutput) + "]]></system-out>" + nl);
      sb.append("  <system-err><![CDATA[]]></system-err>" + nl);
      sb.append("</testsuite>" + nl);
      File junitReportFile = new File(junitReportDir, "TEST-" + pckg.replace('/', '.') + "." + fileName + ".xml");
      System.out.println("Creating JUnit report file " + junitReportFile.getAbsolutePath());
      FileOutputStream os = new FileOutputStream(junitReportFile);
      os.write(sb.toString().getBytes());
      os.flush();
      os.close();
   }
   
   private static String replace(String oldString, String newString, String data)
   {
      int fromIndex = 0;
      int index = 0;
      StringBuilder result = new StringBuilder();
      
      while ((index = data.indexOf(oldString, fromIndex)) >= 0)
      {
         result.append(data.substring(fromIndex, index));
         result.append(newString);
         fromIndex = index + oldString.length();
      }
      result.append(data.substring(fromIndex));
      return result.toString();
   }

}
