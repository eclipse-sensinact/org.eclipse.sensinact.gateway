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
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;

/**
 * A default immutable representation of a provider snapshot
 */
public record ImmutableServiceSnapshot(ProviderSnapshot provider, String name,
        List<ResourceSnapshot> resources) implements ServiceSnapshot {

    public ImmutableServiceSnapshot {
        resources = resources == null ? List.of() : resources.stream()
                .map(r -> r instanceof ImmutableResourceSnapshot ? r : new ImmutableResourceSnapshot(this, r))
                .toList();
    }

    public ImmutableServiceSnapshot(ImmutableProviderSnapshot ps, ServiceSnapshot s) {
        this(ps, s.getName(), s.getResources());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Instant getSnapshotTime() {
        return provider.getSnapshotTime();
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
