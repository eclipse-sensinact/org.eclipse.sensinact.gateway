/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.sensinact.sensorthings.sensing.rest.snapshot;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;

public class HistoricalLocationResourceSnapshot implements ResourceSnapshot {

    private final ServiceSnapshot service;
    private final ResourceSnapshot resource;
    private final TimedValue<?> obs;

    public HistoricalLocationResourceSnapshot(ProviderSnapshot provider, TimedValue<?> obs) {
        this.service = new GenericServiceSnapshot(provider.getService("admin"), this);
        this.resource = provider.getResource("admin", "location");
        this.obs = obs;
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
        return obs;
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
}
