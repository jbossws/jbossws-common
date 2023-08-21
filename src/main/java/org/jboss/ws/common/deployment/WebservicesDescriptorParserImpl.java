/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.ws.common.deployment;

import java.net.URL;

import org.jboss.wsf.spi.metadata.webservices.WebservicesDescriptorParser;
import org.jboss.wsf.spi.metadata.webservices.WebservicesFactory;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;

/**
 * Webservices descriptor parser implementation.
 * 
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 * @author alessio.soldano@jboss.com
 */
public final class WebservicesDescriptorParserImpl implements WebservicesDescriptorParser
{
   private String descriptorName;
   
   @Override
   public String getDescriptorName()
   {
      return this.descriptorName;
   }

   /**
    * Invoked via MC.
    * @param descriptorName
    */
   public void setDescriptorName(final String descriptorName)
   {
      this.descriptorName = descriptorName;
   }

   @Override
   public WebservicesMetaData parse(URL url)
   {
      return new WebservicesFactory(url).load(url);
   }
}
