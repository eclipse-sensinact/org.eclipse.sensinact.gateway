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

package org.eclipse.sensinact.prototype.impl.snapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.model.core.Provider;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;

public class ProviderSnapshotImpl extends AbstractSnapshot implements ProviderSnapshot {

    /**
     * List of provider services
     */
    private final List<ServiceSnapshotImpl> services = new ArrayList<>();

    /**
     * Provider model name
     */
    private final String modelName;

    /**
     * Provider model
     */
    private Provider modelProvider;

    /**
     * @param modelName       Provider model name
     * @param provider        Provider model
     * @param snapshotInstant Instant of snapshot
     */
    public ProviderSnapshotImpl(final String modelName, final Provider provider, final Instant snapshotInstant) {
        super(provider.getId(), snapshotInstant);
        this.modelName = modelName;
        this.modelProvider = provider;
    }

    @Override
    public String toString() {
        return String.format("ProviderSnapshot(%s/%s, %s)", modelName, getName(), getSnapshotTime());
    }

    public String getModelName() {
        return modelName;
    }

    public void add(final ServiceSnapshotImpl svc) {
        this.services.add(svc);
    }

    public List<ServiceSnapshotImpl> getServices() {
        return services;
    }

    public Provider getModelProvider() {
        return modelProvider;
    }

    public void filterEmptyServices() {
        final List<ServiceSnapshotImpl> filteredList = services.stream().filter(s -> !s.getResources().isEmpty())
                .collect(Collectors.toList());
        services.clear();
        services.addAll(filteredList);
    }
}
