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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.Provider;

public class LinkedProviderSnapshotImpl extends AbstractSnapshot implements LinkedProviderSnapshot {

    /**
     * Provider model package URI
     */
    private final String modelPackageUri;

    /**
     * Provider model name
     */
    private final String modelName;

    private final String friendlyName;
    private final String description;
    private final String icon;
    private final GeoJsonObject location;

    /**
     * @param modelPackageUri Provider model package URI
     * @param modelName       Provider model name
     * @param provider        Provider model
     * @param snapshotInstant Instant of snapshot
     */
    public LinkedProviderSnapshotImpl(final Provider provider,
            final Instant snapshotInstant, boolean includeAdmin) {
        super(provider.getId(), snapshotInstant);
        EClass eClass = provider.eClass();
        this.modelPackageUri = eClass.getEPackage().getNsURI();
        this.modelName = EMFUtil.getModelName(eClass);
        if(includeAdmin) {
            Admin admin = provider.getAdmin();
            this.friendlyName = admin.getFriendlyName();
            this.description = admin.getDescription();
            // Null for now until made part of the model, or some other way to get it?
            this.icon = null;
            this.location = admin.getLocation();
        } else {
            this.friendlyName = null;
            this.description = null;
            this.icon = null;
            this.location = null;
        }
    }

    @Override
    public String toString() {
        return String.format("LinkedProviderSnapshot(%s/%s/%s, %s)", modelPackageUri, modelName, getName(),
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
