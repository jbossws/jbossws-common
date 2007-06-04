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
package org.jboss.wsf.spi.tools.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.jboss.wsf.spi.tools.WSContractProvider;

import java.io.File;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/**
 * Ant task which invokes provides a Web Service contract and portable JAX-WS wrapper classes.
 * 
 * <table border="1">
 *   <tr align="left" BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"><th>Attribute</th><th>Description</th><th>Default</th></tr>
 *   <tr><td>fork</td><td>Whether or not to run the generation task in a separate VM.</td><td>true</td></tr>
 *   <tr><td>keep</td><td>Keep/Enable Java source code generation.</td><td>false</td></tr>
 *   <tr><td>destdir</td><td>The output directory for generated artifacts.</td><td>"output"</td></tr>
 *   <tr><td>resourcedestdir</td><td>The output directory for resource artifacts (WSDL/XSD).</td><td>value of destdir</td></tr>
 *   <tr><td>sourcedestdir</td><td>The output directory for Java source.</td><td>value of destdir</td></tr>
 *   <tr><td>genwsdl</td><td>Whether or not to generate WSDL.</td><td>false</td><tr>
 *   <tr><td>verbose</td><td>Enables more informational output about cmd progress.</td><td>false</td><tr>
 *   <tr><td>sei*</td><td>Service Endpoint Implementation.</td><td></td><tr>
 *   <tr><td>classpath</td><td>The classpath that contains the service endpoint implementation.</td><td>""</tr>
 * </table>
 * <b>* = required.</b>
 * 
 * <p>Example:
 * 
 * <pre>
 *  &lt;target name=&quot;test-wsproivde&quot; depends=&quot;init&quot;&gt;
 *    &lt;taskdef name=&quot;WSProvideTask&quot; classname=&quot;org.jboss.wsf.spi.tools.ant.WSProvideTask&quot;&gt;
 *      &lt;classpath refid=&quot;core.classpath&quot;/&gt;
 *    &lt;/taskdef&gt;
 *    &lt;WSProvideTask
 *      fork=&quot;false&quot;
 *      keep=&quot;true&quot;
 *      destdir=&quot;out&quot;
 *      resourcedestdir=&quot;out-resource&quot;
 *      sourcedestdir=&quot;out-source&quot;
 *      genwsdl=&quot;true&quot; 
 *      verbose=&quot;true&quot;
 *      sei=&quot;org.jboss.test.ws.jaxws.jsr181.soapbinding.DocWrappedServiceImpl&quot;&gt;
 *      &lt;classpath&gt;
 *        &lt;pathelement path=&quot;${tests.output.dir}/classes&quot;/&gt;
 *      &lt;/classpath&gt;
 *    &lt;/WSProvideTask&gt;
 *  &lt;/target&gt;
 * </pre>
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class WSProvideTask extends Task
{
   private Path classpath = new Path(getProject());
   private CommandlineJava command = new CommandlineJava();
   private String sei = null;
   private File destdir = null;
   private File resourcedestdir = null;
   private File sourcedestdir = null;
   private boolean keep = false;
   private boolean genwsdl = false;
   private boolean verbose = false;
   private boolean fork = false;
   private boolean debug = false;
   
   // Not actually used right now
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }
   
   public Commandline.Argument createJvmarg() 
   {
      return command.createVmArgument();
   }
   
   public void setClasspath(Path classpath)
   {
      this.classpath = classpath;
   }
   
   public void setClasspathRef(Reference ref)
   {
      createClasspath().setRefid(ref);
   }
   
   public Path createClasspath()
   {
      return classpath;
   }
   
   public void setDestdir(File destdir)
   {
      this.destdir = destdir;
   }

   public void setKeep(boolean keep)
   {
      this.keep = keep;
   }
   
   public void setSei(String sei)
   {
      this.sei = sei;
   }
   
   public void setFork(boolean fork)
   {
      this.fork = fork;
   }

   public void setResourcedestdir(File resourcedestdir)
   {
      this.resourcedestdir = resourcedestdir;
   }

   public void setSourcedestdir(File sourcedestdir)
   {
      this.sourcedestdir = sourcedestdir;
   }

   public void setVerbose(boolean verbose)
   {
      this.verbose = verbose;
   }

   public void setGenwsdl(boolean genwsdl)
   {
      this.genwsdl = genwsdl;
   }
   
   private ClassLoader getClasspathLoader(ClassLoader parent)
   {
		AntClassLoader antLoader = new AntClassLoader(parent, getProject(), classpath, false);

		// It's necessary to wrap it into an URLLoader in order to extract that information
		// within the actual provider impl.
		// See SunRIProviderImpl for instance
		List<URL> urls = new ArrayList<URL>();
		StringTokenizer tok = new StringTokenizer(antLoader.getClasspath(), File.separator);
		while(tok.hasMoreTokens())
		{
			try
			{
				urls.add(new URL(tok.nextToken()));
			}
			catch (MalformedURLException e)
			{
				throw new IllegalArgumentException("Failed to wrap classloader", e);
			}

		}

		ClassLoader wrapper = new URLClassLoader(urls.toArray(new URL[0]), antLoader);
		return wrapper;
   }
   
   public void executeNonForked()
   {
      ClassLoader prevCL = Thread.currentThread().getContextClassLoader();
      ClassLoader antLoader = this.getClass().getClassLoader();
      Thread.currentThread().setContextClassLoader(antLoader);
      try
      {
         WSContractProvider gen = WSContractProvider.newInstance(
					getClasspathLoader(antLoader)
			);         
         if (verbose)
            gen.setMessageStream(new PrintStream(new LogOutputStream(this, Project.MSG_INFO)));
         gen.setGenerateSource(keep);
         gen.setGenerateWsdl(genwsdl);
         if (destdir != null)
            gen.setOutputDirectory(destdir);
         if (resourcedestdir != null)
            gen.setResourceDirectory(resourcedestdir);
         if (sourcedestdir != null)
            gen.setSourceDirectory(sourcedestdir);

         log("Generating from endpoint: " + sei, Project.MSG_INFO);
         
         gen.provide(sei);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(prevCL);
      }
   }
   
   public void execute() throws BuildException
   {
      if (sei == null)
         throw new BuildException("The sei attribute must be specified!", getLocation());
      
      if (fork)
         executeForked();
      else
         executeNonForked();
   }
   
   private Path getTaskClassPath()
   {
      // Why is everything in the Ant API a big hack???
      ClassLoader cl = this.getClass().getClassLoader();
      if (cl instanceof AntClassLoader)
      {
         return new Path(getProject(), ((AntClassLoader)cl).getClasspath());
      }
      
      return new Path(getProject());
   }

   private void executeForked() throws BuildException
   {
      command.setClassname(org.jboss.wsf.spi.tools.cmd.WSProvide.class.getName());
      
      Path path = command.createClasspath(getProject());
      path.append(getTaskClassPath());
      path.append(classpath);
     
      if (keep)
         command.createArgument().setValue("-k");
      
      if (genwsdl)
         command.createArgument().setValue("-w");
      
      if (destdir != null)
      {
         command.createArgument().setValue("-o");
         command.createArgument().setFile(destdir);
      }
      if (resourcedestdir != null)
      {
         command.createArgument().setValue("-r");
         command.createArgument().setFile(resourcedestdir);
      }
      if (sourcedestdir != null)
      {
         command.createArgument().setValue("-s");
         command.createArgument().setFile(sourcedestdir);
      }
      
      if (!verbose)
         command.createArgument().setValue("-q");
      
      // Always dump traces
      command.createArgument().setValue("-t");
      command.createArgument().setValue(sei);
      
      if (verbose)
         log("Command invoked: " + command.getJavaCommand().toString());
      
      ExecuteJava execute = new ExecuteJava();
      execute.setClasspath(path);
      execute.setJavaCommand(command.getJavaCommand());
      if (execute.fork(this) != 0)
         throw new BuildException("Could not invoke WSProvideTask", getLocation());
   }
}