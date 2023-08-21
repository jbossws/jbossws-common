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
package org.jboss.ws.common.utils;

import org.jboss.ws.api.annotation.WebContext;
import org.jboss.wsf.spi.metadata.webservices.JBossPortComponentMetaData;

import jakarta.jws.WebService;
import java.util.StringTokenizer;

/**
 * Shared rules for ws endpoint urlPattern generation,
 * also a single point for the rules.
 *
 * User: rsearls
 * Date: 7/21/14
 */
public class UrlPatternUtils {

    public static String getUrlPatternByClassname(Class<?> beanClass){
        return beanClass.getSimpleName();
    }


    public static String getUrlPatternByWebServiceProvider(Class<?> beanClass){
        return null;
    }


    public static String getUrlPatternByWebService(Class<?> beanClass){
        String urlPattern = null;
        WebService webServiceAnnotation = (WebService)beanClass.getAnnotation(WebService.class);
        if (webServiceAnnotation != null)
        {
            String name = webServiceAnnotation.name();
            urlPattern = !isEmpty(name) ? name : beanClass.getSimpleName();
            String serviceName = webServiceAnnotation.serviceName();
            if (!isEmpty(serviceName))
            {
                urlPattern = serviceName + "/" + urlPattern;
            }
        }
        return urlPattern;
    }


    public static String getUrlPatternByWebContext(Class<?> beanClass){
        WebContext anWebContext = (WebContext)beanClass.getAnnotation(WebContext.class);
        if (anWebContext != null && anWebContext.urlPattern().length() > 0)
        {
            return anWebContext.urlPattern();
        }
        return null;
    }

    public static String getUrlPatternByPortComponentURI(String urlPattern, String contextRoot) {

        if (urlPattern != null)
        {
            urlPattern = getUrlPattern(urlPattern);

            if (contextRoot != null) {
                StringTokenizer st = new StringTokenizer(urlPattern, "/");
                if (st.countTokens() > 1 && urlPattern.startsWith(contextRoot + "/")) {
                    urlPattern = urlPattern.substring(contextRoot.length());
                }
            }
        }
        return urlPattern;
    }


    public static String getUrlPatternByPortComponentURI(JBossPortComponentMetaData portComponent) {
        String urlPattern = null;
        if (portComponent != null) {
            String portComponentURI = portComponent.getPortComponentURI();
            if (portComponentURI != null && portComponentURI.length() > 0) {
                urlPattern = portComponentURI;
            }
        }
        return urlPattern;
    }


    public static String getUrlPattern(String classBasename, String serviceName, String name) {
        String urlPattern = !isEmpty(name) ? name : classBasename;
        if (!isEmpty(serviceName)) {
            urlPattern = serviceName + "/" + urlPattern;
        }
        return urlPattern;
    }


    public static String getUrlPattern(String classBasename, String serviceName) {
        return "/" + (!serviceName.equals("") ? serviceName : classBasename);
    }


    public static String getUrlPattern(String urlPattern) {
        if (urlPattern.startsWith("/") == false)
            urlPattern = "/" + urlPattern;

        return urlPattern;
    }

    private static boolean isEmpty(final String s) {
        return s == null || s.length() == 0;
    }

}
