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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

public class SnapshotFilter implements ICriterion {

    /**
     * Name of the provider to return
     */
    private final String providerName;

    /**
     * Name of the service to return
     */
    private final String serviceName;

    /**
     * Name of the resource to return
     */
    private final String resourceName;

    public SnapshotFilter(final String providerName) {
        this(providerName, null, null);
    }

    public SnapshotFilter(final String providerName, final String serviceName) {
        this(providerName, serviceName, null);
    }

    public SnapshotFilter(final String providerName, final String serviceName, final String resourceName) {
        this.providerName = providerName;
        this.serviceName = serviceName;
        this.resourceName = resourceName;
    }

    @Override
    public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
        return null;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        if (providerName != null) {
            return p -> providerName.equals(p.getName());
        }
        return null;
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        if (serviceName != null) {
            return s -> serviceName.equals(s.getName());
        }
        return null;
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        if (resourceName != null) {
            return r -> resourceName.equals(r.getName());
        }
        return null;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return null;
    }
}
