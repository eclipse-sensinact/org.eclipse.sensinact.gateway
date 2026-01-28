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
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing Thing
 */
public class ThingsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedThing, ProviderSnapshot> {

    public ThingsExtraUseCase(Providers providers) {
        super(providers);
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

        return DtoToModelMapper.toThingUpdates(request, id, locationIds, datastreamIds);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedThing> request) {

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

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
                    datastreamIds = parentResult.getValue().getValue();

                    if (datastreamIds != null) {
                        datastreamIds.forEach(id -> {
                            SensinactProvider spDatastream = twin.getProvider(id);
                            if (sp != null) {
                                spDatastream.delete();
                            }
                        });
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
