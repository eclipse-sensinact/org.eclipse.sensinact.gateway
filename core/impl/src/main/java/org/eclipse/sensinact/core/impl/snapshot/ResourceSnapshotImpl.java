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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.model.impl.ResourceImpl;
import org.eclipse.sensinact.core.model.nexus.emf.EMFUtil;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.Service;

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

    /**
     * Resource type (action, property, ...)
     */
    private final ResourceType resourceType;

    /**
     * Resource value type (read-only, read-write)
     */
    private final ValueType valueType;

    public ResourceSnapshotImpl(final ServiceSnapshotImpl parent, final ETypedElement rcFeature,
            final Instant snapshotInstant) {
        super(rcFeature.getName(), snapshotInstant);
        this.service = parent;
        this.rcFeature = rcFeature;
        this.type = rcFeature.getEType().getInstanceClass();
        this.resourceType = ResourceImpl.findResourceType(rcFeature);

        // TODO: get it from the resource description
        this.valueType = ValueType.UPDATABLE;

        Service modelService = parent.getModelService();
        Metadata rcMetadata = modelService == null ? null : modelService.getMetadata().get(rcFeature);
        final Map<String, Object> rcMeta = new HashMap<>();
        rcMeta.putAll(EMFUtil.toMetadataAttributesToMap(rcMetadata, rcFeature));
        this.metadata = rcMeta;
    }

    @Override
    public String toString() {
        final ProviderSnapshot provider = service.getProvider();
        return String.format("ResourceSnapshot(%s/%s/%s/%s, %s)", provider.getModelName(), provider.getName(),
                service.getName(), getName(), getSnapshotTime());
    }

    @Override
    public ServiceSnapshotImpl getService() {
        return service;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public TimedValue<?> getValue() {
        return rcValue;
    }

    public void setValue(final TimedValue<?> value) {
        this.rcValue = value;
    }

    @Override
    public boolean isSet() {
        return getValue() != null && getValue().getTimestamp() != null;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public ETypedElement getFeature() {
        return rcFeature;
    }

    @Override
    public ResourceType getResourceType() {
        return resourceType;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public List<Entry<String, Class<?>>> getArguments() {
        return resourceType == ResourceType.ACTION ? ResourceImpl.findActionParameters((EOperation)rcFeature) : null;
    }
}
