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

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sensinact.prototype.command.TimedValue;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;

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
     * Service feature
     */
    private final EStructuralFeature rcFeature;

    /**
     * Resource content type
     */
    private final Class<?> type;

    public ResourceSnapshotImpl(final ServiceSnapshotImpl parent, final EStructuralFeature rcFeature,
            final Instant snapshotInstant) {
        super(rcFeature.getName(), snapshotInstant);
        this.service = parent;
        this.rcFeature = rcFeature;
        this.type = rcFeature.getEType().getInstanceClass();
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

    public TimedValue<?> getValue() {
        return rcValue;
    }

    public void setValue(final TimedValue<?> value) {
        this.rcValue = value;
    }

    public Class<?> getType() {
        return type;
    }

    public EStructuralFeature getFeature() {
        return rcFeature;
    }
}
