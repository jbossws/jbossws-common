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
package org.jboss.ws.tools.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * An Ant task that writes a given path element to a file, so that
 * it can be easily imported by other build files.
 * 
 * @author alessio.soldano@jboss.com
 * @since 13-Mar-2008
 */
public class PathWriterTask extends Task
{
   private String pathId;
   private String outputFile;
   private String variables; //to perform path substitution, i.e. jboss.home <--> /dati/jboss-4.2.3.GA

   @Override
   public void execute() throws BuildException
   {
      Project project = getProject();
      Path path = (Path)project.getReference(pathId);
      String[] pathElements = path.list();
      try
      {
         StringBuffer sb = new StringBuffer();
         generateContent(sb, pathElements);
         BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFile)));
         out.write(sb.toString());
         out.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new BuildException(e);
      }
   }

   private void generateContent(StringBuffer sb, String[] libs)
   {
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      sb.append("<project>\n");
      sb.append("<path id=\"");
      sb.append(pathId);
      sb.append("\">");
      for (int i = 0; i < libs.length; i++)
      {
         sb.append("<pathelement location=\"");
         sb.append(getPath(libs[i]));
         sb.append("\"/>\n");
      }
      sb.append("</path>");
      sb.append("</project>\n");
   }

   private String getPath(String absolutePath)
   {
      StringTokenizer st = new StringTokenizer(variables, ";:, ", false);
      while (st.hasMoreTokens())
      {
         String v = st.nextToken();
         String value = getProject().getProperty(v);
         if (absolutePath.contains(value))
         {
            int begin = absolutePath.indexOf(value);
            int end = begin + value.length();
            absolutePath = absolutePath.substring(0, begin) + "${" + v + "}" + absolutePath.substring(end);
         }
      }
      return absolutePath;
   }

   public void setPathId(String pathId)
   {
      this.pathId = pathId;
   }

   public void setOutputFile(String outputFile)
   {
      this.outputFile = outputFile;
   }

   public void setVariables(String variables)
   {
      this.variables = variables;
   }

}
