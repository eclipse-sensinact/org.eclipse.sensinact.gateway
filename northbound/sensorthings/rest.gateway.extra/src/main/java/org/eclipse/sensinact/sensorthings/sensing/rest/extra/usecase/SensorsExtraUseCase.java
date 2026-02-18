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
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
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
 * sensor
 */
public class SensorsExtraUseCase extends AbstractExtraUseCaseModelDelete<Sensor, ProviderSnapshot> {

    public SensorsExtraUseCase(Providers providers, Application application) {
        super(providers, application);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<Sensor> request) {
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
    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<Sensor> request) {

        return super.delete(request);

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
                request.id(), DtoMapperSimple.SERVICE_SENSOR, "datastreamIds") {

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
                    List<SensinactProvider> datastreamProv = datastreamIds.stream().map(id -> twin.getProvider(id))
                            .filter(Objects::nonNull).toList();
                    List<Promise<List<TimedValue<?>>>> promisesOfLists = datastreamProv.stream().map(sp -> {
                        Promise<TimedValue<?>> lastObs = getPromiseLastObservation(sp);

                        Promise<TimedValue<?>> opId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "observedPropertyId")
                                .getValue(String.class);

                        Promise<TimedValue<?>> thingId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "thingId").getValue(String.class);

                        return pf.all(List.of(lastObs, opId, thingId));
                    }).toList();

                    return pf.all(promisesOfLists).map(resolvedLists -> {
                        Map<String, Map<String, TimedValue<?>>> finalMap = new HashMap<>();

                        for (int i = 0; i < datastreamProv.size(); i++) {
                            List<TimedValue<?>> list = resolvedLists.get(i);
                            finalMap.put(datastreamProv.get(i).getName(), Map.of("lastObservation", list.get(0), "opId",
                                    list.get(1), "thingId", list.get(2)));
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

                    datastreamIds.stream().map(id -> twin.getProvider(id)).filter(Objects::nonNull)
                            .forEach(spDatastream -> {
                                // cache dto for Foi

                                String thingId = (String) map.get(spDatastream.getName()).get("thingId").getValue();
                                String opId = (String) map.get(spDatastream.getName()).get("opId").getValue();

                                promisesMap.put(thingId,
                                        twin.getProvider(thingId)
                                                .getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds")
                                                .getMultiValue(String.class));
                                promisesMap.put(opId,
                                        twin.getProvider(opId)
                                                .getResource(DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "datastreamIds")
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

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<Sensor> request) {

        DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> parentCommand = getDatastreamidsSensorOp(
                request);

        return new DependentCommand<Map<String, List<?>>, List<Void>>(parentCommand) {

            @SuppressWarnings("unchecked")
            @Override
            protected Promise<List<Void>> call(Promise<Map<String, List<?>>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
                    SensinactProvider sp = twin.getProvider(providerId);
                    Map<String, List<?>> map = parentResult.getValue();
                    Map<String, List<TimedValue<List<String>>>> mapDatastreamThingOp = map.entrySet().stream()
                            .filter(entry -> {
                                List<?> list = entry.getValue();
                                return !list.isEmpty() && list.get(0) instanceof TimedValue<?>;
                            }).collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> (List<TimedValue<List<String>>>) entry.getValue()));
                    List<String> datastreamIds = map.entrySet().stream().filter(entry -> {
                        List<?> list = entry.getValue();
                        return !list.isEmpty() && list.get(0) instanceof String;
                    }).flatMap(entry -> ((List<String>) entry.getValue()).stream()).toList();
                    mapDatastreamThingOp.entrySet().stream().forEach(entry -> {
                        SensinactProvider prov = twin.getProvider(entry.getKey());
                        SensinactResource datastreamToChangeIds = prov.getResource(DtoMapperSimple.SERVICE_THING,
                                "datastreamIds") != null
                                        ? prov.getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds")
                                        : prov.getResource(DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "datastreamIds");

                        entry.getValue().stream().forEach(tv -> {
                            List<String> datastreamElemIds = tv.getValue();
                            List<String> newList = datastreamElemIds.stream().filter(id -> !datastreamIds.contains(id))
                                    .toList();
                            datastreamToChangeIds.setValue(newList);
                        });

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
        };
    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<Sensor> request) {
        String sensorId = request.id();
        if (sensorId == null) {
            throw new BadRequestException("bad id format");
        }
        Sensor receivedSensor = request.model();
        checkRequireField(request);
        ProviderSnapshot provider = providerUseCase.read(request.session(), request.id());
        List<String> datastreamIds = List.of();
        if (provider != null) {
            datastreamIds = DtoToModelMapper.getDatastreamIdsFromSensor(provider);
        }
        Sensor sensorToUpdate = new Sensor(null, sensorId, receivedSensor.name(), receivedSensor.description(),
                receivedSensor.encodingType(), receivedSensor.metadata(), receivedSensor.properties(), null);
        return List.of(DtoToModelMapper.toSensorUpdate(request.id(), sensorToUpdate, datastreamIds, null));

    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<Sensor> request) {
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

}
