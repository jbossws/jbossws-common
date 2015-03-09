/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.ws.common;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.TRACE;
import static org.jboss.logging.Logger.Level.WARN;

import java.net.URL;

import javax.management.ObjectName;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.jboss.wsf.spi.deployment.EndpointState;

/**
 * JBossWS Common log messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageLogger(projectCode = "JBWS")
public interface Loggers extends BasicLogger
{
    Loggers ROOT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.common");
    Loggers MONITORING_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.common.monitoring");
    Loggers MANAGEMENT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.common.management");
    Loggers DEPLOYMENT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.common.deployment");
    
    @LogMessage(level = ERROR)
    @Message(id = 22001, value = "Cannot get children for resource %s")
    void cannotGetChildrenForResource(@Cause Throwable cause, URL url);
    
    @LogMessage(level = ERROR)
    @Message(id = 22002, value = "Cannot get name for resource %s")
    void cannotGetNameForResource(@Cause Throwable cause, URL url);
    
    @LogMessage(level = TRACE)
    @Message(id = 22010, value = "Class %s is not assignable from %s due to conflicting classloaders: %s and %s")
    void notAssignableDueToConflictingClassLoaders(Class<?> dest, Class<?> src, ClassLoader destClassLoader, ClassLoader srcClassLoader);
    
    @LogMessage(level = TRACE)
    @Message(id = 22011, value = "Could not clear blacklist for classloader %s")
    void couldNotClearBlacklist(ClassLoader cl, @Cause Throwable cause);
    
    @LogMessage(level = DEBUG)
    @Message(id = 22012, value = "Could not load %s")
    void couldNotLoad(String className);
    
    @LogMessage(level = ERROR)
    @Message(id = 22013, value = "Cannot parse: %s")
    void cannotParse(String s);
    
    @LogMessage(level = ERROR)
    @Message(id = 22021, value = "Cannot read resource: %s")
    void cannotReadResource(String s, @Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 22022, value = "Cannot load ID '%s' as URL (protocol = %s)")
    void cannotLoadIDAsURL(String id, String protocol);
    
    @LogMessage(level = DEBUG)
    @Message(id = 22025, value = "WSDL import published to %s")
    void wsdlImportPublishedTo(URL url);
    
    @LogMessage(level = DEBUG)
    @Message(id = 22026, value = "XMLSchema import published to %s")
    void xmlSchemaImportPublishedTo(URL url);
    
    @LogMessage(level = WARN)
    @Message(id = 22027, value = "Cannot delete published wsdl document: %s")
    void cannotDeletePublishedWsdlDoc(URL url);
    
//    @LogMessage(level = ERROR)
//    @Message(id = 22038, value = "Cannot register endpoint %s with JMX server")
//    void cannotRegisterEndpointWithJmxServer(ObjectName endpointName, @Cause Throwable cause);
//    
//    @LogMessage(level = ERROR)
//    @Message(id = 22039, value = "Cannot unregister endpoint %s with JMX server")
//    void cannotUnregisterEndpointWithJmxServer(ObjectName endpointName, @Cause Throwable cause);
//    
//    @LogMessage(level = WARN)
//    @Message(id = 22040, value = "MBeanServer not available, cannot unregister endpoint with JMX server")
//    void cannotUnregisterDueToMBeanServerUnavailable();
//    
//    @LogMessage(level = DEBUG)
//    @Message(id = 22041, value = "Destroying service endpoint manager")
//    void destroyingServiceEndpointManager();
    
    @LogMessage(level = DEBUG)
    @Message(id = 22042, value = "Cannot register processor %s with JMX server, will be trying using the default managed implementation. ")
    void cannotRegisterProvidedProcessor(ObjectName processorName, @Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 22043, value = "Cannot register processor %s with JMX server")
    void cannotRegisterProcessorWithJmxServer(ObjectName processorName, @Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 22044, value = "Cannot unregister processor %s with JMX server")
    void cannotUnregisterProcessorWithJmxServer(ObjectName processorName, @Cause Throwable cause);
        
//    @LogMessage(level = INFO)
//    @Message(id = 22050, value = "Endpoint registered: %s")
//    void endpointRegistered(ObjectName epName);
//        
//    @LogMessage(level = INFO)
//    @Message(id = 22051, value = "Endpoint unregistered: %s")
//    void endpointUnregistered(ObjectName epName);
        
    @LogMessage(level = INFO)
    @Message(id = 22052, value = "Starting %s %s")
    void startingWSServerConfig(String implTitle, String implVersion);
        
    @LogMessage(level = DEBUG)
    @Message(id = 22053, value = "Unable to calculate webservices port, using default %s")
    void unableToCalculateWebServicesPort(String def);
        
    @LogMessage(level = DEBUG)
    @Message(id = 22054, value = "Unable to calculate webservices secure port, using default %s")
    void unableToCalculateWebServicesSecurePort(String def);
        
    @LogMessage(level = DEBUG)
    @Message(id = 22055, value = "Using undefined webservices host: %s")
    void usingUndefinedWebServicesHost(String host);
        
    @LogMessage(level = DEBUG)
    @Message(id = 22056, value = "Setting webservices host to localhost: %s")
    void usingLocalHostWebServicesHost(String host);
        
    @LogMessage(level = DEBUG)
    @Message(id = 22057, value = "Could not get address for host: %s")
    void couldNotGetAddressForHost(String host, @Cause Throwable cause);
        
    @LogMessage(level = WARN)
    @Message(id = 22058, value = "Could not get port for webservices configuration from configured HTTP connector")
    void couldNotGetPortFromConfiguredHTTPConnector();
        
    @LogMessage(level = WARN)
    @Message(id = 22059, value = "Unable to read from the http servlet request")
    void unableToReadFromHttpServletRequest(@Cause Throwable cause);
        
    @LogMessage(level = ERROR)
    @Message(id = 22060, value = "Cannot trace SOAP message")
    void cannotTraceSoapMessage(@Cause Throwable cause);
        
    @LogMessage(level = WARN)
    @Message(id = 22061, value = "Method invocation failed with exception")
    void methodInvocationFailed(@Cause Throwable cause);
        
    @LogMessage(level = TRACE)
    @Message(id = 22090, value = "Cannot get %s from root file, trying with additional metadata files")
    void cannotGetRootFileTryingWithAdditionalMetaData(String resourcePath);
        
    @LogMessage(level = TRACE)
    @Message(id = 22098, value = "Cannot get %s from %s")
    void cannotGetRootResourceFrom(String resourcePath, Object uvf, @Cause Throwable cause);
        
    @LogMessage(level = DEBUG)
    @Message(id = 22099, value = "Error during deployment: %s")
    void errorDuringDeployment(String dep, @Cause Throwable cause);
        
    @LogMessage(level = ERROR)
    @Message(id = 22100, value = "Error while destroying deployment %s due to previous exception")
    void errorDestroyingDeployment(String dep, @Cause Throwable cause);
        
    @LogMessage(level = ERROR)
    @Message(id = 22102, value = "Cannot stop endpoint in state %s: %s")
    void cannotStopEndpoint(EndpointState state, ObjectName epName);
        
    @LogMessage(level = ERROR)
    @Message(id = 22103, value = "Cannot start endpoint in state %s: %s")
    void cannotStartEndpoint(EndpointState state, ObjectName epName);
        
    @LogMessage(level = WARN)
    @Message(id = 22110, value = "Could not add handler %s as part of client or endpoint configuration")
    void cannotAddHandler(String className, @Cause Throwable cause);
        
    @LogMessage(level = WARN)
    @Message(id = 22111, value = "PortNamePattern and ServiceNamePattern filters not supported; adding handlers anyway")
    void filtersNotSupported();
    
    @LogMessage(level = WARN)
    @Message(id = 22112, value = "Init params not supported; adding handler anyway")
    void initParamsNotSupported();
    
    @LogMessage(level = ERROR)
    @Message(id = 22113, value = "Error closing JAXBIntro configuration stream: %s")
    void errorClosingJAXBIntroConf(URL url, @Cause Throwable cause);
    
    @LogMessage(level = TRACE)
    @Message(id = 22114, value = "%s doesn't work on %s")
    void aspectDoesNotWorkOnDeployment(Class<?> aspect, Class<?> deployment);
    
    @LogMessage(level = TRACE)
    @Message(id = 22115, value = "Cannot get URL for %s")
    void cannotGetURLFor(String path);
    
    @LogMessage(level = TRACE)
    @Message(id = 22116, value = "Could not find %s in the additional metadatafiles")
    void cannotFindInAdditionalMetaData(String resourcePath);
    
    @LogMessage(level = WARN)
    @Message(id = 22118, value = "Cannot obtain host for vituralHost %s, use default host")
    void cannotObtainHost(String host);
    
    @LogMessage(level = WARN)
    @Message(id = 22119, value = "Cannot obtain port for vituralHost %s, use default port")
    void cannotObtainPort(String host);
}
