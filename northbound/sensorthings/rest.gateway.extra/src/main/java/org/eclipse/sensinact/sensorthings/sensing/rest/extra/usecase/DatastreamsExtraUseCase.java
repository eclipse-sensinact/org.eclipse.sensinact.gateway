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
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
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
public class DatastreamsExtraUseCase extends AbstractExtraUseCaseModelDelete<ExpandedDataStream, ProviderSnapshot> {

    private final IDtoMemoryCache<ExpandedObservation> obsCache;

    @SuppressWarnings("unchecked")
    public DatastreamsExtraUseCase(Providers providers, Application application) {
        super(providers, application);

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

        Sensor sensor = getCachedExpandedSensor(request, request.model());
        ObservedProperty observedProperty = getCachedExpandedObservedProperty(request, request.model());
        UnitOfMeasurement unit = request.model().unitOfMeasurement();
        String sensorId = DtoToModelMapper.getNewId(sensor);
        String observedPropertyId = DtoToModelMapper.getNewId(observedProperty);
        String thingId = getThingId(request, datastream, datastreamId);
        ProviderSnapshot providerThing = providerUseCase.read(request.session(), thingId);

        GeoJsonObject observedArea = getObservedArea(request.session(), request.id());
        updateOldThingDatastreamIdIfNeeded(request, listUpdates, datastreamId, thingId);
        checkRequireLink(request, sensor, observedProperty, unit, providerThing);
        addDatastreamIdLinkToLinkThing(request, datastreamId, providerThing, listUpdates);

        List<String> existingDatastreamIdsSensor = DtoToModelMapper.getExistingDatastreamIdsSensor(request,
                providerUseCase, sensorId);
        List<String> existingDatastreamIdsObservedProperty = DtoToModelMapper
                .getExistingDatastreamIdsObservedProperty(request, providerUseCase, observedPropertyId);

        if (datastream.observations() != null && datastream.observations().size() > 0) {
            listUpdates.addAll(datastream.observations().stream().flatMap(obs -> {
                FeatureOfInterest foi = getFeatureOfInterest(request.session(), obs.featureOfInterest());
                List<SensorThingsUpdate> updates = new ArrayList<SensorThingsUpdate>();
                if (foi == null) {
                    // check if default foi already exists
                    foi = getFeatureOfInterest(request.session(), providerThing.getName());
                    if (foi == null) {
                        GeoJsonObject locationFeature = DtoMapperSimple.getResourceField(
                                DtoMapperSimple.getAdminService(providerThing), "location", GeoJsonObject.class);
                        final GeoJsonObject feature = locationFeature != null ? locationFeature : observedArea;

                        foi = new FeatureOfInterest(null, providerThing.getName() + "foi", "default",
                                "default feature of interest", "application/vnd.geo+json", feature, Map.of(), null);
                    }
                }

                updates.add(DtoToModelMapper.toFoiUpdate((String) foi.id(), foi, DtoToModelMapper
                        .getDatastreamIdsFoi(providerUseCase, request, foi.id().toString(), datastreamId), true));

                updates.addAll(DtoToModelMapper.toDatastreamUpdate(request.mapper(), datastreamId, observedArea,
                        thingId, datastream, sensorId, sensor, existingDatastreamIdsSensor, observedPropertyId,
                        observedProperty, existingDatastreamIdsObservedProperty, unit, obs, foi));

                return updates.stream();
            }).toList());
        } else {
            listUpdates.addAll(DtoToModelMapper.toDatastreamUpdate(request.mapper(), datastreamId, observedArea,
                    thingId, datastream, sensorId, sensor, existingDatastreamIdsSensor, observedPropertyId,
                    observedProperty, existingDatastreamIdsObservedProperty, unit, null, null));
        }

        return listUpdates;

    }

    private String getThingId(ExtraUseCaseRequest<?> request) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
        return getThingId(request, providerId);
    }

    private String getSensorId(ExtraUseCaseRequest<?> request) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
        ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
        return DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(provider), "sensorId",
                String.class);
    }

    private String getObservedPropertyId(ExtraUseCaseRequest<?> request) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
        ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
        return DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(provider), "observedPropertyId",
                String.class);
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
            List<String> ids = DtoToModelMapper.getDatastreamIdsFromThing(providerThing).stream()
                    .filter(id -> !datastreamId.equals(id)).toList();

            listUpdates.add(new ThingUpdate(thingId, Instant.now(), null, null, null, thingId, null, null, ids));

        }
    }

    private void addDatastreamIdLinkToLinkThing(ExtraUseCaseRequest<ExpandedDataStream> request, String datastreamId,
            ProviderSnapshot providerThing, List<SensorThingsUpdate> listUpdates) {
        ServiceSnapshot serviceThing = providerThing.getService("thing");

        List<String> ids = DtoToModelMapper.getDatastreamIds(serviceThing);
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
        observations.stream().forEach(u -> updateObservationMemoryHistory(obsCache, request.mapper(), u));

        ProviderSnapshot provider = providerUseCase.read(request.session(), id);

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

    protected AbstractSensinactCommand<Map<String, TimedValue<?>>> getDatastreamidsSensorOp(String providerId,
            String thingId, String opId, String sensorId) {

        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = new AbstractSensinactCommand<Map<String, TimedValue<?>>>() {

            @Override
            protected Promise<Map<String, TimedValue<?>>> call(SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                SensinactProvider spOp = twin.getProvider(opId);
                SensinactProvider spSensor = twin.getProvider(sensorId);
                SensinactProvider sp = twin.getProvider(providerId);

                SensinactProvider spThing = twin.getProvider(thingId);

                if (sp == null) {
                    return pf.failed(new NotFoundException("provider for datastream " + providerId + " not found"));
                }

                // Make sure all promises are Promise<TimedValue<?>>
                SensinactResource r = spThing.getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> datastreamIds = (Promise<TimedValue<?>>) (Promise<?>) r
                        .getMultiValue(String.class);
                r = sp.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> lastObservation = (Promise<TimedValue<?>>) (Promise<?>) r.getValue(String.class);

                r = spOp.getResource(DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "datastreamIds");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> datastreamIdsOp = (Promise<TimedValue<?>>) (Promise<?>) r
                        .getMultiValue(String.class);

                r = spSensor.getResource(DtoMapperSimple.SERVICE_SENSOR, "datastreamIds");
                @SuppressWarnings("unchecked")
                Promise<TimedValue<?>> datastreamIdsSensor = (Promise<TimedValue<?>>) (Promise<?>) r
                        .getMultiValue(String.class);

                // Combine all promises in a concrete list
                List<Promise<TimedValue<?>>> promises = List.of(datastreamIds, datastreamIdsOp, datastreamIdsSensor,
                        lastObservation);

                // pf.all now works
                return pf.all(promises).map(list -> Map.of("datastreamIds", list.get(0), "datastreamIdsOp", list.get(1),
                        "datastreamIdsSensor", list.get(2), "lastObservation", list.get(3)));

            }
        };
        return parentCommand;
    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // delete datastream
        // TODO Authorization
        String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
        String thingId = getThingId(request);
        String opId = getObservedPropertyId(request);
        String sensorId = getSensorId(request);
        AbstractSensinactCommand<Map<String, TimedValue<?>>> parentCommand = getDatastreamidsSensorOp(providerId,
                thingId, opId, sensorId);

        return new DependentCommand<Map<String, TimedValue<?>>, List<Void>>(parentCommand) {

            @Override
            protected Promise<List<Void>> call(Promise<Map<String, TimedValue<?>>> parentResult,
                    SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                    SensinactProvider sp = twin.getProvider(providerId);
                    Map<String, TimedValue<?>> map = parentResult.getValue();

                    List<Promise<Void>> list = new ArrayList<Promise<Void>>();

                    @SuppressWarnings("unchecked")
                    List<String> datastreamIdsSensor = (List<String>) map.get("datastreamIdsSensor").getValue();
                    @SuppressWarnings("unchecked")
                    List<String> datastreamIdsOp = (List<String>) map.get("datastreamIdsOp").getValue();

                    updateSensorDatastreamIds(request, thingId, twin, list, datastreamIdsSensor);
                    updateObservedPropertyDatastreamIds(request, opId, twin, list, datastreamIdsOp);
                    @SuppressWarnings("unchecked")
                    List<String> datastreamIds = map.containsKey("datastreamIds")
                            ? (List<String>) map.get("datastreamIds").getValue()
                            : null;
                    updateThingDatastreamIds(request, sensorId, twin, list, datastreamIds);
                    if (sp != null) {
                        // check if there are still observed property and sensor

                        obsCache.removeDtoStartWith(providerId);
                        sp.delete();

                    }
                    return pf.all(list);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

        };

    }

}
