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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;

import javax.management.ObjectName;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.ws.common.injection.InjectionException;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.EndpointState;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * JBossWS Common exception messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageBundle(projectCode = "JBWS")
public interface Messages {

    Messages MESSAGES = org.jboss.logging.Messages.getBundle(Messages.class);
    
    @Message(id = 22000, value = "Cannot get URL for %s")
    IOException cannotGetURLFor(String path);
    
    @Message(id = 22003, value = "UnifiedVirtualFile not initialized; could not load resources from classloader: %s")
    IllegalStateException unifiedVirtualFileNotInitialized(ClassLoader cl);
    
    @Message(id = 22004, value = "Invalid ObjectName: %s")
    Error invalidObjectName(@Cause Throwable cause, String message);
    
    @Message(id = 22005, value = "Could not get component type from array; invalid binary component for array: %s")
    IllegalArgumentException invalidBinaryComponentForArray(String arrayType);
    
    @Message(id = 22006, value = "Could not check whether class '%s' is assignable from class '%s'; both classes must not be null.")
    IllegalArgumentException cannotCheckClassIsAssignableFrom(Class<?> dest, Class<?> src);
    
    @Message(id = 22007, value = "Unable to convert DataHandler to byte[]: %s")
    WebServiceException unableToConvertDataHandler(@Cause Throwable cause, String dhName);
    
    @Message(id = 22008, value = "Cannot pretty print document and ignore white spaces at the same time")
    IllegalStateException cannotPrettyPrintAndIgnoreWhiteSpaces();
    
    @Message(id = 22009, value = "Unable to create instance of %s")
    RuntimeException unableToCreateInstanceOf(@Cause Throwable cause, String className);
    
    @Message(id = 22014, value = "Source type not implemented: %s")
    RuntimeException sourceTypeNotImplemented(Class<?> clazz);
    
    @Message(id = 22015, value = "Entity resolution: invalid character reference in %s")
    IllegalArgumentException entityResolutionInvalidCharacterReference(String string);
    
    @Message(id = 22016, value = "Entity resolution: invalid entity reference in %s")
    IllegalArgumentException entityResolutionInvalidEntityReference(String string);
    
    @Message(id = 22017, value = "Entity resolution: invalid entity %s")
    IllegalArgumentException entityResolutionInvalidEntity(String entity);
    
    @Message(id = 22018, value = "A UUID must be 16 bytes!")
    IllegalArgumentException uuidMustBeOf16Bytes();
    
    @Message(id = 22019, value = "Entity resolution: no entities mapping defined in resource file: %s")
    IllegalArgumentException entityResolutionNoEntityMapppingDefined(String file);
    
    @Message(id = 22020, value = "Entity resolution: resource not found: %s")
    IllegalArgumentException entityResolutionResourceNotFound(String res);
    
    @Message(id = 22023, value = "Error building an URLConnection capable of handling multiply-nested jars; '!' missing or in unexpected position in provided url: %s")
    MalformedURLException jarUrlConnectionBuildError(String url);
    
    @Message(id = 22024, value = "JAR URLConnection unable to locate segment %s for url %s")
    IOException jarUrlConnectionUnableToLocateSegment(String segment, String url);
    
    @Message(id = 22028, value = "Cannot find schema import '%s' in deployment '%s'")
    IllegalArgumentException cannotFindSchemaImportInDeployment(String xsdImport, String deploymentName);
    
    @Message(id = 22029, value = "Failed to provide spi: %s")
    WSFException failedToProvideSPI(Class<?> spiType);
    
    @Message(id = 22030, value = "No deployment aspect found with attribute last='true'")
    IllegalStateException noDeploymentAspectFoundWithAttributeLast();
    
    @Message(id = 22031, value = "Cycle detected in sub-graph: %s")
    IllegalStateException cycleDetectedInSubGraph(Collection<?> c);
    
    @Message(id = 22034, value = "Missing VFS root for service-ref: %s")
    IllegalStateException missingVFSRootInServiceRef(String serviceRefName);
    
    @Message(id = 22035, value = "Missing service reference type for service-ref: %s")
    IllegalStateException missingServiceRefTypeInServiceRef(String serviceRefName);
    
    @Message(id = 22036, value = "Cannot create generic javax.xml.ws.Service without wsdlLocation; service-ref metadata = '%s'")
    IllegalArgumentException cannotCreateServiceWithoutWsdlLocation(UnifiedServiceRefMetaData serviceRefMD);
    
    @Message(id = 22037, value = "Annotation class cannot be null")
    IllegalArgumentException annotationClassCannotBeNull();
    
    @Message(id = 22045, value = "Cannot register / unregister null endpoint")
    IllegalArgumentException cannotRegisterUnregisterNullEndpoint();
    
    @Message(id = 22046, value = "Looking for endpoints with null name in the endpoint registry is not supported")
    IllegalArgumentException endpointNameCannotBeNull();
    
    @Message(id = 22047, value = "Cannot register endpoint with null name: %s")
    IllegalArgumentException cannotRegisterEndpointWithNullName(ObjectName epName);
    
    @Message(id = 22048, value = "Endpoint already registered: %s")
    IllegalStateException endpointAlreadyRegistered(ObjectName endpointName);
    
    @Message(id = 22049, value = "Endpoint not registered: %s")
    IllegalStateException endpointNotRegistered(ObjectName endpointName);
    
    @Message(id = 22062, value = "Cannot find attachment %s in webservice deployment %s")
    IllegalStateException cannotFindAttachmentInDeployment(Class<?> attachmentClass, String dep);
    
    @Message(id = 22063, value = "Reference resolution: cannot resolve %s")
    IllegalArgumentException cannotResolve(Object obj);
    
    @Message(id = 22064, value = "Reference resolution: accessible object class cannot be null")
    IllegalArgumentException accessibleObjectClassCannotBeNull();
    
    @Message(id = 22065, value = "Method %s annotated with @%s can't declare primitive parameters")
    InjectionException methodCannotDeclarePrimitiveParameters2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22066, value = "Field %s can't be of primitive or void type")
    InjectionException fieldCannotBeOfPrimitiveOrVoidType(Field f);
    
    @Message(id = 22067, value = "Method %s annotated with @%s can't be of primitive or void type")
    InjectionException fieldCannotBeOfPrimitiveOrVoidType2(Field f, Class<? extends Annotation> ann);
    
    @Message(id = 22068, value = "Method %s has to have no parameters")
    InjectionException methodHasToHaveNoParameters(Method m);
    
    @Message(id = 22069, value = "Method %s annotated with @%s has to have no parameters")
    InjectionException methodHasToHaveNoParameters2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22070, value = "Method %s has to return void")
    InjectionException methodHasToReturnVoid(Method m);
    
    @Message(id = 22071, value = "Method %s annotated with @%s has to return void")
    InjectionException methodHasToReturnVoid2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22072, value = "Method %s cannot throw checked exceptions")
    InjectionException methodCannotThrowCheckedException(Method m);
    
    @Message(id = 22073, value = "Method %s annotated with @%s cannot throw checked exception")
    InjectionException methodCannotThrowCheckedException2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22074, value = "Method %s cannot be static")
    InjectionException methodCannotBeStatic(Method m);
    
    @Message(id = 22075, value = "Method %s annotated with @%s cannot be static")
    InjectionException methodCannotBeStatic2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22076, value = "Field %s cannot be static or final")
    InjectionException fieldCannotBeStaticOrFinal(Field f);
    
    @Message(id = 22077, value = "Field %s annotated with @%s cannot be static")
    InjectionException fieldCannotBeStaticOrFinal2(Field f, Class<? extends Annotation> ann);
    
    @Message(id = 22078, value = "Method %s has to declare exactly one parameter")
    InjectionException methodHasToDeclareExactlyOneParameter(Method m);
    
    @Message(id = 22079, value = "Method %s annotated with @%s has to declare exactly one parameter")
    InjectionException methodHasToDeclareExactlyOneParameter2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22080, value = "Method %s doesn't respect Java Beans setter method name")
    InjectionException methodDoesNotRespectJavaBeanSetterMethodName(Method m);
    
    @Message(id = 22081, value = "Method %s annotated with @%s doesn't respect Java Beans setter method name")
    InjectionException methodDoesNotRespectJavaBeanSetterMethodName2(Method m, Class<? extends Annotation> ann);
    
    @Message(id = 22082, value = "Only one method can exist")
    InjectionException onlyOneMethodCanExist();
    
    @Message(id = 22083, value = "Only one method annotated with @%s can exist")
    InjectionException onlyOneMethodCanExist2(Class<? extends Annotation> ann);
    
    @Message(id = 22084, value = "Method %s can't declare primitive parameters")
    InjectionException methodCannotDeclarePrimitiveParameters(Method m);
    
    @Message(id = 22085, value = "Virtual host must be the same for all endpoints of the deployment %s")
    IllegalStateException virtualHostMustBeTheSameForAllEndpoints(String dep);
    
    @Message(id = 22086, value = "Cannot obtain servlet mapping for %s")
    IllegalStateException cannotObtainServletMapping(String name);
    
    @Message(id = 22087, value = "Failed to read %s: %s")
    WebServiceException failedToRead(String descriptor, String mex, @Cause Throwable cause);
    
    @Message(id = 22088, value = "Unexpected element parsing %s: %s")
    IllegalStateException unexpectedElement(String descriptor, String elem);
    
    @Message(id = 22089, value = "Unexpectedly reached end of XML document: %s")
    IllegalStateException reachedEndOfXMLDocUnexpectedly(String descriptor);
    
    @Message(id = 22092, value = "Could not resolve %s in deployment %s")
    IOException cannotResolveResource(String resourcePath, String deploymentName);
    
    @Message(id = 22093, value = "Context root expected to start with leading slash: %s")
    IllegalStateException contextRootExpectedToStartWithLeadingSlash(String contextRoot);
    
    @Message(id = 22094, value = "Lifecycle handler not initialized for endpoint %s")
    IllegalStateException lifecycleHandlerNotInitialized(ObjectName epName);
    
    @Message(id = 22095, value = "Cannot obtain context root for deployment %s")
    IllegalStateException cannotObtainContextRoot(String dep);
    
    @Message(id = 22096, value = "Cannot obtain url pattern for endpoint %s")
    IllegalStateException cannotObtainUrlPattern(ObjectName epName);
    
    @Message(id = 22097, value = "Cannot find <url-pattern> for servlet-name %s")
    RuntimeException cannotFindUrlPatternForServletName(String s);
    
    @Message(id = 22101, value = "Invocation handler not available for endpoint %s")
    IllegalStateException invocationHandlerNotAvailable(ObjectName epName);
    
    @Message(id = 22104, value = "All endpoints must share the same context root; deployment = %s")
    IllegalStateException allEndpointsMustShareSameContextRoot(String dep);
    
    @Message(id = 22105, value = "Cannot modify endpoint in state %s: %s")
    IllegalStateException cannotModifyEndpointInState(EndpointState state, ObjectName epName);
    
    @Message(id = 22106, value = "Operation %s not supported by %s")
    RuntimeException operationNotSupportedBy(String op, Class<?> clazz);
    
    @Message(id = 22107, value = "Could not read configuration from %s")
    RuntimeException couldNotReadConfiguration(String path, @Cause Throwable cause);
    
    @Message(id = 22108, value = "Configuration %s not found")
    RuntimeException configurationNotFound(String conf);
    
    @Message(id = 22109, value = "%s is not a JAX-WS Handler")
    RuntimeException notJAXWSHandler(String className);
}
