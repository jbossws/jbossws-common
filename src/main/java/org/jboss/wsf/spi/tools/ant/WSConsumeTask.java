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
package org.jboss.wsf.spi.tools.ant;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.jboss.wsf.spi.tools.WSContractConsumer;

/**
 * Ant task which consumes a Web Service contract.
 *
 * <table border="1">
 *   <tr align="left" BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"><th>Attribute</th><th>Description</th><th>Default</th></tr>
 *   <tr><td>fork</td><td>Whether or not to run the generation task in a separate VM.</td><td>true</td></tr>
 *   <tr><td>keep</td><td>Keep/Enable Java source code generation.</td><td>false</td></tr>
 *   <tr><td>catalog</td><td> Oasis XML Catalog file for entity resolution</td><td>none</td></tr>
 *   <tr><td>package</td><td> The target Java package for generated code.</td><td>generated</td></tr>
 *   <tr><td>binding</td><td>A JAX-WS or JAXB binding file</td><td>none</td></tr>
 *   <tr><td>wsdlLocation</td><td>Value to use for @@WebService.wsdlLocation</td><td>generated</td></tr>
 *   <tr><td>destdir</td><td>The output directory for generated artifacts.</td><td>"output"</td></tr>
 *   <tr><td>sourcedestdir</td><td>The output directory for Java source.</td><td>value of destdir</td></tr>
 *   <tr><td>extension</td><td>Enable SOAP 1.2 binding extension.</td><td>false</td></tr>
 *   <tr><td>target</td><td>The JAX-WS specification target. Allowed values are 2.0, 2.1 and 2.2</td><td></td></tr>
 *   <tr><td>verbose</td><td>Enables more informational output about cmd progress.</td><td>false</td><tr>
 *   <tr><td>wsdl*</td><td>The WSDL file or URL</td><td>n/a</td><tr>
 * </table>
 * <b>* = required.</b>
 *
 * <p>Example:
 *
 * <pre>
 * &lt;WSConsumeTask
 *   fork=&quot;true&quot;
 *   verbose=&quot;true&quot;
 *   destdir=&quot;output&quot;
 *   sourcedestdir=&quot;gen-src&quot;
 *   keep=&quot;true&quot;
 *   wsdllocation=&quot;handEdited.wsdl&quot;
 *   wsdl=&quot;foo.wsdl&quot;&gt;
 *   &lt;binding dir=&quot;binding-files&quot; includes=&quot;*.xml&quot; excludes=&quot;bad.xml&quot;/&gt;
 * &lt;/wsimport&gt;
 * </pre>
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class WSConsumeTask extends Task
{
   private CommandlineJava command = new CommandlineJava();
   private String wsdl;
   private File destdir;
   private File sourcedestdir;
   private List<File> bindingFiles = new ArrayList<File>();
   private File catalog;
   private String wsdlLocation;
   private String targetPackage;
   private boolean keep;
   private boolean extension;
   private boolean verbose;
   private boolean fork;
   private boolean debug;
   private boolean nocompile;
   private boolean additionalHeaders;
   private String target;

   // Not actually used right now
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   public Commandline.Argument createJvmarg()
   {
      return command.createVmArgument();
   }

   public void setBinding(File bindingFile)
   {
      bindingFiles.add(bindingFile);
   }

   public void setCatalog(File catalog)
   {
      this.catalog = catalog;
   }

   public void setDestdir(File destdir)
   {
      this.destdir = destdir;
   }

   public void setFork(boolean fork)
   {
      this.fork = fork;
   }

   public void setKeep(boolean keep)
   {
      this.keep = keep;
   }

   public void setExtension(boolean extension)
   {
      this.extension = extension;
   }
   
   public void setAdditionalHeaders(boolean additionalHeaders)
   {
      this.additionalHeaders = additionalHeaders;
   }

   public void setSourcedestdir(File sourcedestdir)
   {
      this.sourcedestdir = sourcedestdir;
   }

   public void setTarget(String target)
   {
      this.target = target;
   }

   public void setPackage(String targetPackage)
   {
      this.targetPackage = targetPackage;
   }

   public void setVerbose(boolean verbose)
   {
      this.verbose = verbose;
   }

   public void setNoCompile(boolean nocompile)
   {
      this.nocompile = nocompile;
   }

   public void setWsdl(String wsdl)
   {
      this.wsdl = wsdl;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   public void addConfiguredBinding(FileSet fs)
   {
      DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      File baseDir = ds.getBasedir();
      for (String file : ds.getIncludedFiles())
      {
         bindingFiles.add(new File(baseDir, file));
      }
   }

   public void executeNonForked()
   {
      ClassLoader prevCL = SecurityActions.getContextClassLoader();
      ClassLoader antLoader = SecurityActions.getClassLoader(this.getClass());
      SecurityActions.setContextClassLoader(antLoader);
      try
      {
         WSContractConsumer consumer = WSContractConsumer.newInstance();
         consumer.setGenerateSource(keep);
         consumer.setExtension(extension);
         consumer.setAdditionalHeaders(additionalHeaders);
         consumer.setNoCompile(nocompile);
         if (destdir != null)
            consumer.setOutputDirectory(destdir);
         if (sourcedestdir != null)
            consumer.setSourceDirectory(sourcedestdir);
         if (targetPackage != null)
            consumer.setTargetPackage(targetPackage);
         if (wsdlLocation != null)
            consumer.setWsdlLocation(wsdlLocation);
         if (catalog != null)
         {
            if (catalog.exists() && catalog.isFile())
            {
               consumer.setCatalog(catalog);
            }
            else
            {
               log("Catalog file not found: " + catalog, Project.MSG_WARN);
            }
         }
         if (bindingFiles != null && bindingFiles.size() > 0)
            consumer.setBindingFiles(bindingFiles);
         if (target != null)
            consumer.setTarget(target);

         log("Consuming wsdl: " + wsdl, Project.MSG_INFO);

         if (verbose)
         {
            consumer.setMessageStream(new PrintStream(new LogOutputStream(this, Project.MSG_INFO)));
         }

         try
         {
            consumer.setAdditionalCompilerClassPath(getTaskClassPathStrings());
            consumer.consume(wsdl);
         }
         catch (Throwable e)
         {
            throw new BuildException(e, getLocation());
         }
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevCL);
      }
   }

   public void execute() throws BuildException
   {
      if (wsdl == null)
         throw new BuildException("The wsdl attribute must be specified!", getLocation());

      if (fork)
         executeForked();
      else executeNonForked();
   }

   private Path getTaskClassPath()
   {
      // Why is everything in the Ant API a big hack???
      ClassLoader cl = SecurityActions.getClassLoader(this.getClass());
      if (cl instanceof AntClassLoader)
      {
         return new Path(getProject(), ((AntClassLoader)cl).getClasspath());
      }

      return new Path(getProject());
   }

   private List<String> getTaskClassPathStrings()
   {
      // Why is everything in the Ant API a big hack???
      List<String> strings = new ArrayList<String>();
      ClassLoader cl = SecurityActions.getClassLoader(this.getClass());
      if (cl instanceof AntClassLoader)
      {
         for (String string : ((AntClassLoader)cl).getClasspath().split(File.pathSeparator))
            strings.add(string);
      }

      return strings;
   }

   private void executeForked() throws BuildException
   {
      command.setClassname(org.jboss.wsf.spi.tools.cmd.WSConsume.class.getName());

      Path path = command.createClasspath(getProject());
      path.append(getTaskClassPath());

      if (keep)
         command.createArgument().setValue("-k");
      
      if (extension)
         command.createArgument().setValue("-e");
      
      if (additionalHeaders)
         command.createArgument().setValue("-a");

      for (File file : bindingFiles)
      {
         command.createArgument().setValue("-b");
         command.createArgument().setFile(file);
      }

      if (catalog != null)
      {
         command.createArgument().setValue("-c");
         command.createArgument().setFile(catalog);
      }

      if (targetPackage != null)
      {
         command.createArgument().setValue("-p");
         command.createArgument().setValue(targetPackage);
      }

      if (wsdlLocation != null)
      {
         command.createArgument().setValue("-w");
         command.createArgument().setValue(wsdlLocation);
      }

      if (destdir != null)
      {
         command.createArgument().setValue("-o");
         command.createArgument().setFile(destdir);
      }

      if (sourcedestdir != null)
      {
         command.createArgument().setValue("-s");
         command.createArgument().setFile(sourcedestdir);
      }

      if (target != null)
      {
         command.createArgument().setValue("-t");
         command.createArgument().setValue(target);
      }

      if (verbose)
         command.createArgument().setValue("-v");

      command.createArgument().setValue(wsdl);

      log("Consuming wsdl: " + wsdl, Project.MSG_INFO);
      
      if (verbose)
         log("Command invoked: " + command.getJavaCommand().toString());

      ExecuteJava execute = new ExecuteJava();
      execute.setClasspath(path);
      execute.setJavaCommand(command.getJavaCommand());

      // propagate system properties (useful e.g. for endorsing)
      String[] arguments = command.getVmCommand().getArguments();
      SysProperties properties = AntTaskHelper.toSystemProperties(arguments);
      execute.setSystemProperties(properties);

      if (execute.fork(this) != 0)
         throw new BuildException("Could not invoke WSConsumeTask", getLocation());
   }
}
