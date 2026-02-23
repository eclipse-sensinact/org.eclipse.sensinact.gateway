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
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;

/**
 * A default immutable representation of a provider snapshot
 */
public record ImmutableProviderSnapshot(String modelPackageUri, String model, String name,
        Instant snapshotTime, List<ServiceSnapshot> services, List<LinkedProviderSnapshot> linkedProviders) implements ProviderSnapshot {

    public ImmutableProviderSnapshot {
        services = services == null ? List.of() : services.stream()
                .map(s -> s instanceof ImmutableServiceSnapshot && s.getProvider() != null ? s : new ImmutableServiceSnapshot(this, s))
                .toList();
        linkedProviders = linkedProviders == null ? List.of() : linkedProviders.stream()
                .map(lp -> lp instanceof ImmutableLinkedProviderSnapshot ? lp : new ImmutableLinkedProviderSnapshot(lp))
                .toList();
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
        return name;
    }

    @Override
    public Instant getSnapshotTime() {
        return snapshotTime;
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
        return linkedProviders;
    }

}
