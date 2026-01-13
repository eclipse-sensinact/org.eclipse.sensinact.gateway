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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.eNS_URI;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.IndependentCommands;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.LocationUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
public class LocationsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedLocation, ServiceSnapshot> {

    public LocationsExtraUseCase(Providers providers) {
        super(providers);
    }

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            LocationUpdate locationUpdate = (LocationUpdate) listDtoModels.stream().filter(s -> {
                return s instanceof LocationUpdate;
            }).findFirst().get();

            ProviderSnapshot provider = providerUseCase.read(request.session(), locationUpdate.providerId());
            if (provider != null) {
                String locationId = request.id();
                return new ExtraUseCaseResponse<ServiceSnapshot>(locationId, UtilDto.getLocationService(provider));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "failed to create Location");

        } catch (Exception e) {
            throw new InternalServerErrorException(e);

        }

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedLocation> request) {
        // read thing for each location and update it
        ExpandedLocation location = request.model();
        checkRequireField(request);

        List<SensorThingsUpdate> listUpdates = DtoToModelMapper.toLocationUpdates(request.model(), request.id());
        if (location.things() != null && location.things().size() >= 0 || request.parentId() != null) {
            List<String> listThingIds = new ArrayList<String>();

            if (location.things() != null && location.things().size() >= 0) {
                listThingIds.addAll(location.things().stream().map(refId -> (String) refId.id()).toList());
            }
            if (request.parentId() != null) {
                listThingIds.add(request.parentId());
            }

            listThingIds.stream().map(providerId -> {
                ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);

                List<String> ids = getLocationIds(provider);
                String locationId = request.id();
                if (!ids.contains(locationId)) {
                    ids = Stream.concat(ids.stream(), Stream.of(locationId)).toList();
                    return new ThingUpdate(providerId, DtoToModelMapper.getAggregateLocation(request, ids), null, null,
                            providerId, null, ids, null);
                }
                return null;
            }).filter(java.util.Objects::nonNull).forEach(listUpdates::add);
        }
        return listUpdates;
    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            LocationUpdate locationUpdate = (LocationUpdate) listDtoModels.get(0);

            ProviderSnapshot provider = providerUseCase.read(request.session(), locationUpdate.providerId());
            if (provider != null) {
                String locationId = request.id();
                return new ExtraUseCaseResponse<ServiceSnapshot>(locationId, UtilDto.getLocationService(provider));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedLocation> request) {
        // delete location with link between location and thing
        String locationId = request.id();
        ;
        AbstractSensinactCommand<Void> deleteLocationCommand = new AbstractSensinactCommand<Void>() {

            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(locationId);
                if (sp != null) {
                    sp.delete();
                }
                return pf.resolved(null);
            }
        };
        List<AbstractSensinactCommand<?>> listCommand = List.of(deleteLocationThingsLink(locationId, null),
                deleteLocationCommand);
        return new IndependentCommands<>(listCommand);

    }

    /**
     * delete all location list in parameter and all linked thing association
     *
     * @param locationIdsToDelete
     * @return
     */
    public DependentCommand<Map<String, TimedValue<List<String>>>, Void> deleteLocationThingsLink(String locationId,
            String thingId) {
        AbstractSensinactCommand<Map<String, TimedValue<List<String>>>> thingsListProviderCommand = getCommandThingProviders(
                thingId);
        // remove locationId in providerThing->thing->resource(locationIds)
        return new DependentCommand<Map<String, TimedValue<List<String>>>, Void>(thingsListProviderCommand) {

            @Override
            protected Promise<Void> call(Promise<Map<String, TimedValue<List<String>>>> parentResult,
                    SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {

                try {
                    Map<String, TimedValue<List<String>>> mapLocationIdsByProvider = parentResult.getValue();

                    List<Promise<Void>> promises = mapLocationIdsByProvider.entrySet().stream().map(es -> {
                        TimedValue<List<String>> timedValue = es.getValue();

                        List<String> newLocationsList = timedValue.getValue().stream()
                                .filter(id -> !id.equals(locationId)).toList();
                        return twin.getResource(es.getKey(), UtilDto.SERVICE_THING, "locationIds")
                                .setValue(newLocationsList);
                    }).toList();

                    return pf.all(promises).map(l -> null);

                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }
            }

        };
    }

    /**
     *
     * return list of single provider for thing in parameter else all thing provider
     *
     * @param thingId
     * @return
     */
    private AbstractSensinactCommand<Map<String, TimedValue<List<String>>>> getCommandThingProviders(String thingId) {
        AbstractSensinactCommand<Map<String, TimedValue<List<String>>>> thingsListProviderCommand = new AbstractTwinCommand<Map<String, TimedValue<List<String>>>>() {
            @Override
            protected Promise<Map<String, TimedValue<List<String>>>> call(SensinactDigitalTwin twin,
                    PromiseFactory pf) {

                List<? extends SensinactProvider> providers = thingId == null
                        ? twin.getProviders(eNS_URI, SENSOR_THING_DEVICE.getName())
                        : List.of(twin.getProvider(thingId));

                List<Promise<Map.Entry<String, TimedValue<List<String>>>>> promises = providers.stream()
                        .map(p -> p.getResource(UtilDto.SERVICE_THING, "locationIds").getMultiValue(String.class)
                                .map(tv -> Map.entry(p.getName(), tv)))
                        .toList();

                return pf.all(promises)
                        .map(e -> e.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
        };
        return thingsListProviderCommand;
    }

}
