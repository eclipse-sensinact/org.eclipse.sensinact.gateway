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

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
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
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());

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
            locationIds = getLocationIds(provider);
            datastreamIds = getDatastreamIds(provider);
        }

        return DtoToModelMapper.toThingUpdates(request.model(), id, locationIds, datastreamIds);
    }

    @SuppressWarnings("unchecked")
    private List<String> getLocationIds(ProviderSnapshot provider) {
        ResourceSnapshot resource = provider.getResource("thing", "locationIds");
        if (resource.getValue() != null)
            return (List<String>) resource.getValue().getValue();
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<String> getDatastreamIds(ProviderSnapshot provider) {
        ResourceSnapshot resource;
        resource = provider.getResource("thing", "datastreamIds");
        if (resource.getValue() != null)
            return (List<String>) resource.getValue().getValue();
        return List.of();
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedThing> request) {

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(false, "fail to create");

        }

        ProviderSnapshot snapshot = providerUseCase.read(request.session(), request.id());
        if (snapshot != null) {
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), snapshot);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    @Override
    public List<AbstractSensinactCommand<?>> dtoToDelete(ExtraUseCaseRequest<ExpandedThing> request) {
        List<AbstractSensinactCommand<?>> list = new ArrayList<AbstractSensinactCommand<?>>();
        ServiceSnapshot service = serviceUseCase.read(request.session(), request.id(), "thing");
        @SuppressWarnings("unchecked")
        List<String> datastreamIds = (List<String>) UtilDto.getResourceField(service, "datastreamIds", Object.class);

        list.add(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(request.id());
                if (sp != null) {
                    sp.delete();
                }
                datastreamIds.stream().forEach(idDatastream -> {
                    SensinactProvider spDatastream = twin.getProvider(idDatastream);
                    if (spDatastream != null) {
                        spDatastream.delete();
                    }
                });
                return pf.resolved(null);
            }
        });

        return list;
    }

}
