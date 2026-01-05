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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * sensor
 */
public class SensorsExtraUseCase extends AbstractExtraUseCaseDto<ExpandedSensor, Object> {

    private final DataUpdate dataUpdate;
    private final IAccessServiceUseCase serviceUseCase;
    private final IDtoMemoryCache<ExpandedSensor> cacheSensor;

    @SuppressWarnings("unchecked")
    public SensorsExtraUseCase(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        cacheSensor = resolve(providers, IDtoMemoryCache.class, ExpandedSensor.class);
    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<ExpandedSensor> request) {
        ExpandedSensor sensor = request.model();
        checkRequireField(request);
        String sensorId = request.id();
        ExpandedSensor createdSensor = new ExpandedSensor(null, sensorId, sensor.name(), sensor.description(),
                sensor.encodingType(), sensor.metadata(), sensor.properties(), null);
        cacheSensor.addDto(sensorId, createdSensor);
        return new ExtraUseCaseResponse<Object>(sensorId, createdSensor);

    }

    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<ExpandedSensor> request) {
        return new ExtraUseCaseResponse<Object>(false, "not implemented");

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedSensor> request) {
        String providerId = DtoToModelMapper.extractFirstIdSegment(request.id());
        String sensorId = DtoToModelMapper.extractSecondIdSegment(request.id());
        if (providerId == null || sensorId == null) {
            throw new BadRequestException("bad id format");
        }
        ExpandedSensor receivedSensor = request.model();
        checkRequireField(request);
        ExpandedSensor sensorToUpdate = new ExpandedSensor(null, sensorId, receivedSensor.name(),
                receivedSensor.description(), receivedSensor.encodingType(), receivedSensor.metadata(),
                receivedSensor.properties(), null);
        return List.of(DtoToModelMapper.toDatastreamUpdate(providerId, sensorToUpdate, null, null, null, null));

    }

    public ExtraUseCaseResponse<Object> update(ExtraUseCaseRequest<ExpandedSensor> request) {
        // check if sensor is in cached map
        ExpandedSensor sensor = getInMemorySensor(request.id());
        if (sensor != null) {
            ExpandedSensor createdSensor = updateInMemorySensor(request, sensor);
            return new ExtraUseCaseResponse<Object>(request.id(), createdSensor);
        } else {
            String providerId = UtilIds.extractFirstIdSegment(request.id());

            List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

            // update/create provider
            try {
                dataUpdate.pushUpdate(listDtoModels).getValue();

            } catch (InvocationTargetException | InterruptedException e) {
                return new ExtraUseCaseResponse<Object>(false, new InternalServerErrorException(e), e.getMessage());
            }
            ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), providerId, "datastream");
            if (serviceSnapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            return new ExtraUseCaseResponse<Object>(request.id(), serviceSnapshot);

        }

    }

    private ExpandedSensor updateInMemorySensor(ExtraUseCaseRequest<ExpandedSensor> request, ExpandedSensor sensor) {
        ExpandedSensor updateSensor = request.model();
        ExpandedSensor createdSensor = new ExpandedSensor(null, request.id(),
                updateSensor.name() != null ? updateSensor.name() : sensor.name(),
                updateSensor.description() != null ? updateSensor.description() : sensor.description(),
                updateSensor.encodingType() != null ? updateSensor.encodingType() : sensor.encodingType(),
                updateSensor.metadata() != null ? updateSensor.metadata() : sensor.metadata(),
                updateSensor.properties() != null ? updateSensor.properties() : sensor.properties(), null);
        cacheSensor.addDto(request.id(), createdSensor);
        return createdSensor;
    }

    public ExpandedSensor getInMemorySensor(String id) {
        return cacheSensor.getDto(id);
    }

    public void removeInMemorySensor(String id) {
        cacheSensor.removeDto(id);
    }

    @Override
    public List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<ExpandedSensor> request) {
        // TODO Auto-generated method stub
        return null;
    }

}
