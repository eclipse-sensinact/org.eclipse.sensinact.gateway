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
import java.util.List;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.osgi.util.promise.Promise;

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
public abstract class AbstractExtraUseCaseDtoDelete<M extends Id, S> extends AbstractExtraUseCaseDto<M, S> {

    public abstract AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<M> request);

    public AbstractExtraUseCaseDtoDelete(Providers providers, Application application) {
        super(providers, application);
    }

    protected void saveObservationHistoryMemory(IDtoMemoryCache<ExpandedObservation> cacheObs,
            ExtraUseCaseRequest<?> request, String obsStr, Instant obsStamp) {
        ExpandedObservation lastObs = parseObservation(request.mapper(), obsStr);
        if (lastObs != null) {
            ExpandedObservation obsDeleted = getObservationDeleted(lastObs);
            cacheObs.addDto(obsDeleted.id() + "~" + DtoMapperSimple.stampToId(obsStamp), obsDeleted);
        }
    }

    protected List<Promise<Void>> removeDatastream(SensinactDigitalTwin twin, String providerId) {
        ArrayList<Promise<Void>> list = new ArrayList<Promise<Void>>();

        SensinactResource id = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM, "id");
        SensinactResource friendlyName = twin.getResource(providerId, DtoMapperSimple.SERVICE_ADMIN, "friendlyName");
        SensinactResource description = twin.getResource(providerId, DtoMapperSimple.SERVICE_ADMIN, "description");
        SensinactResource properties = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM, "properties");
        SensinactResource observationType = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                "observationType");
        SensinactResource thingId = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM, "thingId");
        SensinactResource location = twin.getResource(providerId, DtoMapperSimple.SERVICE_ADMIN, "location");

        SensinactResource lastObservation = twin.getResource(providerId, DtoMapperSimple.SERVICE_DATASTREAM,
                "lastObservation");

        return List.of(id.setValue(null), friendlyName.setValue(null), description.setValue(null),
                properties.setValue(null), observationType.setValue(null), thingId.setValue(null),
                location.setValue(null), lastObservation.setValue(null));
    }

    protected ExpandedObservation getObservationDeleted(ExpandedObservation obs) {
        return new ExpandedObservation(obs.selfLink(), obs.id(), obs.phenomenonTime(), obs.resultTime(), obs.result(),
                obs.resultQuality(), obs.validTime(), obs.parameters(), obs.properties(), obs.datastreamLink(),
                obs.featureOfInterestLink(), obs.datastream(), obs.featureOfInterest(), true);
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
