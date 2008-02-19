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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * An Ant task creating Eclipse's launch configuration files for the JUnit tests
 * of the test-suite.
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Feb-2008
 */
public class EclipseJUnitTestsTask extends Task
{
   private String projectName;
   private String projectWorkingDir; // the Eclipse project working dir, i.e. the output dir
   private String srcDir; // the tests src dir
   private String integrationTarget;
   private String jbossHome;
   private String endorsedDir;
   private String namingProviderUrl;
   private String securityPolicy;
   private FileSet fileset;

   @Override
   public void execute() throws BuildException
   {
      try
      {
         DirectoryScanner dsc = fileset.getDirectoryScanner(getProject());
         String[] classes = dsc.getIncludedFiles();
         for (int i = 0; i < classes.length; i++)
         {
            String clazz = classes[i];
            File file = new File(getProject().getBaseDir(), pathToClassName(clazz) + ".launch");
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(getSingleTestConf(clazz).toString());
            out.close();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new BuildException(e);
      }
   }

   public FileSet createFileset()
   {
      this.fileset = new FileSet();
      return fileset;
   }

   private static String pathToFullClassName(String path)
   {
      // remove ".class" and replace slashes and backslashes with a dot
      return path.substring(0, path.length() - 6).replaceAll("\\\\", ".").replaceAll("/", ".");
   }

   private static String pathToClassName(String path)
   {
      String fullClassName = pathToFullClassName(path);
      return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
   }

   private LaunchConfiguration getSingleTestConf(String clazz)
   {
      LaunchConfiguration conf = new LaunchConfiguration();
      conf.addEntryToListAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS", "/" + projectName + "/" + absoluteToRelativePath(srcDir) + "/"
            + clazz.substring(0, clazz.length() - 6) + ".java");
      conf.addEntryToListAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", "1");
      conf.putBooleanAttribute("org.eclipse.debug.core.appendEnvironmentVariables", true);
      conf.putBooleanAttribute("org.eclipse.jdt.junit.KEEPRUNNING_ATTR", false);
      conf.putStringAttribute("org.eclipse.jdt.junit.CONTAINER", "");
      conf.putStringAttribute("org.eclipse.jdt.junit.TESTNAME", "");
      conf.putStringAttribute("org.eclipse.jdt.junit.TEST_KIND", "org.eclipse.jdt.junit.loader.junit3");
      conf.putStringAttribute("org.eclipse.jdt.launching.MAIN_TYPE", pathToFullClassName(clazz));
      conf.putStringAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", projectName);
      // computing the userDir; please note we get the relative path since we use the Eclipse $workspace_loc variable
      String userDir = "${workspace_loc:" + projectName + "}/" + absoluteToRelativePath(projectWorkingDir);
      conf.putStringAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS", getVMArguments(userDir));
      conf.putStringAttribute("org.eclipse.jdt.launching.WORKING_DIRECTORY", userDir);
      return conf;
   }

   private String getVMArguments(String userDir)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("-Djbossws.integration.target=").append(integrationTarget);
      sb.append("&#10;-ea&#10;");
      sb.append("-Dtest.execution.dir=").append(userDir);
      sb.append("&#10;-Djava.endorsed.dirs=").append(endorsedDir);
      sb.append("&#10;");
      sb.append("-Djava.naming.provider.url=").append(namingProviderUrl);
      sb.append("&#10;-Djava.protocol.handler.pkgs=org.jboss.virtual.protocol&#10;");
      sb.append("-Djava.security.policy=").append(absoluteToRelativePath(securityPolicy));
      sb.append("&#10;-Djava.naming.factory.initial=org.jnp.interfaces.NamingContextFactory&#10;");
      sb.append("-Duser.dir=").append(userDir);
      sb.append("&#10;-Djboss.home=").append(jbossHome);
      sb.append("&#10;-Djdk.home=${env_var:JAVA_HOME}");
      return sb.toString();
   }

   private String absoluteToRelativePath(String absolutePath)
   {
      String baseDir = getProject().getBaseDir().toString();
      if (!absolutePath.startsWith(baseDir))
         throw new IllegalArgumentException("The provided absolute path is outside the current basedir: " + baseDir);
      return absolutePath.substring(baseDir.length() + 1);
   }

   public void setSrcDir(String srcDir)
   {
      this.srcDir = srcDir;
   }

   public void setProjectName(String projectName)
   {
      this.projectName = projectName;
   }

   public void setProjectWorkingDir(String projectWorkingDir)
   {
      this.projectWorkingDir = projectWorkingDir;
   }

   public void setIntegrationTarget(String integrationTarget)
   {
      this.integrationTarget = integrationTarget;
   }

   public void setJbossHome(String jbossHome)
   {
      this.jbossHome = jbossHome;
   }

   public void setNamingProviderUrl(String namingProviderUrl)
   {
      this.namingProviderUrl = namingProviderUrl;
   }

   public void setSecurityPolicy(String securityPolicy)
   {
      this.securityPolicy = securityPolicy;
   }

   public void setEndorsedDir(String endorsedDir)
   {
      this.endorsedDir = endorsedDir;
   }

   private class LaunchConfiguration
   {
      private Map<String, String> booleanAttributes = new LinkedHashMap<String, String>();
      private Map<String, String> stringAttributes = new LinkedHashMap<String, String>();
      private Map<String, List<String>> listAttributes = new LinkedHashMap<String, List<String>>();

      public String toString()
      {
         StringBuffer sb = new StringBuffer();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         sb.append("<launchConfiguration type=\"org.eclipse.jdt.junit.launchconfig\">\n");
         for (String key : listAttributes.keySet())
         {
            sb.append("<listAttribute key=\"").append(key).append("\">\n");
            for (String value : listAttributes.get(key))
            {
               sb.append("<listEntry value=\"").append(value).append("\"/>\n");
            }
            sb.append("</listAttribute>\n");
         }
         for (String key : booleanAttributes.keySet())
         {
            sb.append("<booleanAttribute key=\"").append(key);
            sb.append("\" value=\"").append(booleanAttributes.get(key)).append("\"/>\n");
         }
         for (String key : stringAttributes.keySet())
         {
            sb.append("<stringAttribute key=\"").append(key);
            sb.append("\" value=\"").append(stringAttributes.get(key)).append("\"/>\n");
         }
         sb.append("</launchConfiguration>");
         return sb.toString();
      }

      public Map<String, String> getBooleanAttributes()
      {
         return booleanAttributes;
      }

      public void putBooleanAttribute(String name, boolean value)
      {
         this.booleanAttributes.put(name, String.valueOf(value));
      }

      public Map<String, String> getStringAttributes()
      {
         return stringAttributes;
      }

      public void putStringAttribute(String name, String value)
      {
         this.stringAttributes.put(name, value);
      }

      public Map<String, List<String>> getListAttributes()
      {
         return listAttributes;
      }

      public void addEntryToListAttribute(String attribute, String entryValue)
      {
         if (!listAttributes.containsKey(attribute))
            listAttributes.put(attribute, new LinkedList<String>());
         listAttributes.get(attribute).add(entryValue);
      }
   }
}
