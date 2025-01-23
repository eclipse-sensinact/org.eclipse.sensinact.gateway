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
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * Utility methods for tests
 */
public class RcUtils {

    static ProviderSnapshot makeProvider(final String providerName) {
        return makeProvider(providerName, providerName);
    }

    static ProviderSnapshot makeProvider(final String modelName, final String providerName) {
        return new ProviderSnapshot() {
            final List<ServiceSnapshot> services = new ArrayList<>();

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

            @SuppressWarnings("unchecked")
            @Override
            public ServiceSnapshot getService(String name) {
                return services.stream().filter(s -> s.getName().equals(name)).findFirst().get();
            }

            @SuppressWarnings("unchecked")
            @Override
            public ResourceSnapshot getResource(String service, String resource) {
                return getService(service).getResource(resource);
            }
        };
    }

    static ServiceSnapshot addService(final ProviderSnapshot provider, final String svcName) {
        ServiceSnapshot svc = new ServiceSnapshot() {
            final List<ResourceSnapshot> resources = new ArrayList<>();

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

            @SuppressWarnings("unchecked")
            @Override
            public ResourceSnapshot getResource(String name) {
                return resources.stream().filter(r -> r.getName().equals(name)).findFirst().get();
            }
        };
        provider.getServices().add(svc);
        return svc;
    }

    static ResourceSnapshot addResource(ServiceSnapshot svc, final String rcName, final Object value) {
        return addResource(svc, rcName, value, Instant.now());
    }

    static ResourceSnapshot addResource(ServiceSnapshot svc, final String rcName, final Object value,
            final Instant rcTime) {
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
                return svc.toString() + "/" + rcName;
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
                return svc;
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

        svc.getResources().add(rc);
        return rc;
    }
}
