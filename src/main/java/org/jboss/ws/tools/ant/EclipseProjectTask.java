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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * An Ant task creating a simple Eclipse .project file using the provided project name.
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Feb-2008
 */
public class EclipseProjectTask extends Task
{
   private String projectName;
   private String outputFile;

   @Override
   public void execute() throws BuildException
   {
      try
      {
         StringBuffer sb = new StringBuffer();
         generateContent(sb);
         File file;
         if (outputFile != null)
            file = new File(outputFile);
         else
            file = new File(getProject().getBaseDir(), ".project");
         BufferedWriter out = new BufferedWriter(new FileWriter(file));
         out.write(sb.toString());
         out.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new BuildException(e);
      }
   }
   
   private void generateContent(StringBuffer sb)
   {
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      sb.append("<projectDescription>\n   <name>");
      sb.append(projectName);
      sb.append("</name>\n" +
            "   <comment></comment>\n" +
            "   <projects>\n" +
            "   </projects>\n" +
            "   <buildSpec>\n" +
            "      <buildCommand>\n" +
            "         <name>org.eclipse.jdt.core.javabuilder</name>\n" +
            "         <arguments>\n" +
            "         </arguments>\n" +
            "      </buildCommand>\n" +
            "   </buildSpec>\n" +
            "   <natures>\n" +
            "      <nature>org.eclipse.jdt.core.javanature</nature>\n" +
            "   </natures>\n" +
            "</projectDescription>");
   }

   public void setProjectName(String projectName)
   {
      this.projectName = projectName;
   }

   public void setOutputFile(String outputFile)
   {
      this.outputFile = outputFile;
   }
}
