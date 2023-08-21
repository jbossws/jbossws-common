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
package org.jboss.ws.common.invocation;

import jakarta.xml.ws.WebServiceContext;

import org.jboss.ws.common.injection.InjectionHelper;
import org.jboss.ws.common.injection.PreDestroyHolder;
import org.jboss.ws.common.injection.ThreadLocalAwareWebServiceContext;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Reference;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;

/**
 * Handles invocations on JAXWS endpoints.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 */
public final class InvocationHandlerJAXWS extends AbstractInvocationHandlerJSE
{

   /**
    * Injects resources on target bean and calls post construct method.
    * Finally it registers target bean for predestroy phase.
    *
    * @param endpoint used for predestroy phase registration process
    * @param invocation current invocation
    */
   @Override
   public void onEndpointInstantiated(final Endpoint endpoint, final Invocation invocation)
   {
      final Object _targetBean = this.getTargetBean(invocation);
      // TODO: refactor injection to AS IL
      final Reference reference = endpoint.getInstanceProvider().getInstance(_targetBean.getClass().getName());
      final Object targetBean = reference.getValue();

      InjectionHelper.injectWebServiceContext(targetBean, ThreadLocalAwareWebServiceContext.getInstance());

      if (!reference.isInitialized())
      {
         InjectionHelper.callPostConstructMethod(targetBean);
         reference.setInitialized();
      }

      endpoint.addAttachment(PreDestroyHolder.class, new PreDestroyHolder(targetBean));
   }

   /**
    * Injects webservice context on target bean.
    *
    *  @param invocation current invocation
    */
   @Override
   public void onBeforeInvocation(final Invocation invocation)
   {
      final WebServiceContext wsContext = this.getWebServiceContext(invocation);
      ThreadLocalAwareWebServiceContext.getInstance().setMessageContext(wsContext);
   }

   /**
    * Cleanups injected webservice context on target bean.
    *
    * @param invocation current invocation
    */
   @Override
   public void onAfterInvocation(final Invocation invocation)
   {
      ThreadLocalAwareWebServiceContext.getInstance().setMessageContext(null);
   }

   /**
    * Returns WebServiceContext associated with this invocation.
    *
    * @param invocation current invocation
    * @return web service context or null if not available
    */
   private WebServiceContext getWebServiceContext(final Invocation invocation)
   {
      final InvocationContext invocationContext = invocation.getInvocationContext();

      return invocationContext.getAttachment(WebServiceContext.class);
   }

   /**
    * Returns endpoint instance associated with current invocation.
    *
    * @param invocation current invocation
    * @return target bean in invocation
    */
   private Object getTargetBean(final Invocation invocation)
   {
      final InvocationContext invocationContext = invocation.getInvocationContext();

      return invocationContext.getTargetBean();
   }

}
