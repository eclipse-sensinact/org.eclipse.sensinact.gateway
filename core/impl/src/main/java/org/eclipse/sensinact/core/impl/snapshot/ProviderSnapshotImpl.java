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

package org.eclipse.sensinact.core.impl.snapshot;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.model.core.provider.Provider;

public class ProviderSnapshotImpl extends AbstractSnapshot implements ProviderSnapshot {

    /**
     * List of provider services
     */
    private final Map<String, ServiceSnapshotImpl> services = new LinkedHashMap<>();

    /**
     * Provider model package URI
     */
    private final String modelPackageUri;

    /**
     * Provider model name
     */
    private final String modelName;

    /**
     * Provider model
     */
    private Provider modelProvider;

    /**
     * @param modelPackageUri Provider model package URI
     * @param modelName       Provider model name
     * @param provider        Provider model
     * @param snapshotInstant Instant of snapshot
     */
    public ProviderSnapshotImpl(final String modelPackageUri, final String modelName, final Provider provider,
            final Instant snapshotInstant) {
        super(provider.getId(), snapshotInstant);
        this.modelPackageUri = modelPackageUri;
        this.modelName = modelName;
        this.modelProvider = provider;
    }

    @Override
    public String toString() {
        return String.format("ProviderSnapshot(%s/%s/%s, %s)", modelPackageUri, modelName, getName(),
                getSnapshotTime());
    }

    @Override
    public String getModelPackageUri() {
        return modelPackageUri;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void add(final ServiceSnapshotImpl svc) {
        this.services.put(svc.getName(), svc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ServiceSnapshotImpl> getServices() {
        return List.copyOf(services.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceSnapshotImpl getService(String name) {
        return services.get(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResourceSnapshotImpl getResource(String service, String resource) {
        ServiceSnapshotImpl svc = services.get(service);
        return svc != null ? svc.getResource(resource) : null;
    }

    public Provider getModelProvider() {
        return modelProvider;
    }

    public void filterEmptyServices() {
        Map<String, ServiceSnapshotImpl> filtered = services.entrySet().stream()
                .filter(e -> !e.getValue().getResources().isEmpty())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        services.clear();
        services.putAll(filtered);
    }
}
