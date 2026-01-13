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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * datastream
 */
public class DatastreamsExtraUseCase extends AbstractExtraUseCaseDto<ExpandedDataStream, ServiceSnapshot> {

    private final DataUpdate dataUpdate;

    private final IAccessProviderUseCase providerUseCase;

    private final IAccessServiceUseCase serviceUseCase;

    private final IDtoMemoryCache<ExpandedSensor> sensorCache;

    private final IDtoMemoryCache<FeatureOfInterest> foiCache;

    private final IDtoMemoryCache<ExpandedObservedProperty> observedPropertyCache;

    @SuppressWarnings("unchecked")
    public DatastreamsExtraUseCase(Providers providers) {
        super(providers);

        sensorCache = resolve(providers, IDtoMemoryCache.class, ExpandedSensor.class);
        foiCache = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);
        observedPropertyCache = resolve(providers, IDtoMemoryCache.class, ExpandedObservedProperty.class);
    }

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String idDatastream = request.id();

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }

        ServiceSnapshot snapshot = serviceUseCase.read(request.session(), idDatastream, "datastream");
        if (snapshot != null) {

            removeCachedExpandedObservedProperty(request.model());
            removeCachedExpandedSensor(request.model());
            if (request.model().observations() != null) {
                request.model().observations().stream()
                        .forEach(obs -> removeCachedFeatureOfInterest(obs.featureOfInterest()));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(idDatastream, snapshot);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // read thing for each location and update it
        List<SensorThingsUpdate> listUpdates = new ArrayList<SensorThingsUpdate>();

        ExpandedDataStream datastream = request.model();
        String datastreamId = request.id();

        checkRequireField(request);

        ExpandedSensor sensor = getCachedExpandedSensor(request.model());
        ExpandedObservedProperty observedProperty = getCachedExpandedObservedProperty(request.model());
        UnitOfMeasurement unit = request.model().unitOfMeasurement();

        String thingId = getThingId(request, datastream, datastreamId);
        ProviderSnapshot providerThing = providerUseCase.read(request.session(), thingId);

        updateOldThingDatastreamIdIfNeeded(request, listUpdates, datastreamId, thingId);
        checkRequireLink(request, sensor, observedProperty, unit, providerThing);
        addDatastreamIdLinkToLinkThing(request, datastreamId, providerThing, listUpdates);

        if (datastream.observations() != null && datastream.observations().size() > 0) {
            listUpdates.addAll(datastream.observations().stream()
                    .map(obs -> DtoToModelMapper.toDatastreamUpdate(datastreamId, thingId, datastream, sensor,
                            observedProperty, unit, obs, getCachedFeatureOfInterest(obs.featureOfInterest())))
                    .toList());
        } else {
            listUpdates.add(DtoToModelMapper.toDatastreamUpdate(datastreamId, thingId, datastream, sensor,
                    observedProperty, unit, null, null));
        }
        return listUpdates;

    }

    private String getThingId(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return getThingId(request, request.id());
    }

    private String getThingId(ExtraUseCaseRequest<ExpandedDataStream> request, String datastreamId) {
        return getThingId(request, null, datastreamId);
    }

    private String getThingId(ExtraUseCaseRequest<ExpandedDataStream> request, ExpandedDataStream datastream,
            String datastreamId) {
        String thingId = datastream == null || datastream.thing() == null ? request.parentId()
                : (String) datastream.thing().id();
        // if datastream up date. check which thing is assign to and remove it
        if (thingId == null) {
            ProviderSnapshot providerDatastream = providerUseCase.read(request.session(), datastreamId);
            if (providerDatastream != null) {
                thingId = providerDatastream.getResource("datastream", "thingId").getValue() != null
                        ? (String) providerDatastream.getResource("datastream", "thingId").getValue().getValue()
                        : null;
            }
        }
        return thingId;
    }

    private void updateOldThingDatastreamIdIfNeeded(ExtraUseCaseRequest<ExpandedDataStream> request,
            List<SensorThingsUpdate> listUpdates, String datastreamId, String thingId) {
        ProviderSnapshot providerDatastream = providerUseCase.read(request.session(), datastreamId);
        if (providerDatastream == null) {
            return;
        }
        String oldThingId = UtilDto.getResourceField(providerDatastream.getService("datastream"), "thingId",
                String.class);
        if (thingId != null && oldThingId != null && !oldThingId.equals(thingId)) {

            // need to remove link to this datastream in oldthing
            ProviderSnapshot providerThing = providerUseCase.read(request.session(), oldThingId);
            List<String> ids = getDatastreamIds(providerThing).stream().filter(id -> !datastreamId.equals(id)).toList();

            listUpdates.add(new ThingUpdate(thingId, null, null, thingId, null, null, ids));

        }
    }

    private void addDatastreamIdLinkToLinkThing(ExtraUseCaseRequest<ExpandedDataStream> request, String datastreamId,
            ProviderSnapshot providerThing, List<SensorThingsUpdate> listUpdates) {
        ServiceSnapshot serviceThing = providerThing.getService("thing");

        List<String> ids = getDatastreamIds(serviceThing);
        if (!ids.contains(datastreamId)) {
            ids = Stream.concat(ids.stream(), Stream.of(datastreamId)).toList();
            listUpdates.add(
                    new ThingUpdate(providerThing.getName(), null, null, providerThing.getName(), null, null, ids));
        }
    }

    private void checkRequireLink(ExtraUseCaseRequest<ExpandedDataStream> request, ExpandedSensor sensor,
            ExpandedObservedProperty observedProperty, UnitOfMeasurement unit, ProviderSnapshot provider) {
        if (HttpMethod.POST.equals(request.method())) {
            DtoToModelMapper.checkRequireLink(request, provider, sensor, observedProperty, unit);
        } else if (HttpMethod.PUT.equals(request.method())) {
            DtoToModelMapper.checkRequireLink(request, provider, unit);
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

                sensor = sensorCache.getDto(idSensor);
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

                featureOfInterest = foiCache.getDto(idFoi);
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

            sensorCache.removeDto(idSensor);

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
                observedProperty = observedPropertyCache.getDto(idObservedProperty);
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
            observedPropertyCache.removeDto(idObservedProperty);
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
            foiCache.removeDto(idFoi);
        }

    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String id = request.id();
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }

        ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), id, "datastream");

        removeCachedExpandedObservedProperty(request.model());
        removeCachedExpandedSensor(request.model());
        if (request.model().observations() != null) {
            request.model().observations().stream()
                    .forEach(obs -> removeCachedFeatureOfInterest(obs.featureOfInterest()));
        }

        return new ExtraUseCaseResponse<ServiceSnapshot>(id, serviceSnapshot);

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // delete datastream
        // TODO Authorization
        String thingId = getThingId(request);
        ResourceCommand<TimedValue<List<String>>> parentCommand = new ResourceCommand<TimedValue<List<String>>>(thingId,
                UtilDto.SERVICE_THING, "datastreamIds") {
            @Override
            protected Promise<TimedValue<List<String>>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMultiValue(String.class);
            }
        };
        return new DependentCommand<TimedValue<List<String>>, Void>(parentCommand) {

            @Override
            protected Promise<Void> call(Promise<TimedValue<List<String>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {

                SensinactProvider sp = twin.getProvider(request.id());
                if (sp != null) {
                    sp.delete();
                }
                try {
                    List<String> datastreamIds = parentResult.getValue().getValue();
                    if (datastreamIds != null) {
                        List<String> newDatastreamIds = datastreamIds.stream().filter(id -> !id.equals(request.id()))
                                .toList();
                        SensinactResource resource = twin.getResource(thingId, UtilDto.SERVICE_THING, "datastreamIds");
                        return resource.setValue(newDatastreamIds);
                    }
                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

        };

    }

}
