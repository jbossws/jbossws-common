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
package org.jboss.wsf.spi.tools.cmd;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.jboss.wsf.spi.tools.WSContractProvider;
import org.jboss.wsf.spi.util.Log4JUtil;
import org.jboss.wsf.spi.util.Log4jOutputStream;

/**
 * WSProvideTask is a cmd line tool that generates portable JAX-WS artifacts
 * for a service endpoint implementation.
 * 
 * <pre>
 *  usage: WSProvideTask [options] &lt;endpoint class name&gt;
 *  options: 
 *  <table>
 *  <tr><td>-h, --help                      </td><td>Show this help message</td></tr>
 *  <tr><td>-k, --keep                      </td><td>Keep/Generate Java source</td></tr>
 *  <tr><td>-w, --wsdl                      </td><td>Enable WSDL file generation</td></tr>
 *  <tr><td>-c, --classpath=&lt;path&lt;    </td><td>The classpath that contains the endpoint</td></tr>
 *  <tr><td>-o, --output=&lt;directory&gt;  </td><td>The directory to put generated artifacts</td></tr>
 *  <tr><td>-r, --resource=&lt;directory&gt;</td><td>The directory to put resource artifacts</td></tr>
 *  <tr><td>-s, --source=&lt;directory&gt;  </td><td>The directory to put Java source</td></tr>
 *  <tr><td>-q, --quiet                     </td><td>Be somewhat more quiet</td></tr>
 *  <tr><td>-t, --show-traces               </td><td>Show full exception stack traces</td></tr>
 *  <tr><td>-l, --load-provider             </td><td>Load the provider and exit (debug utility)</td></tr>
 *  <tr><td>-e, --extension                 </td><td>Enable SOAP 1.2 binding extension</td></tr>
 * </pre>
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public final class WSProvide
{
   private static final ClassLoader MODULES_LOADER = SecurityActions.getModulesClassLoader();
   private ClassLoader loader = MODULES_LOADER != null ? MODULES_LOADER : SecurityActions.getContextClassLoader();
   private File outputDir = new File("output");
   private boolean generateSource;
   private boolean generateWsdl;
   private boolean extension;
   private boolean quiet;
   private boolean showTraces;
   private boolean loadProvider;
   private File resourceDir;
   private File sourceDir;

   public static final String PROGRAM_NAME = SecurityActions.getSystemProperty("program.name", WSProvide.class.getSimpleName());

   public static void main(String[] args)
   {
      WSProvide generate = new WSProvide();
      String endpoint = generate.parseArguments(args);
      System.exit(generate.generate(endpoint));
   }
   
   //hide constructor
   private WSProvide()
   {
      //NOOP
   }
   
   private String parseArguments(String[] args)
   {
      String shortOpts = "hwko:r:s:c:qtle";
      LongOpt[] longOpts = 
      {
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("wsdl", LongOpt.NO_ARGUMENT, null, 'w'),
         new LongOpt("keep", LongOpt.NO_ARGUMENT, null, 'k'),
         new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
         new LongOpt("resource", LongOpt.REQUIRED_ARGUMENT, null, 'r'),
         new LongOpt("source", LongOpt.REQUIRED_ARGUMENT, null, 's'),
         new LongOpt("classpath", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
         new LongOpt("quiet", LongOpt.NO_ARGUMENT, null, 'q'),
         new LongOpt("show-traces", LongOpt.NO_ARGUMENT, null, 't'),
         new LongOpt("load-provider", LongOpt.NO_ARGUMENT, null, 'l'),
         new LongOpt("extension", LongOpt.NO_ARGUMENT, null, 'e'),
      };
      
      Getopt getopt = new Getopt(PROGRAM_NAME, args, shortOpts, longOpts);
      int c;
      while ((c = getopt.getopt()) != -1)
      {
         switch (c)
         {
            case 'k':
               generateSource = true;
               break;
            case 's':
               sourceDir = new File(getopt.getOptarg());
               break;
            case 'r':
               resourceDir = new File(getopt.getOptarg());
               break;
            case 'w':
               generateWsdl = true;
               break;
            case 't':
               showTraces = true;
               break;
            case 'o':
               outputDir = new File(getopt.getOptarg());
               break;
            case 'q':
               quiet = true;
               break;
            case 'c':
               processClassPath(getopt.getOptarg());
               break;
            case 'l':
               loadProvider = true;
               break;
            case 'e':
               extension = true;
               break;
            case 'h':
               printHelp();
               System.exit(0);
            case '?':
               System.exit(1);
         }
      }

      // debug output
      if(loadProvider)
      {
         WSContractProvider gen = WSContractProvider.newInstance(loader);
         System.out.println("WSContractProvider instance: " + gen.getClass().getCanonicalName());
         System.exit(0);
      }

      int endpointPos = getopt.getOptind();
      if (endpointPos >= args.length)
      {
         System.err.println("Error: endpoint implementation was not specified!");
         printHelp();
         System.exit(1);
      }
      
      return args[endpointPos];
   }
   
   
   private int generate(String endpoint)
   {
      try
      {
         SecurityActions.loadClass(loader, endpoint);
      }
      catch (Exception e)
      {
         System.err.println("Error: Could not load class [" + endpoint + "]. Did you specify a valid --classpath?");
         return 1;
      }
      
      WSContractProvider gen = WSContractProvider.newInstance(loader);
      gen.setGenerateWsdl(generateWsdl);
      gen.setGenerateSource(generateSource);
      gen.setOutputDirectory(outputDir);
      gen.setExtension(extension);
      if (resourceDir != null)
         gen.setResourceDirectory(resourceDir);
      if (sourceDir != null)
         gen.setSourceDirectory(sourceDir);

      if (! quiet)
      {
         PrintStream ps;
         if (Log4JUtil.isLog4jConfigurationAvailable())
         {
            ps = new PrintStream(new Log4jOutputStream(Logger.getLogger("WSProvide"), Level.INFO));
         }
         else
         {
            ps = System.out;
            ps.println("Could not find log4j.xml configuration, logging to console.\n");
         }
         gen.setMessageStream(ps);
      }
      
      try
      {
         gen.provide(endpoint);
         return 0;
      }
      catch (Throwable t)
      {
         System.err.println("Error: Could not generate. (use --show-traces to see full traces)");
         if (!showTraces)
         {
            String message = t.getMessage();
            if (message == null)
               message = t.getClass().getSimpleName();
            System.err.println("Error: " + message);
         }
         else
         {
            t.printStackTrace(System.err);
         }
         
      }
      
      return 1;
   }

   private void processClassPath(String classPath)
   {
      String[] entries =  classPath.split(File.pathSeparator);
      List<URL> urls= new ArrayList<URL>(entries.length);
      for (String entry : entries)
      {
         try 
         {
            urls.add(new File(entry).toURI().toURL());
         }
         catch (MalformedURLException e)
         {
            System.err.println("Error: a classpath entry was malformed: " + entry);
         }
      }
      loader = new URLClassLoader(urls.toArray(new URL[0]), loader);
   }

   private static void printHelp()
   {
      PrintStream out = System.out;
      out.println("WSProvideTask generates portable JAX-WS artifacts for an endpoint implementation.\n");
      out.println("usage: " + PROGRAM_NAME + " [options] <endpoint class name>\n");
      out.println("options: ");
      out.println("    -h, --help                  Show this help message");
      out.println("    -k, --keep                  Keep/Generate Java source");
      out.println("    -w, --wsdl                  Enable WSDL file generation");
      out.println("    -c, --classpath=<path>      The classpath that contains the endpoint");
      out.println("    -o, --output=<directory>    The directory to put generated artifacts");
      out.println("    -r, --resource=<directory>  The directory to put resource artifacts");
      out.println("    -s, --source=<directory>    The directory to put Java source");
      out.println("    -e, --extension             Enable SOAP 1.2 binding extension");
      out.println("    -q, --quiet                 Be somewhat more quiet");
      out.println("    -t, --show-traces           Show full exception stack traces");
      out.println("    -l, --load-provider         Load the provider and exit (debug utility)");
      out.flush();
   }
}
