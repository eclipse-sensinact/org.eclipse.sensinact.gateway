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
package org.eclipse.sensinact.core.snapshot;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.twin.TimedValue;

class ResourceDataBackedProviderSnapshot implements ProviderSnapshot {
    final ResourceDataNotification rdn;

    final ResourceDataBackedServiceSnapshot service;

    public ResourceDataBackedProviderSnapshot(ResourceDataNotification rdn) {
        this.rdn = rdn;
        this.service = new ResourceDataBackedServiceSnapshot(this);
    }

    @Override
    public String getName() {
        return rdn.provider;
    }

    @Override
    public Instant getSnapshotTime() {
        return rdn.timestamp;
    }

    @Override
    public String getModelPackageUri() {
        return rdn.modelPackageUri;
    }

    @Override
    public String getModelName() {
        return rdn.model;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ServiceSnapshot> List<T> getServices() {
        return List.of((T) service);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ServiceSnapshot> T getService(String name) {
        return Objects.equals(name, rdn.service) ? (T) service : null;
    }

    @Override
    public <T extends ResourceSnapshot> T getResource(String service, String resource) {
        return Objects.equals(service, rdn.service) ? this.service.getResource(resource) :
                null;
    }
}

class ResourceDataBackedServiceSnapshot implements ServiceSnapshot {
    final ResourceDataBackedProviderSnapshot provider;

    final ResourceDataBackedResourceSnapshot resource;

    public ResourceDataBackedServiceSnapshot(ResourceDataBackedProviderSnapshot provider) {
        this.provider = provider;
        this.resource = new ResourceDataBackedResourceSnapshot(this);
    }

    @Override
    public String getName() {
        return provider.rdn.service;
    }

    @Override
    public Instant getSnapshotTime() {
        return provider.rdn.timestamp;
    }

    @Override
    public ProviderSnapshot getProvider() {
        return provider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ResourceSnapshot> List<T> getResources() {
        return List.of((T) resource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ResourceSnapshot> T getResource(String name) {
        return Objects.equals(name, provider.rdn.resource) ? (T) resource : null;
    }

}

class ResourceDataBackedResourceSnapshot implements ResourceSnapshot {
    private final ResourceDataBackedServiceSnapshot service;

    public ResourceDataBackedResourceSnapshot(ResourceDataBackedServiceSnapshot service) {
        this.service = service;
    }

    @Override
    public String getName() {
        return service.provider.rdn.resource;
    }

    @Override
    public Instant getSnapshotTime() {
        return service.provider.rdn.timestamp;
    }

    @Override
    public ServiceSnapshot getService() {
        return service;
    }

    @Override
    public boolean isSet() {
        return service.provider.rdn.timestamp != null;
    }

    @Override
    public Class<?> getType() {
        return service.provider.rdn.type;
    }

    @Override
    public TimedValue<?> getValue() {
        return new TimedValue<Object>() {
            @Override
            public Instant getTimestamp() {
                return service.provider.rdn.timestamp;
            }

            @Override
            public Object getValue() {
                return service.provider.rdn.newValue;
            }
        };
    }

    @Override
    public Map<String, Object> getMetadata() {
        return service.provider.rdn.metadata;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.SENSOR;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.UPDATABLE;
    }

}
