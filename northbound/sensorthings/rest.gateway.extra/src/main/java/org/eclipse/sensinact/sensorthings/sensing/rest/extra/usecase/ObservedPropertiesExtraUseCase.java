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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observedProperty
 */
public class ObservedPropertiesExtraUseCase
        extends AbstractExtraUseCaseModelDelete<ObservedProperty, ProviderSnapshot> {

    public ObservedPropertiesExtraUseCase(Providers providers, Application application) {
        super(providers, application);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ObservedProperty> request) {
        String idOp = request.id();

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);
        }
        ProviderSnapshot snapshot = providerUseCase.read(request.session(), idOp);
        if (snapshot == null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "can't find sensor");
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
    }

    @Override
    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<ObservedProperty> request) {

        return super.delete(request);

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ObservedProperty> request) {
        String observedPropertyId = request.id();
        if (observedPropertyId == null) {
            throw new BadRequestException("bad id format");
        }
        ObservedProperty receivedOp = request.model();
        checkRequireField(request);
        ProviderSnapshot provider = providerUseCase.read(request.session(), request.id());
        List<String> datastreamIds = List.of();
        if (provider != null) {
            datastreamIds = DtoToModelMapper.getDatastreamIdsFromObservedProperty(provider);
        }
        ObservedProperty opToUpdate = new ObservedProperty(null, observedPropertyId, receivedOp.name(),
                receivedOp.description(), receivedOp.definition(), receivedOp.properties(), null);
        return List.of(DtoToModelMapper.toObservedProperty(observedPropertyId, opToUpdate, datastreamIds, null));
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ObservedProperty> request) {
        // check if sensor is in cached map

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
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "can't find sensor");
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);

    }

    /**
     * return the thingId, observedPropertyId and lastObservation for all datastream
     * link to sensor
     *
     * @param datastreamIds
     * @return
     */
    protected DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> getDatastreamidsSensorOp(
            ExtraUseCaseRequest<?> request) {
        ResourceCommand<TimedValue<List<String>>> listDatastreamIds = new ResourceCommand<TimedValue<List<String>>>(
                request.id(), DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "datastreamIds") {

            @Override
            protected Promise<TimedValue<List<String>>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMultiValue(String.class);
            }

        };
        DependentCommand<TimedValue<List<String>>, Map<String, Map<String, TimedValue<?>>>> parentCommand = new DependentCommand<TimedValue<List<String>>, Map<String, Map<String, TimedValue<?>>>>(
                listDatastreamIds) {

            @SuppressWarnings("unchecked")
            @Override
            protected Promise<Map<String, Map<String, TimedValue<?>>>> call(
                    Promise<TimedValue<List<String>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    List<String> datastreamIds = parentResult.getValue().getValue();
                    List<SensinactProvider> datastreamProvs = datastreamIds.stream().map(id -> twin.getProvider(id))
                            .filter(Objects::nonNull).toList();

                    List<Promise<List<TimedValue<?>>>> promisesOfLists = datastreamProvs.stream().map(sp -> {

                        Promise<TimedValue<?>> lastObs = getPromiseLastObservation(sp);

                        Promise<TimedValue<?>> sensorId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "sensorId").getValue(String.class);

                        Promise<TimedValue<?>> thingId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "thingId").getValue(String.class);

                        return pf.all(List.of(lastObs, sensorId, thingId));
                    }).toList();

                    return pf.all(promisesOfLists).map(resolvedLists -> {
                        Map<String, Map<String, TimedValue<?>>> finalMap = new HashMap<>();

                        for (int i = 0; i < datastreamProvs.size(); i++) {
                            List<TimedValue<?>> list = resolvedLists.get(i);
                            finalMap.put(datastreamProvs.get(i).getName(), Map.of("lastObservation", list.get(0),
                                    "sensorId", list.get(1), "thingId", list.get(2)));
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
                    List<SensinactProvider> datastreamProvs = map.keySet().stream().map(id -> twin.getProvider(id))
                            .filter(Objects::nonNull).toList();

                    Map<String, Promise<TimedValue<List<String>>>> promisesMap = new LinkedHashMap<>();

                    datastreamProvs.stream().forEach(spDatastream -> {

                        String thingId = (String) map.get(spDatastream.getName()).get("thingId").getValue();
                        String sensorId = (String) map.get(spDatastream.getName()).get("sensorId").getValue();

                        promisesMap.put(thingId,
                                twin.getProvider(thingId).getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds")
                                        .getMultiValue(String.class));
                        promisesMap.put(sensorId,
                                twin.getProvider(sensorId).getResource(DtoMapperSimple.SERVICE_SENSOR, "datastreamIds")
                                        .getMultiValue(String.class));
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

    @SuppressWarnings("unchecked")
    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ObservedProperty> request) {

        DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> parentCommand = getDatastreamidsSensorOp(
                request);

        return new DependentCommand<Map<String, List<?>>, List<Void>>(parentCommand) {

            @Override
            protected Promise<List<Void>> call(Promise<Map<String, List<?>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                    SensinactProvider sp = twin.getProvider(providerId);
                    Map<String, List<?>> map = parentResult.getValue();
                    Map<String, List<TimedValue<List<String>>>> mapDatastreamThingSensor = map.entrySet().stream()
                            .filter(entry -> {
                                List<?> list = entry.getValue();
                                return !list.isEmpty() && list.get(0) instanceof TimedValue<?>;
                            }).collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> (List<TimedValue<List<String>>>) entry.getValue()));
                    List<String> datastreamIds = map.entrySet().stream().filter(entry -> {
                        List<?> list = entry.getValue();
                        return !list.isEmpty() && list.get(0) instanceof String;
                    }).flatMap(entry -> ((List<String>) entry.getValue()).stream()).toList();
                    mapDatastreamThingSensor.entrySet().stream().forEach(entry -> {
                        SensinactProvider prov = twin.getProvider(entry.getKey());
                        SensinactResource datastreamToChangeIds = prov.getResource(DtoMapperSimple.SERVICE_THING,
                                "datastreamIds") != null
                                        ? prov.getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds")
                                        : prov.getResource(DtoMapperSimple.SERVICE_SENSOR, "datastreamIds");

                        updateSensorOrOpDatastreamIds(datastreamIds, entry, datastreamToChangeIds);

                    });

                    if (sp != null) {
                        if (datastreamIds != null && datastreamIds.size() > 0) {
                            datastreamIds.stream().map(id -> twin.getProvider(id)).filter(Objects::nonNull)
                                    .forEach(spDatastream -> {
                                        spDatastream.delete();
                                    });
                        }
                        sp.delete();

                    }
                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

            private void updateSensorOrOpDatastreamIds(List<String> datastreamIds,
                    Entry<String, List<TimedValue<List<String>>>> entry, SensinactResource datastreamToChangeIds) {
                entry.getValue().stream().forEach(tv -> {
                    List<String> datastreamElemIds = tv.getValue();
                    List<String> newList = datastreamElemIds.stream().filter(id -> !datastreamIds.contains(id))
                            .toList();
                    datastreamToChangeIds.setValue(newList);
                });
            }

        };
    }

}
