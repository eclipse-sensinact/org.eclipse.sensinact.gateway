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

import org.eclipse.sensinact.model.core.provider.Service;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;

public class ServiceSnapshotImpl extends AbstractSnapshot implements ServiceSnapshot {

    /**
     * List of service resources
     */
    private final List<ResourceSnapshotImpl> resources = new ArrayList<>();

    /**
     * Parent provider
     */
    private final ProviderSnapshotImpl provider;

    /**
     * Model service
     */
    private final Service modelService;

    public ServiceSnapshotImpl(final ProviderSnapshotImpl parent, final String serviceName, Service modelService,
            Instant timestamp) {
        super(serviceName, timestamp);
        this.provider = parent;
        this.modelService = modelService;
    }

    @Override
    public String toString() {
        final ProviderSnapshot provider = getProvider();
        return String.format("ServiceSnapshot(%s/%s/%s, %s)", provider.getModelName(), provider.getName(), getName(),
                getSnapshotTime());
    }

    public ProviderSnapshotImpl getProvider() {
        return provider;
    }

    public Service getModelService() {
        return modelService;
    }

    public void add(final ResourceSnapshotImpl rc) {
        resources.add(rc);
    }

    public List<ResourceSnapshotImpl> getResources() {
        return List.copyOf(resources);
    }
}
