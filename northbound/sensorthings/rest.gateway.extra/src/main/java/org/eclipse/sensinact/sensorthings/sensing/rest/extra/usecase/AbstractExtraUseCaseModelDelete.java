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
import java.util.List;
import java.util.Map;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
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
