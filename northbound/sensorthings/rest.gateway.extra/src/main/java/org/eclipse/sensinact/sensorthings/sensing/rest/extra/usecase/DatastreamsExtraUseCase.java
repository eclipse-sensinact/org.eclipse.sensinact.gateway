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

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * datastream
 */
public class DatastreamsExtraUseCase extends AbstractExtraUseCase<ExpandedDataStream, ServiceSnapshot> {

    private final DataUpdate dataUpdate;

    private final IAccessProviderUseCase providerUseCase;

    private final IAccessServiceUseCase serviceUseCase;

    private final SensorsExtraUseCase sensorExtraUseCase;

    private final FeatureOfInterestExtraUseCase featureOfInterestUseCase;

    private final ObservedPropertiesExtraUseCase observedPropertyUseCase;

    public DatastreamsExtraUseCase(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        sensorExtraUseCase = resolveUseCase(providers, SensorsExtraUseCase.class);
        featureOfInterestUseCase = resolveUseCase(providers, FeatureOfInterestExtraUseCase.class);
        observedPropertyUseCase = resolveUseCase(providers, ObservedPropertiesExtraUseCase.class);
    }

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String id = getId(request.model());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels.get(0)).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());
        }
        String thingId = request.model().thing().id() == null ? request.parentId()
                : (String) request.model().thing().id();
        if (thingId == null) {
            throw new BadRequestException("Thing id not found");
        }
        ServiceSnapshot snapshot = serviceUseCase.read(request.session(), thingId, id);
        if (snapshot != null) {

            removeCachedExpandedObservedProperty(request.model());
            removeCachedExpandedSensor(request.model());
            if (request.model().observations() != null) {
                request.model().observations().stream()
                        .forEach(obs -> removeCachedFeatureOfInterest(obs.featureOfInterest()));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(id, snapshot);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> delete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "not implemented");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> patch(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "not implemented");

    }

    private void checkRequireLink(ProviderSnapshot thing, ExpandedSensor sensor,
            ExpandedObservedProperty observedProperty, UnitOfMeasurement unit) {

        if (thing == null) {
            throw new BadRequestException("Thing not found in Datastream Payload");
        }
        if (sensor == null) {
            throw new BadRequestException("Sensor not found in Datastream Payload");
        }
        if (observedProperty == null) {
            throw new BadRequestException("ObservedProperty  not found in Datastream Payload");
        }
        if (unit == null) {
            throw new BadRequestException("unit  not found in Datastream Payload");
        }
    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // read thing for each location and update it
        ExpandedDataStream datastream = request.model();
        DtoToModelMapper.checkRequireField(datastream);

        String providerId = request.model().thing() != null ? (String) request.model().thing().id()
                : request.parentId();
        if (providerId == null) {
            throw new BadRequestException("Thing id not found");
        }
        ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);

        ExpandedSensor sensor = getCachedExpandedSensor(datastream);
        ExpandedObservedProperty observedProperty = getCachedExpandedObservedProperty(datastream);
        UnitOfMeasurement unit = datastream.unitOfMeasurement();

        checkRequireLink(provider, sensor, observedProperty, unit);

        if (datastream.observations() != null && datastream.observations().size() > 0) {
            return datastream
                    .observations().stream().map(obs -> DtoToModelMapper.toDatastreamUpdate(providerId, datastream,
                            sensor, observedProperty, unit, obs, getCachedFeatureOfInterest(obs.featureOfInterest())))
                    .toList();
        } else {
            return List.of(DtoToModelMapper.toDatastreamUpdate(providerId, datastream, sensor, observedProperty, unit,
                    null, null));
        }

    }

    /**
     * get cache sensor if we create datastream using link sensor id or directly
     * sensor inline or null
     *
     * @param datastream
     * @return
     */
    private ExpandedSensor getCachedExpandedSensor(ExpandedDataStream datastream) {
        ExpandedSensor sensor = null;
        // retrieve created sensor
        if (datastream.sensor() != null) {
            if (DtoToModelMapper.isRecordOnlyField(datastream.sensor(), "id")) {
                String idSensor = DtoToModelMapper.getIdFromRecord(datastream.sensor());

                sensor = sensorExtraUseCase.getInMemorySensor(idSensor);
                if (sensor == null) {
                    throw new BadRequestException(String.format("sensor id %s doesn't exists", idSensor));
                }
            } else {
                sensor = datastream.sensor();
                DtoToModelMapper.checkRequireField(sensor);
            }
        }
        return sensor;
    }

    /**
     * get cache foi if we create observation using link foi id or directly foi
     * inline or null
     *
     * @param foi
     * @return
     */
    private FeatureOfInterest getCachedFeatureOfInterest(FeatureOfInterest foi) {
        FeatureOfInterest featureOfInterest = null;
        // retrieve created sensor
        if (foi != null) {
            if (DtoToModelMapper.isRecordOnlyField(foi, "id")) {

                String idFoi = DtoToModelMapper.getIdFromRecord(foi);

                featureOfInterest = featureOfInterestUseCase.getInMemoryFeatureOfInterest(idFoi);
                if (featureOfInterest == null) {
                    throw new BadRequestException(String.format("Feature of interest id %s doesn't exists", idFoi));
                }
            } else {
                featureOfInterest = foi;
                DtoToModelMapper.checkRequireField(featureOfInterest);
            }
        }
        return featureOfInterest;
    }

    /**
     * remove cache sensor linked to datastream
     *
     * @param datastream
     */
    private void removeCachedExpandedSensor(ExpandedDataStream datastream) {
        // retrieve created sensor
        if (datastream.sensor() != null && DtoToModelMapper.isRecordOnlyField(datastream.sensor(), "id")) {
            String idSensor = DtoToModelMapper.getIdFromRecord(datastream.sensor());

            sensorExtraUseCase.removeInMemorySensor(idSensor);

        }

    }

    /**
     * get observed property if we create datastream using link observedpropertyh id
     * or observed property inline or null
     *
     * @param datastream
     * @return
     */
    private ExpandedObservedProperty getCachedExpandedObservedProperty(ExpandedDataStream datastream) {
        ExpandedObservedProperty observedProperty = null;
        // retrieve create observedPorperty
        if (datastream.observedProperty() != null) {
            if (DtoToModelMapper.isRecordOnlyField(datastream.observedProperty(), "id")) {
                String idObservedProperty = DtoToModelMapper.getIdFromRecord(datastream.observedProperty());
                observedProperty = observedPropertyUseCase.getInMemoryObservedProperty(idObservedProperty);
                if (observedProperty == null) {
                    throw new BadRequestException(
                            String.format("observedProperty id %s doesn't exists", idObservedProperty));
                }
            } else {
                observedProperty = datastream.observedProperty();
                DtoToModelMapper.checkRequireField(observedProperty);
            }
        }
        return observedProperty;
    }

    /**
     * remove cache observed Property linked to datastream
     *
     * @param datastream
     */
    private void removeCachedExpandedObservedProperty(ExpandedDataStream datastream) {
        // retrieve create observedPorperty
        if (datastream.observedProperty() != null
                && DtoToModelMapper.isRecordOnlyField(datastream.observedProperty(), "id")) {
            String idObservedProperty = DtoToModelMapper.getIdFromRecord(datastream.observedProperty());
            observedPropertyUseCase.removeInMemoryObservedProperty(idObservedProperty);
        }

    }

    /**
     * remove cache sensor linked to observation
     *
     * @param datastream
     */
    private void removeCachedFeatureOfInterest(FeatureOfInterest foi) {
        // retrieve create observedPorperty
        if (foi != null && DtoToModelMapper.isRecordOnlyField(foi, "id")) {
            String idFoi = DtoToModelMapper.getIdFromRecord(foi);
            featureOfInterestUseCase.removeInMemoryFeatureOfInterest(idFoi);
        }

    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedDataStream> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);
        String id = getId(request.model());
        String thingId = request.model().thing().id() == null ? request.parentId()
                : (String) request.model().thing().id();
        if (thingId == null) {
            throw new BadRequestException("Thing id not found");
        }
        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());
        }

        ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), thingId, id);

        removeCachedExpandedObservedProperty(request.model());
        removeCachedExpandedSensor(request.model());
        if (request.model().observations() != null) {
            request.model().observations().stream()
                    .forEach(obs -> removeCachedFeatureOfInterest(obs.featureOfInterest()));
        }

        return new ExtraUseCaseResponse<ServiceSnapshot>(id, serviceSnapshot);

    }

    @Override
    public String getId(ExpandedDataStream dto) {
        return (String) (dto.id() != null ? dto.id() : dto.name());
    }

}
