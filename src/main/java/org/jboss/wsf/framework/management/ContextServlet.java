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
package org.jboss.wsf.framework.management;

// $Id$

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Set;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;

/**
 * The servlet that that is associated with context /jbossws
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2005
 */
public class ContextServlet extends HttpServlet
{
   // provide logging
   protected final Logger log = Logger.getLogger(ContextServlet.class);

   protected EndpointRegistry epRegistry;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();      
   }

   /** Process GET requests.
    */
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      PrintWriter writer = res.getWriter();
      res.setContentType("text/html");

      writer.print("<html>");
      setupHTMLResponseHeader(writer);

      URL requestURL = new URL(req.getRequestURL().toString());

      writer.print("<body>");

      writer.print("<div class='pageHeader'>JBossWS/Services</div>");
      writer.print("<div class='pageSection'>");
      writer.print("<fieldset>");
      writer.print("<legend><b>Registered Service Endpoints</b></legend>");
      writer.print("<table>");

      // begin iteration
      Set<ObjectName> epNames = epRegistry.getEndpoints();

      if (epNames.isEmpty())
      {
         writer.print("<tr>");
         writer.print("	<td><h3>There are currently no endpoints deployed</h3></td>");
         writer.print("</tr>");
      }

      for (ObjectName oname : epNames)
      {
         Endpoint ep = epRegistry.getEndpoint(oname);

         writer.print("<tr>");
         writer.print("	<td>Endpoint Name</td>");
         writer.print("	<td>" + ep.getName() + "</td>");
         writer.print("</tr>");
         writer.print("<tr>");
         writer.print("	<td>Endpoint Address</td>");
         writer.print("	<td><a href='" + ep.getAddress() + "?wsdl'>" + ep.getAddress() + "?wsdl</a></td>");
         writer.print("</tr>");
         writer.print("<tr>");
         writer.print("	<td colspan=2>");
         writer.print("	");
         writer.print("");

         EndpointMetrics metrics = ep.getEndpointMetrics();
         if (metrics != null)
         {
            writer.print("<table class='metrics'>");
            writer.print("<tr>");
            writer.print(" <td>StartTime</td>");
            writer.print(" <td>StopTime</td>");
            writer.print(" <td></td>");
            writer.print("</tr>");
            writer.print("<tr>");
            writer.print(" <td>" + metrics.getStartTime() + "</td>");

            String stopTime = metrics.getStopTime() != null ? metrics.getStopTime().toString() : "";
            writer.print(" <td>" + stopTime + "</td>");
            writer.print(" <td></td>");
            writer.print("</tr>");
            writer.print("<tr>");

            writer.print(" <td>RequestCount</td>");
            writer.print(" <td>ResponseCount</td>");
            writer.print(" <td>FaultCount</td>");
            writer.print("</tr>");
            writer.print("<tr>");
            writer.print(" <td>" + metrics.getRequestCount() + "</td>");
            writer.print(" <td>" + metrics.getResponseCount() + "</td>");
            writer.print(" <td>" + metrics.getFaultCount() + "</td>");
            writer.print("</tr>");
            writer.print("<tr>");
            writer.print(" <td>MinProcessingTime</td>");
            writer.print(" <td>MaxProcessingTime</td>");
            writer.print(" <td>AvgProcessingTime</td>");
            writer.print("</tr>");
            writer.print("<tr>");
            writer.print(" <td>" + metrics.getMinProcessingTime() + "</td>");
            writer.print(" <td>" + metrics.getMaxProcessingTime() + "</td>");
            writer.print(" <td>" + metrics.getAverageProcessingTime() + "</td>");
            writer.print("</tr>");
            writer.print("");
            writer.print("");
            writer.print("</table>");
            writer.print("");
         }

         writer.print("	</td>");
         writer.print("</tr>");

         writer.print("<tr><td colspan='3'>&nbsp;</td></tr>");
      }
      // end iteration
      writer.print("</table>");
      writer.print("");
      writer.print("</fieldset>");
      writer.print("</div>");

      writer.print("</body>");
      writer.print("</html>");
      writer.close();
   }

   private void setupHTMLResponseHeader(PrintWriter writer)
   {
      Package wsPackage = Package.getPackage("org.jboss.ws");
      writer.println("<head>");
      writer.println("<meta http-equiv='Content-Type content='text/html; charset=iso-8859-1'>");
      writer.println("<title>JBossWS / " + wsPackage.getImplementationVersion() + "</title>");
      writer.println("<link rel='stylesheet' href='./styles.css'>");
      writer.println("</head>");
   }
}
