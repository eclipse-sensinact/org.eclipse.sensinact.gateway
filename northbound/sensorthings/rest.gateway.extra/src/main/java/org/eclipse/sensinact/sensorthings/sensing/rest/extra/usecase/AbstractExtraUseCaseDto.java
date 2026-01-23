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

import java.util.List;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

/**
 * abstract use case
 *
 * @param <M>
 * @param <S>
 */
public abstract class AbstractExtraUseCaseDto<M extends Id, S> extends AbstractExtraUseCase<M, S> {

    public abstract List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<M> request);

    protected final DataUpdate dataUpdate;
    protected final IAccessProviderUseCase providerUseCase;
    protected final IAccessServiceUseCase serviceUseCase;
    protected final GatewayThread gatewayThread;

    public static String getLink(UriInfo uriInfo, String baseUri, String path) {
        String sensorLink = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).build().toString();
        return sensorLink;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path, String id) {
        if (id == null) {
            id = "null";
        }
        String link = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).resolveTemplate("id", id).build().toString();
        return link;
    }

    protected void checkRequireField(ExtraUseCaseRequest<M> request) {
        try {
            if (HttpMethod.POST.equals(request.method()) || HttpMethod.PUT.equals(request.method())) {
                DtoMapperSimple.checkRequireField(request.model());
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    protected GeoJsonObject getObservedArea(SensiNactSession session, String datastreamId) {
        ProviderSnapshot providerDatastream = providerUseCase.read(session, datastreamId);
        GeoJsonObject observedArea = null;

        if (providerDatastream != null) {
            observedArea = DtoMapperSimple.getResourceField(DtoMapperSimple.getAdminService(providerDatastream),
                    DtoMapperSimple.LOCATION, GeoJsonObject.class);
        }
        return observedArea;
    }

    public AbstractExtraUseCaseDto(Providers providers) {
        dataUpdate = resolve(providers, DataUpdate.class);
        providerUseCase = resolve(providers, IAccessProviderUseCase.class);
        serviceUseCase = resolve(providers, IAccessServiceUseCase.class);
        gatewayThread = resolve(providers, GatewayThread.class);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getDatastreamIds(ServiceSnapshot serviceThing) {
        return DtoMapperSimple.getResourceField(serviceThing, "datastreamIds", List.class);

    }

    protected List<String> getLocationIds(ProviderSnapshot provider) {
        return getLocationIds(DtoMapperSimple.getThingService(provider));

    }

    protected List<String> getDatastreamIds(ProviderSnapshot provider) {
        return getDatastreamIds(DtoMapperSimple.getThingService(provider));
    }

    @SuppressWarnings("unchecked")
    protected List<String> getLocationIds(ServiceSnapshot serviceThing) {
        return DtoMapperSimple.getResourceField(serviceThing, "locationIds", List.class);

    }
}
