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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
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

    @SuppressWarnings("unchecked")
    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ObservedProperty> request) {

        DependentCommand<Map<String, Map<String, TimedValue<?>>>, Map<String, List<?>>> parentCommand = getDatastreamidsSensorOp(
                request, DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "sensorId", "sensorId",
                DtoMapperSimple.SERVICE_SENSOR);

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
