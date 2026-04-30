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

import static org.mockito.Answers.CALLS_REAL_METHODS;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.mockito.Answers;
import org.mockito.Mockito;

/**
 * Utility methods for tests
 */
public class RcUtils {

    private static abstract class TestSensinactSession implements SensiNactSession {

        private Map<String, ProviderSnapshot> map = new HashMap<String, ProviderSnapshot>();

        public void addProvider(String providerId, ProviderSnapshot provider) {
            map.put(providerId, provider);
        }

        @Override
        public ProviderSnapshot providerSnapshot(String provider, EnumSet<SnapshotOption> snapshotOptions) {

            return map.get(provider);
        }

    }

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
        private final String modelPackageUri;

        final List<ServiceSnapshot> services = new ArrayList<>();

        private TestProviderSnapshot(String providerName, String modelName, String modelPackageUri) {
            this.providerName = providerName;
            this.modelName = modelName;
            this.modelPackageUri = modelPackageUri;
        }

        @Override
        public Instant getSnapshotTime() {
            return Instant.now();
        }

        @Override
        public String getModelPackageUri() {
            return modelPackageUri;
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
            return services.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
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

    private static TestSensinactSession session;

    static TestSensinactSession getSession() {
        if (session == null) {
            session = Mockito.mock(TestSensinactSession.class);
            Mockito.lenient().when(session.providerSnapshot(Mockito.anyString(), Mockito.any())).then(CALLS_REAL_METHODS);
        }
        return session;
    }

    static ProviderSnapshot makeProvider(final String providerName) {
        ProviderSnapshot p = new TestProviderSnapshot(providerName, providerName,
                "https://eclipse.org/sensinact/test/");
        RcUtils.getSession().addProvider(providerName, p);

        return p;
    }

    static ProviderSnapshot makeProvider(String modelName, String modelPackageUri, final String providerName) {
        ProviderSnapshot p = new TestProviderSnapshot(providerName, modelName, modelPackageUri);
        RcUtils.getSession().addProvider(providerName, p);

        return p;
    }

    static TestServiceSnapshot addService(final ProviderSnapshot provider, final String svcName) {
        TestProviderSnapshot test = (TestProviderSnapshot) provider;
        TestServiceSnapshot svc = new TestServiceSnapshot(test, svcName);
        test.getServices().removeIf(s -> svcName.equals(s.getName()));
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

            @Override
            public List<Entry<String, Class<?>>> getArguments() {
                // Not an action resource
                return null;
            }

            @Override
            public boolean isMultiple() {
                return value instanceof List;
            }
        };
        test.getResources().removeIf(r -> rcName.equals(r.getName()));
        test.getResources().add(rc);
        return rc;
    }
}
