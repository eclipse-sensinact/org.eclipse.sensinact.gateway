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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observedProperty
 */
public class ObservedPropertiesExtraUseCase
        extends AbstractExtraUseCase<ExpandedObservedProperty, ExpandedObservedProperty> {

    Map<String, ExpandedObservedProperty> observedPropertyById = new HashMap<String, ExpandedObservedProperty>();

    public ObservedPropertiesExtraUseCase(Providers providers) {
    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> create(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        ExpandedObservedProperty observedProperty = request.model();
        checkRequireField(request);
        String observedPropertyId = getId(request);
        ExpandedObservedProperty createExpandedProperty = new ExpandedObservedProperty(null, observedPropertyId,
                observedProperty.name(), observedProperty.description(), observedProperty.definition(),
                observedProperty.properties(), null);
        observedPropertyById.put(observedPropertyId, createExpandedProperty);

        return new ExtraUseCaseResponse<ExpandedObservedProperty>(observedPropertyId, createExpandedProperty);

    }

    @Override
    public String getId(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return request.id() != null ? request.id()
                : DtoToModelMapper
                        .sanitizeId(request.model().id() != null ? request.model().id() : request.model().name());
    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> delete(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(false, "not implemented");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return null;
    }

    public ExtraUseCaseResponse<ExpandedObservedProperty> update(
            ExtraUseCaseRequest<ExpandedObservedProperty> request) {
        return new ExtraUseCaseResponse<ExpandedObservedProperty>(false, "not implemented");

    }

    public ExpandedObservedProperty getInMemoryObservedProperty(String id) {
        return observedPropertyById.get(id);
    }

    public ExpandedObservedProperty removeInMemoryObservedProperty(String id) {
        return observedPropertyById.remove(id);
    }

}
