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
import org.jboss.wsf.spi.tools.WSContractConsumer;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * WSConsumeTask is a cmd line tool that generates portable JAX-WS artifacts
 * from a WSDL file.
 *
 * <pre>
 *  usage: WSConsumeTask [options] &lt;wsdl-url&gt;
 *  options:
 *  -h, --help                     Show this help message
 *  -b, --binding=&lt;file&gt;     One or more JAX-WS or JAXB binding files
 *  -k, --keep                     Keep/Generate Java source
 *  -c  --catalog=&lt;file&gt;     Oasis XML Catalog file for entity resolution
 *  -p  --package=&lt;name&gt;     The target package for generated source
 *  -w  --wsdlLocation=&lt;loc&gt; Value to use for @@WebService.wsdlLocation
 *  -o, --output=&lt;directory&gt; The directory to put generated artifacts
 *  -s, --source=&lt;directory&gt; The directory to put Java source
 *  -t, --target=&lt;2.0|2.1&gt;   The target specification target
 *  -q, --quiet                    Be somewhat more quiet
 *  -v, --verbose                  Show full exception stack traces
 *  -l, --load-consumer            Load the consumer and exit (debug utility)
 * </pre>
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class WSConsume
{
   private List<File> bindingFiles = new ArrayList<File>();
   private boolean generateSource = false;
   private File catalog = null;
   private String targetPackage = null;
   private String wsdlLocation = null;
   private boolean quiet = false;
   private boolean verbose = false;
   private boolean loadConsumer = false;
   private File outputDir = new File("output");
   private File sourceDir = null;
   private String target = null;

   public static String PROGRAM_NAME = System.getProperty("program.name", WSConsume.class.getName());

   public static void main(String[] args)
   {
      WSConsume importer = new WSConsume();
      URL wsdl = importer.parseArguments(args);
      System.exit(importer.importServices(wsdl));
   }

   private URL parseArguments(String[] args)
   {
      String shortOpts = "b:c:p:w:o:s:t:khqvl";
      LongOpt[] longOpts =
      {
         new LongOpt("binding", LongOpt.REQUIRED_ARGUMENT, null, 'b'),
         new LongOpt("catalog", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
         new LongOpt("package", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
         new LongOpt("wsdlLocation", LongOpt.REQUIRED_ARGUMENT, null, 'w'),
         new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
         new LongOpt("source", LongOpt.REQUIRED_ARGUMENT, null, 's'),
         new LongOpt("target", LongOpt.REQUIRED_ARGUMENT, null, 't'),
         new LongOpt("keep", LongOpt.NO_ARGUMENT, null, 'k'),
         new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
         new LongOpt("quiet", LongOpt.NO_ARGUMENT, null, 'q'),
         new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v'),
         new LongOpt("load-consumer", LongOpt.NO_ARGUMENT, null, 'l'),
      };

      Getopt getopt = new Getopt(PROGRAM_NAME, args, shortOpts, longOpts);
      int c;
      while ((c = getopt.getopt()) != -1)
      {
         switch (c)
         {
            case 'b':
               bindingFiles.add(new File(getopt.getOptarg()));
               break;
            case 'k':
               generateSource = true;
               break;
            case 'c':
               catalog = new File(getopt.getOptarg());
               break;
            case 'p':
               targetPackage = getopt.getOptarg();
               break;
            case 'w':
               wsdlLocation = getopt.getOptarg();
               break;
            case 'o':
               outputDir = new File(getopt.getOptarg());
               break;
            case 's':
               sourceDir = new File(getopt.getOptarg());
               break;
            case 't':
               target = getopt.getOptarg();
               break;
            case 'q':
               quiet = true;
               break;
            case 'v':
               verbose = true;
               break;
            case 'l':
               loadConsumer = true;
               break;
            case 'h':
               printHelp();
               System.exit(0);
            case '?':
               System.exit(1);
         }
      }

      // debug output
      if(loadConsumer)
      {
         WSContractConsumer importer = WSContractConsumer.newInstance();
         System.out.println("WSContractConsumer instance: " + importer.getClass().getCanonicalName());
         System.exit(0);
      }

      int wsdlPos = getopt.getOptind();
      if (wsdlPos >= args.length)
      {
         System.err.println("Error: WSDL URL was not specified!");
         printHelp();
         System.exit(1);
      }

      URL url = null;
      try
      {
         try
         {
            url = new URL(args[wsdlPos]);
         }
         catch (MalformedURLException e)
         {
            File file = new File(args[wsdlPos]);
            url = file.toURL();
         }
      }
      catch (MalformedURLException e)
      {
         System.err.println("Error: Invalid URI: " + args[wsdlPos]);
         System.exit(1);
      }

      return url;
   }


   private int importServices(URL wsdl)
   {
      WSContractConsumer importer = WSContractConsumer.newInstance();

      importer.setGenerateSource(generateSource);
      importer.setOutputDirectory(outputDir);
      if (sourceDir != null)
         importer.setSourceDirectory(sourceDir);

      if (! quiet)
         importer.setMessageStream(System.out);

      if (catalog != null)
         importer.setCatalog(catalog);

      if (targetPackage != null)
         importer.setTargetPackage(targetPackage);

      if (wsdlLocation != null)
         importer.setWsdlLocation(wsdlLocation);

      if (bindingFiles != null && bindingFiles.size() > 0)
         importer.setBindingFiles(bindingFiles);

      if(target!=null)
         importer.setTarget(target);

      try
      {
         importer.consume(wsdl);
         return 0;
      }
      catch (Throwable t)
      {
         System.err.println("Error: Could not import. (use --verbose to see full traces)");
         if (!verbose)
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

   private static void printHelp()
   {
      PrintStream out = System.out;
      out.println("WSConsumeTask is a cmd line tool that generates portable JAX-WS artifacts from a WSDL file.\n");
      out.println("usage: " + PROGRAM_NAME + " [options] <wsdl-url>\n");
      out.println("options: ");
      out.println("    -h, --help                  Show this help message");
      out.println("    -b, --binding=<file>        One or more JAX-WS or JAXB binding files ");
      out.println("    -k, --keep                  Keep/Generate Java source");
      out.println("    -c  --catalog=<file>        Oasis XML Catalog file for entity resolution");
      out.println("    -p  --package=<name>        The target package for generated source");
      out.println("    -w  --wsdlLocation=<loc>    Value to use for @WebService.wsdlLocation");
      out.println("    -o, --output=<directory>    The directory to put generated artifacts");
      out.println("    -s, --source=<directory>    The directory to put Java source");
      out.println("    -t, --target=<2.0|2.1>      The JAX-WS specification target");
      out.println("    -q, --quiet                 Be somewhat more quiet");
      out.println("    -v, --verbose               Show full exception stack traces");
      out.println("    -l, --load-consumer         Load the consumer and exit (debug utility)");
      out.flush();
   }
}
