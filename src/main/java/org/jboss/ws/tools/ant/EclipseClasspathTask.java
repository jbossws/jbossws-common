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
package org.jboss.ws.tools.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * An Ant task creating a simple Eclipse .classpath file using the provided
 * Ant's path id.
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Feb-2008
 */
public class EclipseClasspathTask extends Task
{
   private String pathId;
   private String excludesFile;

   @Override
   public void execute() throws BuildException
   {
      Project project = getProject();
      Path path = (Path)project.getReference(pathId);
      String[] pathElements = path.list();
      try
      {
         List<String> excludes = getExcludes();
         StringBuffer sb = new StringBuffer();
         generateContent(sb, excludes, pathElements);
         BufferedWriter out = new BufferedWriter(new FileWriter(new File(getProject().getBaseDir(), ".classpath")));
         out.write(sb.toString());
         out.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new BuildException(e);
      }
   }

   private void generateContent(StringBuffer sb, List<String> excludes, String[] libs)
   {
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      sb.append("<classpath>\n");
      if (excludes != null && !excludes.isEmpty())
      {
         sb.append("<classpathentry excluding=\"");
         for (Iterator<String> it = excludes.iterator(); it.hasNext();)
         {
            sb.append(it.next());
            if (it.hasNext())
               sb.append("|");
         }
         sb.append("\" kind=\"src\" path=\"tests/java\"/>\n");
      }
      sb.append("<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
      for (int i = 0; i < libs.length; i++)
      {
         if (new File(libs[i]).exists())
         {
            sb.append("<classpathentry kind=\"lib\" path=\"");
            sb.append(absoluteToRelativePath(libs[i]));
            sb.append("\"/>\n");
         }
      }
      sb.append("<classpathentry kind=\"output\" path=\"bin\"/>\n");
      sb.append("</classpath>");
   }

   private List<String> getExcludes() throws IOException
   {
      List<String> excludes = new LinkedList<String>();
      BufferedReader in = null;
      try
      {
         in = new BufferedReader(new FileReader(excludesFile));
         String str;
         while ((str = in.readLine()) != null)
         {
            if (str.length() > 0 & !str.startsWith("#"))
               excludes.add(str);
         }
      }
      finally
      {
         if (in != null)
            in.close();
      }
      return excludes;
   }

   private String absoluteToRelativePath(String absolutePath)
   {
      String baseDir = getProject().getBaseDir().toString();
      String result = absolutePath;
      if (absolutePath.startsWith(baseDir))
      {
         result = absolutePath.substring(baseDir.length());
         if (result.startsWith("\\") || result.startsWith("/"))
            result = result.substring(1);
      }
      return result;
   }

   public void setPathId(String pathId)
   {
      this.pathId = pathId;
   }

   public void setExcludesFile(String excludesFile)
   {
      this.excludesFile = excludesFile;
   }

}
