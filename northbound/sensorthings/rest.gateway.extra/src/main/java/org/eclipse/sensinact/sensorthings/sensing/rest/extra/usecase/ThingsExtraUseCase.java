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

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing Thing
 */
public class ThingsExtraUseCase extends AbstractExtraUseCase<ExpandedThing, ProviderSnapshot> {

    private DataUpdate dataUpdate;

    private IAccessProviderUseCase providerUseCase;

    public ThingsExtraUseCase(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<ExpandedThing> request) {
        String id = getId(request);
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

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
    public String getId(ExtraUseCaseRequest<ExpandedThing> request) {
        return request.id() != null ? request.id()
                : DtoToModelMapper
                        .sanitizeId(request.model().id() != null ? request.model().id() : request.model().name());
    }

    public ExtraUseCaseResponse<ProviderSnapshot> delete(ExtraUseCaseRequest<ExpandedThing> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    public ExtraUseCaseResponse<ProviderSnapshot> patch(ExtraUseCaseRequest<ExpandedThing> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedThing> request) {
        // check if Thing already exists with location get locations
        List<String> locationIds = new ArrayList<String>();
        checkRequireField(request);
        String id = getId(request);
        ProviderSnapshot provider = providerUseCase.read(request.session(), id);
        if (provider != null) {
            ResourceSnapshot resource = provider.getResource("thing", "locationIds");
            if (resource.getValue() != null)
                locationIds.addAll((List<String>) resource.getValue().getValue());
        }

        return DtoToModelMapper.toThingUpdates(request.model(), request.id(), locationIds);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<ExpandedThing> request) {

        List<SensorThingsUpdate> listDtoModels = toDtos(request);

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

}
