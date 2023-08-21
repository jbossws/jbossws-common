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

import org.jboss.wsf.spi.deployment.Reference;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class ReferenceFactory {

    private ReferenceFactory() {
        // forbidden instantiation
    }

    public static Reference newInitializedReference(final Reference reference) {
        if (reference == null) throw new IllegalArgumentException();
        return newInitializedReference(reference.getValue());
    }

    public static Reference newInitializedReference(final Object reference) {
        if (reference == null) throw new IllegalArgumentException();
        return newReference(reference, true);
    }

    public static Reference newUninitializedReference(final Reference reference) {
        if (reference == null) throw new IllegalArgumentException();
        return newUninitializedReference(reference.getValue());
    }

    public static Reference newUninitializedReference(final Object reference) {
        if (reference == null) throw new IllegalArgumentException();
        return newReference(reference, false);
    }

    private static Reference newReference(final Object reference, final boolean initialized) {
        return new ReferenceImpl(reference, initialized);
    }

    private static final class ReferenceImpl implements Reference {

        private final Object reference;
        private volatile boolean initialized;
        
        private ReferenceImpl(final Object reference, final boolean initialized) {
            this.reference = reference;
            this.initialized = initialized;
        }

        @Override
        public Object getValue() {
            return reference;
        }

        @Override
        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public void setInitialized() {
            initialized = true;
        }

    }

}
