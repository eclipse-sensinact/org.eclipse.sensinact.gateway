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

package org.eclipse.sensinact.northbound.session.snapshot;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * A default immutable representation of a provider snapshot
 */
record ImmutableResourceSnapshot(ServiceSnapshot service, String name, ValueType valueType, ResourceType resourceType,
        Class<?> type, TimedValue<?> value, Map<String, Object> metadata, List<Entry<String, Class<?>>> arguments,
        boolean multiple) implements ResourceSnapshot {

    ImmutableResourceSnapshot {
        // We must be careful as <code>null</code> is a valid metadata value
        metadata = metadata == null ? Map.of() :
            metadata.entrySet().stream().anyMatch(e -> e.getValue() == null || e.getKey() == null) ?
            Collections.unmodifiableMap(new HashMap<>(metadata)) : Map.copyOf(metadata);
    }

    ImmutableResourceSnapshot(ImmutableServiceSnapshot ss, ResourceSnapshot r) {
        this(ss, r.getName(), r.getValueType(), r.getResourceType(), r.getType(), r.getValue(), r.getMetadata(), r.getArguments(), r.isMultiple());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Instant getSnapshotTime() {
        return service.getSnapshotTime();
    }

    @Override
    public ServiceSnapshot getService() {
        return service;
    }

    @Override
    public boolean isSet() {
        return value != null && !value.isEmpty();
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public TimedValue<?> getValue() {
        return value;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public ResourceType getResourceType() {
        return resourceType;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public List<Entry<String, Class<?>>> getArguments() {
        return arguments;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }
}
