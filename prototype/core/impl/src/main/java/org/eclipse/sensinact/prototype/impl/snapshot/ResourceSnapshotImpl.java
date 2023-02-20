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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.prototype.model.nexus.impl.emf.EMFUtil;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.twin.TimedValue;

public class ResourceSnapshotImpl extends AbstractSnapshot implements ResourceSnapshot {

    /**
     * Associated value
     */
    private TimedValue<?> rcValue;

    /**
     * Parent service
     */
    private final ServiceSnapshotImpl service;

    /**
     * Resource feature
     */
    private final ETypedElement rcFeature;

    /**
     * Resource metadata
     */
    private final Map<String, Object> metadata;

    /**
     * Resource content type
     */
    private final Class<?> type;

    public ResourceSnapshotImpl(final ServiceSnapshotImpl parent, final ETypedElement rcFeature,
            final Instant snapshotInstant) {
        super(rcFeature.getName(), snapshotInstant);
        this.service = parent;
        this.rcFeature = rcFeature;
        this.type = rcFeature.getEType().getInstanceClass();

        final ResourceMetadata rcMetadata = parent.getModelService().getMetadata().get(rcFeature);
        if (rcMetadata == null) {
            this.metadata = Map.of();
        } else {
            final Map<String, Object> rcMeta = new HashMap<>();
            rcMeta.putAll(EMFUtil.toEObjectAttributesToMap(rcMetadata));
            this.metadata = rcMeta;
        }
    }

    @Override
    public String toString() {
        final ProviderSnapshot provider = service.getProvider();
        return String.format("ResourceSnapshot(%s/%s/%s/%s, %s)", provider.getModelName(), provider.getName(),
                service.getName(), getName(), getSnapshotTime());
    }

    public ServiceSnapshotImpl getService() {
        return service;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public TimedValue<?> getValue() {
        return rcValue;
    }

    public void setValue(final TimedValue<?> value) {
        this.rcValue = value;
    }

    public Class<?> getType() {
        return type;
    }

    public ETypedElement getFeature() {
        return rcFeature;
    }
}
