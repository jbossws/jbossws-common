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
package org.jboss.wsf.spi.tools.cmd;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.jboss.wsf.spi.tools.WSContractProvider;
import org.jboss.wsf.common.JavaUtils;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * WSProvideTask is a cmd line tool that generates portable JAX-WS artifacts
 * for a service endpoint implementation.
 * 
 * <pre>
 *  usage: WSProvideTask [options] &lt;endpoint class name&gt;
 *  options: 
 *  -h, --help                  Show this help message
 *  -k, --keep                  Keep/Generate Java source
 *  -w, --wsdl                  Enable WSDL file generation
 *  -c, --classpath=&lt;path&lt;      The classpath that contains the endpoint
 *  -o, --output=&lt;directory&gt;    The directory to put generated artifacts
 *  -r, --resource=&lt;directory&gt;  The directory to put resource artifacts
 *  -s, --source=&lt;directory&gt;    The directory to put Java source
 *  -q, --quiet                 Be somewhat more quiet
 *  -t, --show-traces           Show full exception stack traces
 *  -l, --load-provider           Load the provider and exit (debug utility)
 * </pre>
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class WSProvide
{
   private boolean generateSource = false;
   private boolean generateWsdl = false;
   private boolean quiet = false;
   private boolean showTraces = false;
   private boolean loadProvider = false;
   private ClassLoader loader = Thread.currentThread().getContextClassLoader();
   private File outputDir = new File("output");
   private File resourceDir = null;
   private File sourceDir = null;
   
   public static String PROGRAM_NAME = System.getProperty("program.name", WSProvide.class.getSimpleName());

   public static void main(String[] args)
   {
      WSProvide generate = new WSProvide();
      String endpoint = generate.parseArguments(args);
      System.exit(generate.generate(endpoint));
   }
   
   private String parseArguments(String[] args)
   {
      String shortOpts = "hwko:r:s:c:qtl";
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
      if (!JavaUtils.isLoaded(endpoint, loader))
      {
         System.err.println("Error: Could not load class [" + endpoint + "]. Did you specify a valid --classpath?");
         return 1;
      }
      
      WSContractProvider gen = WSContractProvider.newInstance(loader);
      gen.setGenerateWsdl(generateWsdl);
      gen.setGenerateSource(generateSource);
      gen.setOutputDirectory(outputDir);
      if (resourceDir != null)
         gen.setResourceDirectory(resourceDir);
      if (sourceDir != null)
         gen.setSourceDirectory(sourceDir);

      if (! quiet)
         gen.setMessageStream(System.out);
      
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
            urls.add(new File(entry).toURL());
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
      out.println("    -q, --quiet                 Be somewhat more quiet");
      out.println("    -t, --show-traces           Show full exception stack traces");
		out.println("    -l, --load-provider         Load the provider and exit (debug utility)");
		out.flush();
   }
}
