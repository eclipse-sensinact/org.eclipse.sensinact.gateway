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
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.DatastreamUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing Thing
 */
public class ThingsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedThing, ProviderSnapshot> {

    private final IDtoMemoryCache<Sensor> sensorCache;
    private final IDtoMemoryCache<ExpandedObservation> cacheObs;
    private final IDtoMemoryCache<Instant> cacheHl;
    private final IDtoMemoryCache<FeatureOfInterest> cacheFoi;

    private final IDtoMemoryCache<ObservedProperty> observedPropertyCache;

    @SuppressWarnings("unchecked")
    public ThingsExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        sensorCache = resolve(providers, IDtoMemoryCache.class, Sensor.class);
        observedPropertyCache = resolve(providers, IDtoMemoryCache.class, ObservedProperty.class);
        cacheObs = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
        cacheHl = resolve(providers, IDtoMemoryCache.class, Instant.class);
        cacheFoi = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ExpandedThing> request) {
        String id = request.id();
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }

        ProviderSnapshot provider = providerUseCase.read(request.session(), id);
        if (provider != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(id, provider);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "failed to create Thing");

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedThing> request) {
        // check if Thing already exists with location get locations
        List<String> locationIds = new ArrayList<String>();
        List<String> datastreamIds = new ArrayList<String>();

        checkRequireField(request);
        String id = request.id();
        ProviderSnapshot provider = providerUseCase.read(request.session(), id);
        if (provider != null) {
            locationIds.addAll(getLocationIds(provider));
            datastreamIds.addAll(getDatastreamIds(provider));
        }
        List<ExpandedDataStream> listDatastream = Optional.ofNullable(request.model().datastreams()).orElseGet(List::of)
                .stream().map(ds -> {
                    Sensor sensor = getCachedExpandedSensor(request, sensorCache, ds);
                    ObservedProperty observedProperty = getCachedExpandedObservedProperty(request,
                            observedPropertyCache, ds);
                    if (!DtoMapperSimple.isRecordOnlyField(ds, "id")) {
                        checkRequireField(ds);
                        checkRequireLink(sensor, observedProperty);
                    }

                    return new ExpandedDataStream(ds.selfLink(), ds.id(), ds.name(), ds.description(),
                            ds.observationType(), ds.unitOfMeasurement(), ds.observedArea(), ds.phenomenonTime(),
                            ds.resultTime(), ds.properties(), ds.observationsLink(), ds.observedPropertyLink(),
                            ds.sensorLink(), ds.thingLink(), ds.observations(), observedProperty, sensor, ds.obsLink(),
                            ds.thing());
                }).toList();
        List<Location> listNewLocation = Optional.ofNullable(request.model().locations()).orElseGet(List::of).stream()
                .map(l -> {
                    if (!DtoMapperSimple.isRecordOnlyField(l, "id")) {
                        checkRequireField(l);
                        return new Location(null, l.id(), l.name(), l.description(), l.encodingType(), l.location(),
                                l.properties(), null, null);
                    }
                    ProviderSnapshot providerLocation = providerUseCase.read(request.session(), (String) l.id());
                    return DtoMapperSimple.toLocation(request.mapper(), providerLocation, null, null, null);

                }).toList();

        return DtoToModelMapper.toThingUpdates(request, id, locationIds, datastreamIds, listDatastream,
                listNewLocation);
    }

    private List<ResourceSnapshot> getObservationsForMemoryHistory(SensiNactSession session,
            List<SensorThingsUpdate> listDtoModels) {
        List<ResourceSnapshot> obsThingDatastream = null;
        if (isHistoryMemory()) {
            // get if exists last historical
            obsThingDatastream = listDtoModels.stream().filter(update -> update instanceof DatastreamUpdate)
                    .map(update -> getObservationForMemoryHistory(session, ((DatastreamUpdate) update).providerId()))
                    .filter(Objects::nonNull).toList();
        }
        return obsThingDatastream;
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedThing> request) {
        // ensure we don't have inline entities
        checkNoInline(request);
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);
        ResourceSnapshot locationThing = getHistoricalLocationForMemoryHistory(request);
        List<ResourceSnapshot> obsThingDatastream = getObservationsForMemoryHistory(request.session(), listDtoModels);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        updateHistoricalLocationMemoryHistory(request, locationThing);
        obsThingDatastream.stream()
                .forEach(u -> updateObservationMemoryHistory(cacheObs, cacheFoi, request.mapper(), u));
        ProviderSnapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    private void updateHistoricalLocationMemoryHistory(ExtraUseCaseRequest<ExpandedThing> request,
            ResourceSnapshot locationThing) {
        if (locationThing != null) {
            Instant stamp = locationThing.getValue().getTimestamp();
            cacheHl.addDto(request.id() + "~" + DtoMapperSimple.stampToId(stamp), stamp);
        }
    }

    private ResourceSnapshot getHistoricalLocationForMemoryHistory(ExtraUseCaseRequest<ExpandedThing> request) {
        ResourceSnapshot locationThing = request.model().locations() != null && request.model().locations().size() > 0
                ? getLocationExistingThing(request.session(), request.id())
                : null;
        return locationThing;
    }

    private void checkNoInline(ExtraUseCaseRequest<ExpandedThing> request) {
        if (request.model().datastreams() != null && !request.model().datastreams().stream()
                .allMatch(ds -> DtoMapperSimple.isRecordOnlyField(ds, "id"))) {
            throw new BadRequestException("datastream no expected for patch or update");
        }
        if (request.model().locations() != null
                && !request.model().locations().stream().allMatch(l -> DtoMapperSimple.isRecordOnlyField(l, "id"))) {
            throw new BadRequestException("locations no expected for patch or update");

        }
    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedThing> request) {
        // get resource list of datastreamId
        ResourceCommand<TimedValue<List<String>>> listDatastreamIds = new ResourceCommand<TimedValue<List<String>>>(
                request.id(), DtoMapperSimple.SERVICE_THING, "datastreamIds") {

            @Override
            protected Promise<TimedValue<List<String>>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMultiValue(String.class);
            }

        };
        return new DependentCommand<TimedValue<List<String>>, Void>(listDatastreamIds) {
            // delete datastreams and thing
            @Override
            protected Promise<Void> call(Promise<TimedValue<List<String>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(request.id());
                if (sp != null) {
                    sp.delete();
                }

                List<String> datastreamIds;
                try {
                    if (parentResult.getValue() != null) {
                        datastreamIds = parentResult.getValue().getValue();

                        if (datastreamIds != null) {
                            List<Promise<Void>> list = datastreamIds.stream()
                                    .flatMap(id -> removeDatastream(twin, id).stream()).toList();
                            pf.all(list);
                        }
                    }
                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }
        };

    }

    /**
     * delete all link between thing and location. if idLocation is null we delete
     * all link between thing and their locations
     *
     * @param idThing
     * @param idLocation
     * @return
     */
    public AbstractSensinactCommand<?> deleteThingLocationsRef(String idThing, String idLocation) {
        ResourceCommand<TimedValue<List<String>>> listLocationIds = new ResourceCommand<TimedValue<List<String>>>(
                idThing, DtoMapperSimple.SERVICE_THING, "locationIds") {

            @Override
            protected Promise<TimedValue<List<String>>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMultiValue(String.class);
            }

        };
        return new DependentCommand<TimedValue<List<String>>, Void>(listLocationIds) {

            @Override
            protected Promise<Void> call(Promise<TimedValue<List<String>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    List<String> locationIds = parentResult.getValue().getValue();
                    if (locationIds != null) {
                        List<String> newLocationIds = idLocation != null
                                ? locationIds.stream().filter(id -> !id.equals(idLocation)).toList()
                                : List.of();

                        SensinactResource resource = twin.getResource(idThing, DtoMapperSimple.SERVICE_THING,
                                "locationIds");
                        return resource.setValue(newLocationIds);
                    }
                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }
        };
    }

}
