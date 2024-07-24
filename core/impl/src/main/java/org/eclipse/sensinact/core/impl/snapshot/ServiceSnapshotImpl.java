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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.model.core.provider.Service;

public class ServiceSnapshotImpl extends AbstractSnapshot implements ServiceSnapshot {

    /**
     * List of service resources
     */
    private final Map<String, ResourceSnapshotImpl> resources = new LinkedHashMap<>();

    /**
     * Parent provider
     */
    private final ProviderSnapshotImpl provider;

    /**
     * Model service
     */
    private final EClass modelEClass;

    /**
     * Model service
     */
    private final Service modelService;

    public ServiceSnapshotImpl(final ProviderSnapshotImpl parent, final String serviceName,
            Entry<EClass, Service> modelService, Instant timestamp) {
        super(serviceName, timestamp);
        this.provider = parent;
        this.modelEClass = modelService.getKey();
        this.modelService = modelService.getValue();
    }

    @Override
    public String toString() {
        final ProviderSnapshot provider = getProvider();
        return String.format("ServiceSnapshot(%s/%s/%s, %s)", provider.getModelName(), provider.getName(), getName(),
                getSnapshotTime());
    }

    @Override
    public ProviderSnapshotImpl getProvider() {
        return provider;
    }

    public EClass getModelEClass() {
        return modelEClass;
    }

    public Service getModelService() {
        return modelService;
    }

    public void add(final ResourceSnapshotImpl rc) {
        resources.put(rc.getName(), rc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ResourceSnapshotImpl> getResources() {
        return List.copyOf(resources.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResourceSnapshotImpl getResource(String name) {
        return resources.get(name);
    }
}
