package org.jboss.wsf.spi.management;

import java.util.Date;

import org.jboss.wsf.spi.deployment.Endpoint;

public interface EndpointMetrics
{

   Endpoint getEndpoint();

   void setEndpoint(Endpoint endpoint);

   void start();

   void stop();

   long processRequestMessage();

   void processResponseMessage(long beginTime);

   void processFaultMessage(long beginTime);

   Date getStartTime();

   Date getStopTime();

   long getMinProcessingTime();

   long getMaxProcessingTime();

   long getAverageProcessingTime();

   long getTotalProcessingTime();

   long getRequestCount();

   long getFaultCount();

   long getResponseCount();

}