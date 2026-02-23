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

import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

/**
 * A default immutable representation of a provider snapshot
 */
public record ImmutableLinkedProviderSnapshot(String modelPackageUri, String model, String name,
        String friendlyName, String description, String icon, GeoJsonObject location, Instant snapshotTime) implements LinkedProviderSnapshot {

    ImmutableLinkedProviderSnapshot(LinkedProviderSnapshot lp) {
        this(lp.getModelPackageUri(), lp.getModelName(), lp.getName(), lp.getFriendlyName(),
                lp.getDescription(), lp.getIcon(), lp.getLocation(), lp.getSnapshotTime());
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
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public GeoJsonObject getLocation() {
        return location;
    }
}
