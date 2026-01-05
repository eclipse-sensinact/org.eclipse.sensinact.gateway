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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.LocationUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
public class LocationsExtraUseCase extends AbstractExtraUseCase<ExpandedLocation, ServiceSnapshot> {

    private IAccessProviderUseCase providerUseCase;

    private DataUpdate dataUpdate;

    public LocationsExtraUseCase(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
    }

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            LocationUpdate locationUpdate = (LocationUpdate) listDtoModels.stream().filter(s -> {
                return s instanceof LocationUpdate;
            }).findFirst().get();

            ProviderSnapshot provider = providerUseCase.read(request.session(), locationUpdate.providerId());
            if (provider != null) {
                String locationId = getId(request);
                return new ExtraUseCaseResponse<ServiceSnapshot>(locationId, provider.getService("locations"));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "failed to create Location");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, new InternalServerErrorException(e),
                    e.getMessage());
        }

    }

    public ExtraUseCaseResponse<ServiceSnapshot> delete(ExtraUseCaseRequest<ExpandedLocation> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedLocation> request) {
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

            listThingIds.stream().filter(providerId -> {
                ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
                ResourceSnapshot resource = provider.getResource("thing", "locationsIds");
                return resource != null && resource.getValue() != null;
            }).map(providerId -> {
                ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
                ResourceSnapshot resource = provider.getResource("thing", "locationsIds");

                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) resource.getValue().getValue();

                ids.add(getId(request));

                return new ThingUpdate(providerId, null, null, providerId, null, ids);
            }).forEach(listUpdates::add);
        }
        return listUpdates;
    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedLocation> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();
            LocationUpdate locationUpdate = (LocationUpdate) listDtoModels.get(0);

            ProviderSnapshot provider = providerUseCase.read(request.session(), locationUpdate.providerId());
            if (provider != null) {
                String locationId = getId(request);
                return new ExtraUseCaseResponse<ServiceSnapshot>(locationId, provider.getService(locationId));
            }
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");

        } catch (Exception e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerProviderSnapshot");
        }

    }

    @Override
    public String getId(ExtraUseCaseRequest<ExpandedLocation> request) {
        return request.id() != null ? request.id()
                : DtoToModelMapper
                        .sanitizeId(request.model().id() != null ? request.model().id() : request.model().name());
    }

}
