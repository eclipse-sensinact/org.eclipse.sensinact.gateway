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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.DatastreamUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
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
public class ThingsExtraUseCase extends AbstractExtraUseCaseModelDelete<ExpandedThing, ProviderSnapshot> {

    private final IDtoMemoryCache<ExpandedObservation> cacheObs;
    private final IDtoMemoryCache<Instant> cacheHl;

    @SuppressWarnings("unchecked")
    public ThingsExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        cacheObs = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
        cacheHl = resolve(providers, IDtoMemoryCache.class, Instant.class);
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
            locationIds.addAll(DtoToModelMapper.getLocationIds(provider));
            datastreamIds.addAll(DtoToModelMapper.getDatastreamIdsFromThing(provider));
        }
        List<ExpandedDataStream> listDatastream = Optional.ofNullable(request.model().datastreams()).orElseGet(List::of)
                .stream().map(ds -> {
                    Sensor sensor = getCachedExpandedSensor(request, ds);
                    ObservedProperty observedProperty = getCachedExpandedObservedProperty(request, ds);
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

        return DtoToModelMapper.toThingUpdates(request, id, providerUseCase, locationIds, datastreamIds, listDatastream,
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
        if (!request.acceptInlineOnUpdate()) {
            checkNoInline(request);

        }
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);
        ResourceSnapshot resourceLocation = getHistoricalLocationForMemoryHistory(request);
        List<ResourceSnapshot> obsThingDatastream = getObservationsForMemoryHistory(request.session(), listDtoModels);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        updateHistoricalLocationMemoryHistory(request, resourceLocation);
        updateObservationHistoryMemory(request, obsThingDatastream);

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    private void updateObservationHistoryMemory(ExtraUseCaseRequest<ExpandedThing> request,
            List<ResourceSnapshot> obsThingDatastream) {
        if (obsThingDatastream != null)
            obsThingDatastream.stream().forEach(u -> updateObservationMemoryHistory(cacheObs, request.mapper(), u));
    }

    private void updateHistoricalLocationMemoryHistory(ExtraUseCaseRequest<ExpandedThing> request,
            ResourceSnapshot resourceLocation) {
        System.out.println("update historical Cache");
        if (resourceLocation != null) {
            Instant stamp = resourceLocation.getValue().getTimestamp();
            String idCache = request.id();
            System.out.println("update historical Cache done " + idCache + "~" + DtoMapperSimple.stampToId(stamp));

            cacheHl.addDto(idCache + "~" + DtoMapperSimple.stampToId(stamp), stamp);
        }
    }

    private ResourceSnapshot getHistoricalLocationForMemoryHistory(ExtraUseCaseRequest<ExpandedThing> request) {
        ResourceSnapshot provider = request.model().locations() != null && request.model().locations().size() > 0
                ? getProviderThingIfLocationFieldExists(request.session(), request.id())
                : null;
        return provider;
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

    protected DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> getContextDeleteDatastreamProvider(
            ExtraUseCaseRequest<?> request, ResourceCommand<TimedValue<List<String>>> datastreamIdCommand) {
        DependentCommand<TimedValue<List<String>>, Map<String, Map<String, TimedValue<?>>>> parentCommand = new DependentCommand<TimedValue<List<String>>, Map<String, Map<String, TimedValue<?>>>>(
                datastreamIdCommand) {

            @SuppressWarnings("unchecked")
            @Override
            protected Promise<Map<String, Map<String, TimedValue<?>>>> call(
                    Promise<TimedValue<List<String>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    // get list ofd opId and sensorId and lastObservation
                    List<String> datastreamIds = parentResult.getValue().getValue();
                    List<SensinactProvider> datastreamProvs = datastreamIds.stream().map(id -> twin.getProvider(id))
                            .filter(Objects::nonNull).toList();

                    List<Promise<List<TimedValue<?>>>> promisesOfLists = datastreamProvs.stream().map(sp -> {
                        Promise<TimedValue<?>> lastObs = getPromiseLastObservation(sp);

                        Promise<TimedValue<?>> opId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "observedPropertyId")
                                .getValue(String.class);

                        Promise<TimedValue<?>> sensorId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "sensorId").getValue(String.class);

                        return pf.all(List.of(lastObs, opId, sensorId));
                    }).toList();

                    return pf.all(promisesOfLists).map(resolvedLists -> {
                        Map<String, Map<String, TimedValue<?>>> finalMap = new HashMap<>();

                        for (int i = 0; i < datastreamIds.size(); i++) {
                            List<TimedValue<?>> list = resolvedLists.get(i);
                            finalMap.put(datastreamIds.get(i), Map.of("lastObservation", list.get(0), "opId",
                                    list.get(1), "sensorId", list.get(2)));
                        }
                        return finalMap;
                    });
                } catch (Exception e) {
                    return pf.failed(e);

                }
            }
        };
        DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> parent2 = new DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>>(
                parentCommand) {

            @Override
            protected Promise<Map<String, List<?>>> call(Promise<Map<String, Map<String, TimedValue<?>>>> parentResult,
                    SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    Map<String, Map<String, TimedValue<?>>> map = parentResult.getValue();
                    List<String> datastreamIds = map.keySet().stream().toList();

                    Map<String, Promise<TimedValue<List<String>>>> promisesMap = new LinkedHashMap<>();
                    // get datastreamId for sensor and observedproperty
                    datastreamIds.stream().map(id -> twin.getProvider(id)).filter(Objects::nonNull)
                            .forEach(spDatastream -> {

                                String sensorId = (String) map.get(spDatastream.getName()).get("sensorId").getValue();
                                String opId = (String) map.get(spDatastream.getName()).get("opId").getValue();

                                promisesMap.put(sensorId,
                                        twin.getProvider(sensorId)
                                                .getResource(DtoMapperSimple.SERVICE_SENSOR, "datastreamIds")
                                                .getMultiValue(String.class));
                                promisesMap.put(opId,
                                        twin.getProvider(opId)
                                                .getResource(DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "datastreamIds")
                                                .getMultiValue(String.class));
                            });
                    // delete datastreams
                    datastreamIds.stream().forEach(id -> {
                        SensinactProvider spDatastream = twin.getProvider(id);
                        if (spDatastream != null) {

                            spDatastream.delete();
                        }
                    });

                    List<String> keys = new ArrayList<>(promisesMap.keySet());
                    List<Promise<TimedValue<List<String>>>> promises = new ArrayList<>(promisesMap.values());

                    return pf.all(promises).then(resolvedList -> {
                        Map<String, List<?>> result = new LinkedHashMap<>();
                        List<TimedValue<List<String>>> values = resolvedList.getValue();
                        for (int i = 0; i < keys.size(); i++) {
                            result.put(keys.get(i), List.of(values.get(i)));
                        }
                        result.put("datastreamIds", datastreamIds);
                        return pf.resolved(result);
                    });

                } catch (Exception e) {
                    return pf.failed(e);
                }
            }
        };
        return parent2;

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
        DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> depend = getContextDeleteDatastreamProvider(
                request, listDatastreamIds);
        return new DependentCommand<Map<String, List<?>>, List<Void>>(depend) {
            @SuppressWarnings("unchecked")
            @Override
            protected Promise<List<Void>> call(Promise<Map<String, List<?>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(request.id());

                try {
                    Map<String, List<?>> map = parentResult.getValue();
                    Map<String, List<TimedValue<List<String>>>> mapDatastreamSensorOp = map.entrySet().stream()
                            .filter(entry -> {
                                List<?> list = entry.getValue();
                                return !list.isEmpty() && list.get(0) instanceof TimedValue<?>;
                            }).collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> (List<TimedValue<List<String>>>) entry.getValue()));
                    List<String> datastreamIds = map.entrySet().stream().filter(entry -> {
                        List<?> list = entry.getValue();
                        return !list.isEmpty() && list.get(0) instanceof String;
                    }).flatMap(entry -> ((List<String>) entry.getValue()).stream()).toList();
                    mapDatastreamSensorOp.entrySet().stream().forEach(entry -> {
                        String idElem = entry.getKey();
                        entry.getValue().stream().forEach(tv -> {
                            List<String> currentDatastreamElemIds = tv.getValue();
                            List<String> newList = currentDatastreamElemIds.stream()
                                    .filter(id -> !datastreamIds.contains(id)).toList();
                            SensinactProvider prov = twin.getProvider(idElem);
                            SensinactResource datastreamToChangeIds = prov.getResource(DtoMapperSimple.SERVICE_SENSOR,
                                    "datastreamIds") != null
                                            ? prov.getResource(DtoMapperSimple.SERVICE_SENSOR, "datastreamIds")
                                            : prov.getResource(DtoMapperSimple.SERVICE_OBSERVED_PROPERTY,
                                                    "datastreamIds");
                            datastreamToChangeIds.setValue(newList);
                        });
                    });

                    if (sp != null) {
                        sp.delete();
                        if (isHistoryMemory())
                            cacheHl.removeDtoContain(request.id());
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
