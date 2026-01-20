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
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * sensor
 */
public class SensorsExtraUseCase extends AbstractExtraUseCaseDto<Sensor, Object> {

    private final IDtoMemoryCache<Sensor> cacheSensor;

    @SuppressWarnings("unchecked")
    public SensorsExtraUseCase(Providers providers) {
        super(providers);
        cacheSensor = resolve(providers, IDtoMemoryCache.class, Sensor.class);
    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<Sensor> request) {
        Sensor sensor = request.model();
        checkRequireField(request);
        String sensorId = request.id();
        Sensor createdSensor = new Sensor(null, sensorId, sensor.name(), sensor.description(), sensor.encodingType(),
                sensor.metadata(), sensor.properties(), null);
        cacheSensor.addDto(sensorId, createdSensor);
        return new ExtraUseCaseResponse<Object>(sensorId, createdSensor);

    }

    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<Sensor> request) {

        if (cacheSensor.getDto(request.id()) != null) {
            cacheSensor.removeDto(request.id());
            return new ExtraUseCaseResponse<Object>(true, "sensor deleted");

        } else {
            throw new WebApplicationException("Sensor is mandatory for Datastream", Response.Status.CONFLICT);
        }

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<Sensor> request) {
        String providerId = DtoToModelMapper.extractFirstIdSegment(request.id());
        String sensorId = DtoToModelMapper.extractSecondIdSegment(request.id());
        if (providerId == null || sensorId == null) {
            throw new BadRequestException("bad id format");
        }
        Sensor receivedSensor = request.model();
        checkRequireField(request);
        Sensor sensorToUpdate = new Sensor(null, sensorId, receivedSensor.name(), receivedSensor.description(),
                receivedSensor.encodingType(), receivedSensor.metadata(), receivedSensor.properties(), null);
        return List.of(DtoToModelMapper.toDatastreamUpdate(request.mapper(), providerId,
                getObservedArea(request.session(), providerId), null, null, sensorToUpdate, null, null, null, null));

    }

    public ExtraUseCaseResponse<Object> update(ExtraUseCaseRequest<Sensor> request) {
        // check if sensor is in cached map
        Sensor sensor = getInMemorySensor(request.id());
        if (sensor != null) {
            Sensor createdSensor = updateInMemorySensor(request, sensor);
            return new ExtraUseCaseResponse<Object>(request.id(), createdSensor);
        } else {
            String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());

            List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

            // update/create provider
            try {
                dataUpdate.pushUpdate(listDtoModels).getValue();

            } catch (InvocationTargetException | InterruptedException e) {
                throw new InternalServerErrorException(e);
            }
            ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), providerId, "datastream");
            if (serviceSnapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            return new ExtraUseCaseResponse<Object>(request.id(), serviceSnapshot);

        }

    }

    private Sensor updateInMemorySensor(ExtraUseCaseRequest<Sensor> request, Sensor sensor) {
        Sensor updateSensor = request.model();
        Sensor createdSensor = new Sensor(null, request.id(),
                updateSensor.name() != null ? updateSensor.name() : sensor.name(),
                updateSensor.description() != null ? updateSensor.description() : sensor.description(),
                updateSensor.encodingType() != null ? updateSensor.encodingType() : sensor.encodingType(),
                updateSensor.metadata() != null ? updateSensor.metadata() : sensor.metadata(),
                updateSensor.properties() != null ? updateSensor.properties() : sensor.properties(), null);
        cacheSensor.addDto(request.id(), createdSensor);
        return createdSensor;
    }

    public Sensor getInMemorySensor(String id) {
        return cacheSensor.getDto(id);
    }

    public void removeInMemorySensor(String id) {
        cacheSensor.removeDto(id);
    }

}
