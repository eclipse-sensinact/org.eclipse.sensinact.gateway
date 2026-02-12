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
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * datastream
 */
public class DatastreamsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedDataStream, ProviderSnapshot> {

    private final IDtoMemoryCache<Sensor> sensorCache;

    private final IDtoMemoryCache<FeatureOfInterest> foiCache;
    private final IDtoMemoryCache<ExpandedObservation> obsCache;

    private final IDtoMemoryCache<ObservedProperty> observedPropertyCache;

    @SuppressWarnings("unchecked")
    public DatastreamsExtraUseCase(Providers providers, Application application) {
        super(providers, application);

        sensorCache = resolve(providers, IDtoMemoryCache.class, Sensor.class);
        foiCache = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);
        observedPropertyCache = resolve(providers, IDtoMemoryCache.class, ObservedProperty.class);
        obsCache = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String idDatastream = request.id();

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), idDatastream);
        if (snapshot != null) {
            if (!isHistoryMemory()) {
                removeCachedExpandedObservedProperty(request.model());
                removeCachedExpandedSensor(request.model());
                if (request.model().observations() != null) {
                    request.model().observations().stream()
                            .forEach(obs -> removeCachedFeatureOfInterest(obs.featureOfInterest()));
                }
            }
            return new ExtraUseCaseResponse<ProviderSnapshot>(idDatastream, snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to get Snapshot");

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // read thing for each location and update it
        List<SensorThingsUpdate> listUpdates = new ArrayList<SensorThingsUpdate>();

        ExpandedDataStream datastream = request.model();
        String datastreamId = request.id();

        checkRequireField(request);

        Sensor sensor = getCachedExpandedSensor(request, sensorCache, request.model());
        ObservedProperty observedProperty = getCachedExpandedObservedProperty(request, observedPropertyCache,
                request.model());
        UnitOfMeasurement unit = request.model().unitOfMeasurement();

        String thingId = getThingId(request, datastream, datastreamId);
        ProviderSnapshot providerThing = providerUseCase.read(request.session(), thingId);

        GeoJsonObject observedArea = getObservedArea(request.session(), request.id());
        updateOldThingDatastreamIdIfNeeded(request, listUpdates, datastreamId, thingId);
        checkRequireLink(request, sensor, observedProperty, unit, providerThing);
        addDatastreamIdLinkToLinkThing(request, datastreamId, providerThing, listUpdates);

        if (datastream.observations() != null && datastream.observations().size() > 0) {
            listUpdates.addAll(datastream.observations().stream().map(obs -> {
                FeatureOfInterest foi = getCachedFeatureOfInterest(obs.featureOfInterest());
                if (foi == null) {
                    GeoJsonObject locationFeature = DtoMapperSimple.getResourceField(
                            DtoMapperSimple.getAdminService(providerThing), "location", GeoJsonObject.class);
                    final GeoJsonObject feature = locationFeature != null ? locationFeature : observedArea;

                    foi = new FeatureOfInterest(null, DtoToModelMapper.getNewId(), "default",
                            "default feature of interest", "application/vnd.geo+json", feature, Map.of(), null);
                }
                return DtoToModelMapper.toDatastreamUpdate(request.mapper(), datastreamId, observedArea, thingId,
                        datastream, sensor, observedProperty, unit, obs, foi);
            }).toList());
        } else {
            listUpdates.add(DtoToModelMapper.toDatastreamUpdate(request.mapper(), datastreamId, observedArea, thingId,
                    datastream, sensor, observedProperty, unit, null, null));
        }
        return listUpdates;

    }

    private String getThingId(ExtraUseCaseRequest<?> request) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
        return getThingId(request, providerId);
    }

    private String getThingId(ExtraUseCaseRequest<?> request, String datastreamId) {
        return getThingId(request, null, datastreamId);
    }

    private String getThingId(ExtraUseCaseRequest<?> request, ExpandedDataStream datastream, String datastreamId) {
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
        String oldThingId = DtoMapperSimple.getResourceField(providerDatastream.getService("datastream"), "thingId",
                String.class);
        if (thingId != null && oldThingId != null && !oldThingId.equals(thingId)) {

            // need to remove link to this datastream in oldthing
            ProviderSnapshot providerThing = providerUseCase.read(request.session(), oldThingId);
            List<String> ids = getDatastreamIds(providerThing).stream().filter(id -> !datastreamId.equals(id)).toList();

            listUpdates.add(new ThingUpdate(thingId, Instant.now(), null, null, null, thingId, null, null, ids));

        }
    }

    private void addDatastreamIdLinkToLinkThing(ExtraUseCaseRequest<ExpandedDataStream> request, String datastreamId,
            ProviderSnapshot providerThing, List<SensorThingsUpdate> listUpdates) {
        ServiceSnapshot serviceThing = providerThing.getService("thing");

        List<String> ids = getDatastreamIds(serviceThing);
        if (!ids.contains(datastreamId)) {
            ids = Stream.concat(ids.stream(), Stream.of(datastreamId)).toList();
            listUpdates.add(new ThingUpdate(providerThing.getName(), Instant.now(), null, null, null,
                    providerThing.getName(), null, null, ids));
        }
    }

    private void checkRequireLink(ExtraUseCaseRequest<ExpandedDataStream> request, Sensor sensor,
            ObservedProperty observedProperty, UnitOfMeasurement unit, ProviderSnapshot provider) {
        try {
            if (HttpMethod.POST.equals(request.method())) {
                DtoMapperSimple.checkRequireLink(request, provider, sensor, observedProperty, unit);
            } else if (HttpMethod.PUT.equals(request.method())) {
                DtoMapperSimple.checkRequireLink(request, provider, unit);
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
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
                if (idFoi == null) {
                    throw new BadRequestException(String.format("foi id is null"));
                }
                featureOfInterest = foiCache.getDto(idFoi);
                if (featureOfInterest == null) {
                    throw new BadRequestException(String.format("Feature of interest id %s doesn't exists", idFoi));
                }
            } else {
                featureOfInterest = foi;
                DtoMapperSimple.checkRequireField(featureOfInterest);
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

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String id = request.id();
        if (!request.acceptInlineOnUpdate()) {
            checkNoInline(request);

        }

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);
        List<ResourceSnapshot> observations = new ArrayList<ResourceSnapshot>();
        if (isHistoryMemory()) {
            // get if exists last historical
            observations.add(getObservationForMemoryHistory(request.session(), request.id()));

        }
        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }
        observations.stream().forEach(u -> updateObservationMemoryHistory(obsCache, foiCache, request.mapper(), u));

        ProviderSnapshot provider = providerUseCase.read(request.session(), id);
        if (!isHistoryMemory()) {
            removeCachedExpandedObservedProperty(request.model());
            removeCachedExpandedSensor(request.model());
            if (request.model().observations() != null) {
                request.model().observations().stream()
                        .forEach(obs -> removeCachedFeatureOfInterest(obs.featureOfInterest()));
            }
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(id, provider);

    }

    private void checkNoInline(ExtraUseCaseRequest<ExpandedDataStream> request) {
        if (request.model().observations() != null && !request.model().observations().stream()
                .allMatch(ds -> DtoMapperSimple.isRecordOnlyField(ds, "id"))) {
            throw new BadRequestException("observations no expected for patch or update");
        }
        if (request.model().sensor() != null && !DtoMapperSimple.isRecordOnlyField(request.model().sensor(), "id")) {

            throw new BadRequestException("sensor no expected for patch or update");
        }
        if (request.model().observedProperty() != null
                && !DtoMapperSimple.isRecordOnlyField(request.model().observedProperty(), "id")) {

            throw new BadRequestException("observedProperty no expected for patch or update");

        }
    }

    @Override
    protected AbstractSensinactCommand<Map<String, TimedValue<?>>> getContextDeleteDatastreamProvider(
            ExtraUseCaseRequest<?> request) {
        String thingId = getThingId(request);
        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = new AbstractSensinactCommand<Map<String, TimedValue<?>>>() {

            @Override
            protected Promise<Map<String, TimedValue<?>>> call(SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                SensinactProvider sp = twin.getProvider(providerId);
                SensinactProvider spThing = twin.getProvider(thingId);

                if (sp == null) {
                    return pf.failed(new NotFoundException("provider for datastream " + request.id() + " not found"));
                }

                // Make sure all promises are Promise<TimedValue<?>>
                SensinactResource r = spThing.getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> datastreamIds = (Promise<TimedValue<?>>) (Promise<?>) r
                        .getMultiValue(String.class);

                r = sp.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "observedPropertyId");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> observedPropertyId = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "observedPropertyId").getValue(String.class);
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> obsStr = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation").getValue(String.class);

                r = sp.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "sensorId");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> sensorId = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "sensorId").getValue(String.class);

                r = sp.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "id");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> id = (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "id").getValue(String.class);

                // Combine all promises in a concrete list
                List<Promise<TimedValue<?>>> promises = List.of(datastreamIds, observedPropertyId, sensorId, id,
                        obsStr);

                // pf.all now works
                return pf.all(promises).map(list -> Map.of("datastreamIds", list.get(0), "observedPropertyId",
                        list.get(1), "sensorId", list.get(2), "id", list.get(3), "lastObservation", list.get(4)));

            }
        };
        return parentCommand;
    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // delete datastream
        // TODO Authorization
        String thingId = getThingId(request);

        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = getContextDeleteDatastreamProvider(
                request);

        return new DependentCommand<Map<String, TimedValue<?>>, List<Void>>(parentCommand) {

            @Override
            protected Promise<List<Void>> call(Promise<Map<String, TimedValue<?>>> parentResult,
                    SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                    SensinactProvider sp = twin.getProvider(providerId);
                    Map<String, TimedValue<?>> map = parentResult.getValue();
                    String opId = (String) map.get("observedPropertyId").getValue();
                    String sensorId = (String) map.get("sensorId").getValue();
                    String obsStr = (String) map.get("lastObservation").getValue();
                    Instant obsStamp = map.get("lastObservation").getTimestamp();
                    List<Promise<Void>> list = new ArrayList<Promise<Void>>();
                    if (sp != null) {
                        // check if there are still observed property and sensor
                        if (hasNoObservedPropertyAndSensor(opId, sensorId)) {
                            sp.delete();
                            obsCache.removeDtoStartWith(providerId);
                        } else {
                            saveObservationHistoryMemory(obsCache, request, obsStr, obsStamp);
                            list.addAll(removeDatastream(twin, providerId));

                        }
                    }
                    @SuppressWarnings("unchecked")
                    List<String> datastreamIds = map.containsKey("datastreamIds")
                            ? (List<String>) map.get("datastreamIds").getValue()
                            : null;
                    if (datastreamIds != null) {
                        List<String> newDatastreamIds = datastreamIds.stream().filter(id -> !id.equals(request.id()))
                                .toList();
                        SensinactResource resource = twin.getResource(thingId, DtoMapperSimple.SERVICE_THING,
                                "datastreamIds");
                        list.add(resource.setValue(newDatastreamIds));
                    }
                    return pf.all(list);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

            private boolean hasNoObservedPropertyAndSensor(String opId, String sensorId) {
                return opId == null && sensorId == null;
            }

        };

    }

}
