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
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseModelDelete<M extends Id, S> extends AbstractExtraUseCaseModel<M, S> {

    public abstract AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<M> request);

    public AbstractExtraUseCaseModelDelete(Providers providers, Application application) {
        super(providers, application);
    }

    protected FeatureOfInterest getFeatureOfInterest(SensiNactSession session, FeatureOfInterest foi) {
        // retrieve created sensor
        if (foi != null) {
            if (DtoToModelMapper.isRecordOnlyField(foi, "id")) {
                String idFoi = DtoToModelMapper.getIdFromRecord(foi);
                ProviderSnapshot providerFoi = providerUseCase.read(session, idFoi);
                return DtoMapperSimple.toFeatureOfInterest(providerFoi, idFoi, null, null);
            } else {
                foi = new FeatureOfInterest(null, foi.id() != null ? foi.id() : DtoToModelMapper.getNewId(), foi.name(),
                        foi.description(), foi.encodingType(), foi.feature(), Map.of(), null);
            }
        }
        return foi;
    }

    protected FeatureOfInterest getFeatureOfInterest(SensiNactSession session, String idFoi) {
        // retrieve created sensor
        ProviderSnapshot providerFoi = providerUseCase.read(session, idFoi + "foi");
        if (providerFoi == null) {
            return null;
        }
        return DtoMapperSimple.toFeatureOfInterest(providerFoi, idFoi + "foi", null, null);

    }

    @SuppressWarnings("unchecked")
    protected Promise<TimedValue<?>> getPromiseLastObservation(SensinactProvider sp) {
        Promise<TimedValue<?>> lastObs = sp.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation") != null
                ? (Promise<TimedValue<?>>) (Promise<?>) sp
                        .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation").getValue(String.class)
                : null;
        return lastObs;
    }

    protected void updateSensorDatastreamIds(ExtraUseCaseRequest<?> request, String thingId, SensinactDigitalTwin twin,
            List<Promise<Void>> list, List<String> datastreamIdsOp) {
        if (datastreamIdsOp != null) {
            List<String> newDatastreamIds = datastreamIdsOp.stream().filter(id -> !id.equals(request.id())).toList();
            SensinactResource resource = twin.getResource(thingId, DtoMapperSimple.SERVICE_THING, "datastreamIds");
            list.add(resource.setValue(newDatastreamIds));
        }
    }

    protected void updateObservedPropertyDatastreamIds(ExtraUseCaseRequest<?> request, String opId,
            SensinactDigitalTwin twin, List<Promise<Void>> list, List<String> datastreamIdsOp) {
        if (datastreamIdsOp != null) {
            List<String> newDatastreamIds = datastreamIdsOp.stream().filter(id -> !id.equals(request.id())).toList();
            SensinactResource resource = twin.getResource(opId, DtoMapperSimple.SERVICE_OBSERVED_PROPERTY,
                    "datastreamIds");
            list.add(resource.setValue(newDatastreamIds));
        }
    }

    protected void updateThingDatastreamIds(ExtraUseCaseRequest<?> request, String sensorId, SensinactDigitalTwin twin,
            List<Promise<Void>> list, List<String> datastreamIds) {
        if (datastreamIds != null) {
            List<String> newDatastreamIds = datastreamIds.stream().filter(id -> !id.equals(request.id())).toList();
            SensinactResource resource = twin.getResource(sensorId, DtoMapperSimple.SERVICE_SENSOR, "datastreamIds");
            list.add(resource.setValue(newDatastreamIds));
        }
    }

    protected void saveObservationHistoryMemory(IDtoMemoryCache<ExpandedObservation> cacheObs,
            ExtraUseCaseRequest<?> request, String obsStr, Instant obsStamp) {
        ExpandedObservation lastObs = parseObservation(request.mapper(), obsStr);
        if (lastObs != null) {
            ExpandedObservation obsDeleted = getObservationDeleted(lastObs);
            cacheObs.addDto(obsDeleted.id() + "~" + DtoMapperSimple.stampToId(obsStamp), obsDeleted);
        }
    }

    protected ExpandedObservation getObservationDeleted(ExpandedObservation obs) {
        return new ExpandedObservation(obs.selfLink(), obs.id(), obs.phenomenonTime(), obs.resultTime(), obs.result(),
                obs.resultQuality(), obs.validTime(), obs.parameters(), obs.properties(), obs.datastreamLink(),
                obs.featureOfInterestLink(), obs.datastream(), obs.featureOfInterest(), true);
    }

    /**
     * Returns the thingId, other entity ID and lastObservation for all datastreams
     * linked to a sensor or observedProperty.
     *
     * @param request             the use case request
     * @param initialService      the service to read initial datastreamIds from (e.g. SERVICE_OBSERVED_PROPERTY or SERVICE_SENSOR)
     * @param otherFieldInDatastream the field name in the datastream service for the other entity (e.g. "sensorId" or "observedPropertyId")
     * @param otherFieldKey       the key used in the result map (e.g. "sensorId" or "opId")
     * @param otherService        the service of the other entity (e.g. SERVICE_SENSOR or SERVICE_OBSERVED_PROPERTY)
     * @return a command that resolves to a map of entity IDs to their datastreamIds
     */
    protected DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> getDatastreamidsSensorOp(
            ExtraUseCaseRequest<?> request, String initialService, String otherFieldInDatastream,
            String otherFieldKey, String otherService) {
        ResourceCommand<TimedValue<List<String>>> listDatastreamIds = new ResourceCommand<TimedValue<List<String>>>(
                request.id(), initialService, "datastreamIds") {

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

                        Promise<TimedValue<?>> otherId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, otherFieldInDatastream)
                                .getValue(String.class);

                        Promise<TimedValue<?>> thingId = (Promise<TimedValue<?>>) (Promise<?>) sp
                                .getResource(DtoMapperSimple.SERVICE_DATASTREAM, "thingId").getValue(String.class);

                        return pf.all(List.of(lastObs, otherId, thingId));
                    }).toList();

                    return pf.all(promisesOfLists).map(resolvedLists -> {
                        Map<String, Map<String, TimedValue<?>>> finalMap = new HashMap<>();

                        for (int i = 0; i < datastreamProvs.size(); i++) {
                            List<TimedValue<?>> list = resolvedLists.get(i);
                            finalMap.put(datastreamProvs.get(i).getName(), Map.of("lastObservation", list.get(0),
                                    otherFieldKey, list.get(1), "thingId", list.get(2)));
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
                    List<SensinactProvider> datastreamProvs = datastreamIds.stream().map(id -> twin.getProvider(id))
                            .filter(Objects::nonNull).toList();

                    Map<String, Promise<TimedValue<List<String>>>> promisesMap = new LinkedHashMap<>();

                    datastreamProvs.stream().forEach(spDatastream -> {
                        String thingId = (String) map.get(spDatastream.getName()).get("thingId").getValue();
                        String otherId = (String) map.get(spDatastream.getName()).get(otherFieldKey).getValue();

                        promisesMap.put(thingId,
                                twin.getProvider(thingId).getResource(DtoMapperSimple.SERVICE_THING, "datastreamIds")
                                        .getMultiValue(String.class));
                        promisesMap.put(otherId,
                                twin.getProvider(otherId).getResource(otherService, "datastreamIds")
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
    public ExtraUseCaseResponse<S> create(ExtraUseCaseRequest<M> request) {
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
            return new ExtraUseCaseResponse<S>(false, "can't find sensor");
        }
        return new ExtraUseCaseResponse<S>(request.id(), (S) snapshot);
    }

    @SuppressWarnings("unchecked")
    public ExtraUseCaseResponse<S> update(ExtraUseCaseRequest<M> request) {
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
            return new ExtraUseCaseResponse<S>(false, "can't find sensor");
        }
        return new ExtraUseCaseResponse<S>(request.id(), (S) snapshot);
    }

    public ExtraUseCaseResponse<S> delete(ExtraUseCaseRequest<M> request) {
        try {
            String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());
            ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
            if (provider == null) {
                throw new NotFoundException();
            }
            AbstractSensinactCommand<?> command = dtoToDelete(request);
            if (command != null)
                gatewayThread.execute(command).getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            if (e.getCause() instanceof WebApplicationException) {
                throw (WebApplicationException) e.getCause();
            }
            throw new InternalServerErrorException(e);
        }
        return new ExtraUseCaseResponse<S>(true, "datastream deleted");

    }
}
