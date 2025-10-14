/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.filters.sensorthings;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * Utility methods for tests
 */
public class RcUtils {

    private static final class TestServiceSnapshot implements ServiceSnapshot {
        private final TestProviderSnapshot provider;
        private final String svcName;
        final List<ResourceSnapshot> resources = new ArrayList<>();

        private TestServiceSnapshot(TestProviderSnapshot provider, String svcName) {
            this.provider = provider;
            this.svcName = svcName;
        }

        @Override
        public Instant getSnapshotTime() {
            return Instant.now();
        }

        @Override
        public String getName() {
            return svcName;
        }

        @Override
        public String toString() {
            return provider.toString() + "/" + svcName;
        }

        @Override
        public List<ResourceSnapshot> getResources() {
            return resources;
        }

        @Override
        public ProviderSnapshot getProvider() {
            return provider;
        }

        @Override
        public ResourceSnapshot getResource(String name) {
            return resources.stream().filter(r -> r.getName().equals(name)).findFirst().get();
        }
    }

    private static final class TestProviderSnapshot implements ProviderSnapshot {
        private final String providerName;
        private final String modelName;
        final List<ServiceSnapshot> services = new ArrayList<>();

        private TestProviderSnapshot(String providerName, String modelName) {
            this.providerName = providerName;
            this.modelName = modelName;
        }

        @Override
        public Instant getSnapshotTime() {
            return Instant.now();
        }

        @Override
        public String getModelPackageUri() {
            return "https://eclipse.org/sensinact/test/";
        }

        @Override
        public String getName() {
            return providerName;
        }

        @Override
        public String toString() {
            return providerName;
        }

        @Override
        public List<ServiceSnapshot> getServices() {
            return services;
        }

        @Override
        public String getModelName() {
            return modelName;
        }

        @Override
        public ServiceSnapshot getService(String name) {
            return services.stream().filter(s -> s.getName().equals(name)).findFirst().get();
        }

        @Override
        public ResourceSnapshot getResource(String service, String resource) {
            return getService(service).getResource(resource);
        }

        @Override
        public List<LinkedProviderSnapshot> getLinkedProviders() {
            // Linked providers are not supported in the tests
            return null;
        }
    }

    static ProviderSnapshot makeProvider(final String providerName) {
        return makeProvider(providerName, providerName);
    }

    static ProviderSnapshot makeProvider(final String modelName, final String providerName) {
        return new TestProviderSnapshot(providerName, modelName);
    }

    static TestServiceSnapshot addService(final ProviderSnapshot provider, final String svcName) {
        TestProviderSnapshot test = (TestProviderSnapshot) provider;
        TestServiceSnapshot svc = new TestServiceSnapshot(test, svcName);
        test.getServices().add(svc);
        return svc;
    }

    static ResourceSnapshot addResource(ServiceSnapshot svc, final String rcName, final Object value) {
        return addResource(svc, rcName, value, Instant.now());
    }

    static ResourceSnapshot addResource(ServiceSnapshot svc, final String rcName, final Object value,
            final Instant rcTime) {
        TestServiceSnapshot test = (TestServiceSnapshot) svc;
        ResourceSnapshot rc = new ResourceSnapshot() {
            @Override
            public String getName() {
                return rcName;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return Map.of();
            }

            @Override
            public String toString() {
                return test.toString() + "/" + rcName;
            }

            @Override
            public TimedValue<?> getValue() {
                return new DefaultTimedValue<>(value, rcTime);
            }

            @Override
            public Instant getSnapshotTime() {
                return rcTime;
            }

            @Override
            public ServiceSnapshot getService() {
                return test;
            }

            @Override
            public ResourceType getResourceType() {
                return ResourceType.PROPERTY;
            }

            @Override
            public ValueType getValueType() {
                return ValueType.UPDATABLE;
            }

            @Override
            public boolean isSet() {
                return true;
            }

            @Override
            public Class<?> getType() {
                return Object.class;
            }
        };

        test.getResources().add(rc);
        return rc;
    }
}
