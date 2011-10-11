/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
