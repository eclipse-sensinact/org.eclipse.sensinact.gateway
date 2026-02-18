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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * sensor
 */
public class SensorsExtraUseCase extends AbstractExtraUseCaseDtoDelete<Sensor, Object> {

    private final IDtoMemoryCache<Sensor> cacheSensor;
    private final IDtoMemoryCache<ExpandedObservation> obsCache;

    @SuppressWarnings("unchecked")
    public SensorsExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        cacheSensor = resolve(providers, IDtoMemoryCache.class, Sensor.class);
        obsCache = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<Sensor> request) {
        Sensor sensor = request.model();
        checkRequireField(request);
        String sensorId = request.id();
        String sensorLink = getLink(request.uriInfo(), DtoMapperSimple.VERSION, "/Sensors({id})", sensorId);
        String datastreamLink = getLink(request.uriInfo(), sensorLink, "Datastreams");
        Sensor createdSensor = new Sensor(sensorLink, sensorId, sensor.name(), sensor.description(),
                sensor.encodingType(), sensor.metadata(), sensor.properties(), datastreamLink);
        cacheSensor.addDto(sensorId, createdSensor);
        return new ExtraUseCaseResponse<Object>(sensorId, createdSensor);

    }

    @Override
    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<Sensor> request) {
        if (isIdFromCache(request.id())) {
            if (cacheSensor.getDto(request.id()) != null) {
                cacheSensor.removeDto(request.id());
                return new ExtraUseCaseResponse<Object>(true, "sensor deleted");

            }
            throw new NotFoundException();
        } else {
            return super.delete(request);
        }

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<Sensor> request) {
        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = getContextDeleteDatastreamProvider(
                request);

        return new DependentCommand<Map<String, TimedValue<?>>, List<Void>>(parentCommand) {

            @Override
            protected Promise<List<Void>> call(Promise<Map<String, TimedValue<?>>> parentResult,
                    SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                    SensinactProvider sp = twin.getProvider(providerId);
                    String observedPropertyId = (String) parentResult.getValue().get("observedPropertyId").getValue();
                    String obsStr = (String) parentResult.getValue().get("lastObservation").getValue();
                    Instant obsStamp = parentResult.getValue().get("lastObservation").getTimestamp();

                    String id = (String) parentResult.getValue().get("id").getValue();

                    if (sp != null) {
                        // check if there are still observed property and sensor
                        if (hasNoDatastreamAndObservedProperty(observedPropertyId, id)) {
                            sp.delete();
                            obsCache.removeDtoStartWith(providerId);

                        } else if (id == null) {

                            return pf.all(removeSensor(twin, providerId));
                        } else {
                            if (isHistoryMemory()) {// for TCK we remove datastream when we remove sensor (not
                                                    // compliant)
                                saveObservationHistoryMemory(obsCache, request, obsStr, obsStamp);

                                List<Promise<Void>> result = new ArrayList<Promise<Void>>();
                                result.addAll(removeDatastream(twin, providerId));
                                result.addAll(removeSensor(twin, providerId));
                                return pf.all(result);
                            } else

                                return pf.failed(new WebApplicationException(
                                        String.format("datastream %s still exists", providerId), 409));
                        }
                    }

                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

            private List<Promise<Void>> removeSensor(SensinactDigitalTwin twin, String providerId) {
                SensinactResource sensorId = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "sensorId");
                SensinactResource sensorName = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "sensorName");
                SensinactResource sensorDescription = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "sensorDescription");
                SensinactResource sensorProperties = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "sensorProperties");
                SensinactResource sensorMetadata = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "sensorMetadata");
                SensinactResource sensorEncodingType = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                        "sensorEncodingType");
                return List.of(sensorId.setValue(null), sensorName.setValue(null), sensorDescription.setValue(null),
                        sensorProperties.setValue(null), sensorMetadata.setValue(null),
                        sensorEncodingType.setValue(null));
            }

            private boolean hasNoDatastreamAndObservedProperty(String sensorId, String id) {
                return sensorId == null && id == null;
            }
        };
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
            ProviderSnapshot snapshot = providerUseCase.read(request.session(), providerId);
            if (snapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            return new ExtraUseCaseResponse<Object>(request.id(), snapshot);

        }

    }

    private Sensor updateInMemorySensor(ExtraUseCaseRequest<Sensor> request, Sensor sensor) {
        Sensor updateSensor = request.model();
        ;
        String selfLink = DtoToModelMapper.getLink(request.uriInfo(), DtoMapperSimple.VERSION, "/Sensors({id})",
                request.id());
        String datastreamLink = DtoToModelMapper.getLink(request.uriInfo(), selfLink, "/Datastream");

        Sensor createdSensor = new Sensor(selfLink, request.id(),
                updateSensor.name() != null ? updateSensor.name() : sensor.name(),
                updateSensor.description() != null ? updateSensor.description() : sensor.description(),
                updateSensor.encodingType() != null ? updateSensor.encodingType() : sensor.encodingType(),
                updateSensor.metadata() != null ? updateSensor.metadata() : sensor.metadata(),
                updateSensor.properties() != null ? updateSensor.properties() : sensor.properties(), datastreamLink);
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
