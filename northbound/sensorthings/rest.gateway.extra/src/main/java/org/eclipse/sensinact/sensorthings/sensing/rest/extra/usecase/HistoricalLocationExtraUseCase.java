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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing Thing
 */
public class HistoricalLocationExtraUseCase
        extends AbstractExtraUseCaseDtoDelete<HistoricalLocation, ProviderSnapshot> {

    IDtoMemoryCache<Instant> cacheHl;

    @SuppressWarnings("unchecked")
    public HistoricalLocationExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        cacheHl = resolve(providers, IDtoMemoryCache.class, Instant.class);
    }

    public ExtraUseCaseResponse<ProviderSnapshot> create(ExtraUseCaseRequest<HistoricalLocation> request) {
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "cant create historical location");
    }

    public ExtraUseCaseResponse<ProviderSnapshot> update(ExtraUseCaseRequest<HistoricalLocation> request) {

        if (isHistoryMemory()) {
            cacheHl.addDto(request.id(), request.model().time());
            return new ExtraUseCaseResponse<ProviderSnapshot>(request.id(), null);
        }
        return new ExtraUseCaseResponse<ProviderSnapshot>(false, "not implemented");

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<HistoricalLocation> request) {
        // delete value location for the thing
        String ThingId = DtoMapperSimple.extractFirstIdSegment(request.id());

        Instant timestamp = DtoToModelMapper.getTimestampFromId(request.id());
        ResourceSnapshot resourceSnapshot = getResourceLocationFromThing(request, ThingId);
        Instant milliTimestamp = resourceSnapshot.getValue().getTimestamp().truncatedTo(ChronoUnit.MILLIS);

        if (isHistoryMemory() && cacheHl.getDto(request.id()) != null) {
            cacheHl.removeDto(request.id());
        } else if (!milliTimestamp.equals(timestamp)) {
            // TODO in history provider return 400
            if (isHistoryMemory()) {
                throw new NotFoundException();
            }
            throw new WebApplicationException("historical location are immutable", 409);
        }
        if (milliTimestamp.equals(timestamp)) {
            return new AbstractTwinCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                    String thingId = DtoMapperSimple.extractFirstIdSegment(request.id());

                    SensinactProvider sp = twin.getProvider(thingId);
                    SensinactResource resource = sp.getResource(DtoMapperSimple.SERVICE_ADMIN, "location");

                    return resource.setValue(null);
                }
            };
        }
        return null;
    }

    private ResourceSnapshot getResourceLocationFromThing(ExtraUseCaseRequest<HistoricalLocation> request,
            String ThingId) {
        ProviderSnapshot provider = providerUseCase.read(request.session(), ThingId);
        ServiceSnapshot serviceAdmin = DtoMapperSimple.getAdminService(provider);
        ResourceSnapshot resourceSnapshot = serviceAdmin.getResource("location");
        return resourceSnapshot;
    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<HistoricalLocation> request) {
        // do nothing
        return List.of();
    }

}
