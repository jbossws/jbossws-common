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

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.jboss.wsf.spi.tools.WSContractConsumer;
import org.jboss.wsf.spi.util.Log4JUtil;
import org.jboss.wsf.spi.util.Log4jOutputStream;

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
 *  <table>
 *  <tr><td>-h, --help                      </td><td>Show this help message</td></tr>
 *  <tr><td>-b, --binding=&lt;file&gt;      </td><td>One or more JAX-WS or JAXB binding files</td></tr>
 *  <tr><td>-k, --keep                      </td><td>Keep/Generate Java source</td></tr>
 *  <tr><td>-c, --catalog=&lt;file&gt;      </td><td>Oasis XML Catalog file for entity resolution</td></tr>
 *  <tr><td>-p, --package=&lt;name&gt;      </td><td>The target package for generated source</td></tr>
 *  <tr><td>-w, --wsdlLocation=&lt;loc&gt;  </td><td>Value to use for @@WebService.wsdlLocation</td></tr>
 *  <tr><td>-o, --output=&lt;directory&gt;  </td><td>The directory to put generated artifacts</td></tr>
 *  <tr><td>-s, --source=&lt;directory&gt;  </td><td>The directory to put Java source</td></tr>
 *  <tr><td>-t, --target=&lt;2.0|2.1|2.2&gt;</td><td>The target specification target</td></tr>
 *  <tr><td>-n, --nocompile                 </td><td>Do not compile generated sources</td></tr> 
 *  <tr><td>-q, --quiet                     </td><td>Be somewhat more quiet</td></tr>
 *  <tr><td>-v, --verbose                   </td><td>Show full exception stack traces</td></tr>
 *  <tr><td>-l, --load-consumer             </td><td>Load the consumer and exit (debug utility)</td></tr>
 *  <tr><td>-e, --extension                 </td><td>Enable SOAP 1.2 binding extension</td></tr>
 *  <tr><td>-a, --additionalHeaders         </td><td>Enable SOAP 1.2 binding extension</td></tr>
 *  </table>
 * </pre>
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class WSConsume
{
   private static final ClassLoader MODULES_LOADER = SecurityActions.getModulesClassLoader();
   private List<File> bindingFiles = new ArrayList<File>();
   private File outputDir = new File("output");
   private boolean generateSource;
   private File catalog;
   private String targetPackage;
   private String wsdlLocation;
   private boolean quiet;
   private boolean verbose;
   private boolean loadConsumer;
   private boolean extension;
   private boolean additionalHeaders;
   private boolean noCompile;
   private File sourceDir;
   private String target;
   
   public static final String PROGRAM_NAME = SecurityActions.getSystemProperty("program.name", WSConsume.class.getName());

   public static void main(String[] args)
   {
      if (MODULES_LOADER != null)
      {
         final ClassLoader origLoader = SecurityActions.getContextClassLoader();
         try
         {
            SecurityActions.setContextClassLoader(MODULES_LOADER);
            mainInternal(args);
         }
         finally
         {
            SecurityActions.setContextClassLoader(origLoader);
         }
      }
      else
      {
         mainInternal(args);
      }
   }
   
   private static void mainInternal(final String[] args)
   {
       WSConsume importer = new WSConsume();
       URL wsdl = importer.parseArguments(args);
       System.exit(importer.importServices(wsdl));
   }

   private URL parseArguments(String[] args)
   {
      String shortOpts = "b:c:p:w:o:s:t:khqvlnea";
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
         new LongOpt("nocompile", LongOpt.NO_ARGUMENT, null, 'n'),
         new LongOpt("extension", LongOpt.NO_ARGUMENT, null, 'e'),
         new LongOpt("additionalHeaders", LongOpt.NO_ARGUMENT, null, 'a'),
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
            case 'e':
               extension = true;
               break;
            case 'a':
               additionalHeaders = true;
               break;
            case 'n':
               noCompile = true;
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
            url = file.toURI().toURL();
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
      WSContractConsumer consumer = WSContractConsumer.newInstance();

      consumer.setGenerateSource(generateSource);
      consumer.setOutputDirectory(outputDir);
      consumer.setExtension(extension);
      consumer.setAdditionalHeaders(additionalHeaders);
      if (sourceDir != null)
         consumer.setSourceDirectory(sourceDir);

      if (! quiet)
      {
         PrintStream ps;
         if (Log4JUtil.isLog4jConfigurationAvailable())
         {
            ps = new PrintStream(new Log4jOutputStream(Logger.getLogger("WSConsume"), Level.INFO));
         }
         else
         {
            ps = System.out;
            ps.println("Could not find log4j.xml configuration, logging to console.\n");
         }
         consumer.setMessageStream(ps);
      }

      if (catalog != null)
      {
         if (catalog.exists() && catalog.isFile())
         {
            consumer.setCatalog(catalog);
         }
         else
         {
            System.err.println("Warning: catalog file not found: " + catalog);
         }
      }

      if (targetPackage != null)
         consumer.setTargetPackage(targetPackage);

      if (wsdlLocation != null)
         consumer.setWsdlLocation(wsdlLocation);

      if (bindingFiles != null && bindingFiles.size() > 0)
         consumer.setBindingFiles(bindingFiles);

      if(target!=null)
         consumer.setTarget(target);
      
      if (noCompile)
         consumer.setNoCompile(noCompile);

      try
      {
         consumer.consume(wsdl);
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
      out.println("    -t, --target=<2.0|2.1|2.2>  The JAX-WS specification target");
      out.println("    -q, --quiet                 Be somewhat more quiet");
      out.println("    -v, --verbose               Show full exception stack traces");
      out.println("    -l, --load-consumer         Load the consumer and exit (debug utility)");
      out.println("    -e, --extension             Enable SOAP 1.2 binding extension");
      out.println("    -a, --additionalHeaders     Enable processing of implicit SOAP headers");
      out.println("    -n, --nocompile             Do not compile generated sources");
      out.flush();
   }
}
