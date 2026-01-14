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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.notification.ClientActionListener;
import org.eclipse.sensinact.core.notification.ClientDataListener;
import org.eclipse.sensinact.core.notification.ClientLifecycleListener;
import org.eclipse.sensinact.core.notification.ClientMetadataListener;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.ProviderDescription;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.ResourceShortDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.ServiceDescription;

/**
 * Utility methods for tests
 */
public class RcUtils {

    private static final class TestSensinactSession implements SensiNactSession {

        @Override
        public String getSessionId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Instant getExpiry() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void extend(Duration duration) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isExpired() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void expire() {
            // TODO Auto-generated method stub

        }

        @Override
        public Map<String, List<String>> activeListeners() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String addListener(List<String> topics, ClientDataListener cdl, ClientMetadataListener cml,
                ClientLifecycleListener cll, ClientActionListener cal) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeListener(String id) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> TimedValue<T> getResourceTimedValue(String provider, String service, String resource, Class<T> clazz,
                GetLevel getLevel) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> TimedValue<List<T>> getResourceTimedMultiValue(String provider, String service, String resource,
                Class<T> clazz, GetLevel getLevel) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setResourceValue(String provider, String service, String resource, Object o) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setResourceValue(String provider, String service, String resource, Object o, Instant instant) {
            // TODO Auto-generated method stub

        }

        @Override
        public Map<String, Object> getResourceMetadata(String provider, String service, String resource) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setResourceMetadata(String provider, String service, String resource,
                Map<String, Object> metadata) {
            // TODO Auto-generated method stub

        }

        @Override
        public TimedValue<Object> getResourceMetadataValue(String provider, String service, String resource,
                String metadata) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setResourceMetadata(String provider, String service, String resource, String metadata,
                Object value) {
            // TODO Auto-generated method stub

        }

        @Override
        public Object actOnResource(String provider, String service, String resource, Map<String, Object> parameters) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ResourceDescription describeResource(String provider, String service, String resource) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ResourceShortDescription describeResourceShort(String provider, String service, String resource) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServiceDescription describeService(String provider, String service) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProviderDescription describeProvider(String provider) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProviderDescription linkProviders(String parent, String child) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProviderDescription unlinkProviders(String parent, String child) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<ProviderDescription> listProviders() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<ProviderSnapshot> filteredSnapshot(ICriterion filter) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<ProviderSnapshot> filteredSnapshot(ICriterion filter, EnumSet<SnapshotOption> snapshotOptions) {
            // TODO Auto-generated method stub
            return null;
        }

        private Map<String, ProviderSnapshot> map = new HashMap<String, ProviderSnapshot>();

        public void addProvider(String providerId, ProviderSnapshot provider) {
            map.put(providerId, provider);
        }

        @Override
        public ProviderSnapshot providerSnapshot(String provider, EnumSet<SnapshotOption> snapshotOptions) {

            return map.get(provider);
        }

        @Override
        public ServiceSnapshot serviceSnapshot(String provider, String service) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ResourceSnapshot resourceSnapshot(String provider, String service, String resource) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public UserInfo getUserInfo() {
            // TODO Auto-generated method stub
            return null;
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
            session = new TestSensinactSession();
        }
        return session;
    }

    static ProviderSnapshot makeProvider(final String providerName) {
        ProviderSnapshot p = makeProvider(providerName, providerName);
        return p;
    }

    static ProviderSnapshot makeProvider(String modelName, final String providerName) {
        ProviderSnapshot p = new TestProviderSnapshot(providerName, modelName);
        RcUtils.getSession().addProvider(providerName, p);

        return p;
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

        test.getResources().add(rc);
        return rc;
    }
}
