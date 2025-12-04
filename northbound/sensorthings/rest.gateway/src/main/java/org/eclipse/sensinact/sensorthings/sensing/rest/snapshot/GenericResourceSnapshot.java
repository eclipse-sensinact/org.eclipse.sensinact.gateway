/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.eclipse.sensinact.sensorthings.sensing.rest.snapshot;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * A default immutable representation of a provider snapshot
 */
class GenericProviderSnapshot implements ProviderSnapshot {

    private final String modelPackageUri;
    private final String model;
    private final String provider;
    private final List<ServiceSnapshot> services;

    public GenericProviderSnapshot(ProviderSnapshot real, GenericServiceSnapshot service) {
        this.modelPackageUri = real.getModelPackageUri();
        this.model = real.getModelName();
        this.provider = real.getName();
        this.services = List.of(service);
    }

    @Override
    public String getModelPackageUri() {
        return modelPackageUri;
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public String getName() {
        return provider;
    }

    @Override
    public Instant getSnapshotTime() {
        return services.get(0).getSnapshotTime();
    }

    @Override
    public List<ServiceSnapshot> getServices() {
        return services;
    }

    @Override
    public ServiceSnapshot getService(String name) {
        return internalGetService(name)
                .orElse(null);
    }

    private Optional<ServiceSnapshot> internalGetService(String name) {
        return services.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
    }

    @Override
    public ResourceSnapshot getResource(String service, String resource) {
        return internalGetService(service)
                .map(s -> s.getResource(resource))
                .orElse(null);
    }

    @Override
    public List<LinkedProviderSnapshot> getLinkedProviders() {
        return List.of();
    }
}
class GenericServiceSnapshot implements ServiceSnapshot {

    private final ProviderSnapshot provider;
    private final String service;
    private final List<ResourceSnapshot> resources;

    public GenericServiceSnapshot(ServiceSnapshot real, ResourceSnapshot pretend) {
        this.provider = new GenericProviderSnapshot(real.getProvider(), this);
        this.service = real.getName();
        this.resources = List.of(pretend);
    }

    @Override
    public String getName() {
        return service;
    }

    @Override
    public Instant getSnapshotTime() {
        return resources.get(0).getSnapshotTime();
    }

    @Override
    public ProviderSnapshot getProvider() {
        return provider;
    }

    @Override
    public List<ResourceSnapshot> getResources() {
        return resources;
    }

    @Override
    public ResourceSnapshot getResource(String name) {
        return resources.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
public class GenericResourceSnapshot implements ResourceSnapshot {

    private final ServiceSnapshot service;
    private final ResourceSnapshot resource;
    private final TimedValue<?> value;

    public GenericResourceSnapshot(ResourceSnapshot resource, TimedValue<?> value) {
        if(null == resource || null == value) {
            throw new IllegalArgumentException("Resource and value must not be null.");
        }
        this.service = new GenericServiceSnapshot(resource.getService(), this);
        this.resource = resource;
        this.value = value;
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public Instant getSnapshotTime() {
        return resource.getSnapshotTime();
    }

    @Override
    public ServiceSnapshot getService() {
        return service;
    }

    @Override
    public boolean isSet() {
        return resource.isSet();
    }

    @Override
    public Class<?> getType() {
        return resource.getType();
    }

    @Override
    public TimedValue<?> getValue() {
        return value;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return resource.getMetadata();
    }

    @Override
    public ResourceType getResourceType() {
        return resource.getResourceType();
    }

    @Override
    public List<Entry<String, Class<?>>> getArguments() {
        return resource.getArguments();
    }

    @Override
    public ValueType getValueType() {
        return resource.getValueType();
    }

    @Override
    public boolean isMultiple() {
        return resource.isMultiple();
    }
}
