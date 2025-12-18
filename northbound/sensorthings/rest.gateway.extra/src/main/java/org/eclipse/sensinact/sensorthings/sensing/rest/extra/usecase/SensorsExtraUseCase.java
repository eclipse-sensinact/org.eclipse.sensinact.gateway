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

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * sensor
 */
public class SensorsExtraUseCase extends AbstractExtraUseCase<ExpandedSensor, ExpandedSensor> {

    Map<String, ExpandedSensor> sensorById = new HashMap<String, ExpandedSensor>();

    public SensorsExtraUseCase(Providers providers) { }

    public ExtraUseCaseResponse<ExpandedSensor> create(ExtraUseCaseRequest<ExpandedSensor> request) {
        ExpandedSensor sensor = request.model();
        DtoToModelMapper.checkRequireField(sensor);
        String observedPropertyId = getId(sensor);
        ExpandedSensor createdSensor = new ExpandedSensor(null, observedPropertyId, sensor.name(), sensor.description(),
                sensor.encodingType(), sensor.metadata(), sensor.properties(), null);
        sensorById.put(observedPropertyId, createdSensor);

        return new ExtraUseCaseResponse<ExpandedSensor>(observedPropertyId, createdSensor);

    }

    public ExtraUseCaseResponse<ExpandedSensor> delete(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ExpandedSensor>(false, "not implemented");

    }

    public ExtraUseCaseResponse<ExpandedSensor> patch(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ExpandedSensor>(false, "not implemented");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedSensor> request) {
        return null;
    }

    public ExtraUseCaseResponse<ExpandedSensor> update(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<ExpandedSensor>(false, "not implemented");

    }

    @Override
    public String getId(ExpandedSensor dto) {
        return DtoToModelMapper.sanitizeId(dto.id() != null ? dto.id() : dto.name());
    }

    public ExpandedSensor getInMemorySensor(String id) {
        return sensorById.get(id);
    }

    public ExpandedSensor removeInMemorySensor(String id) {
        return sensorById.remove(id);
    }

}
