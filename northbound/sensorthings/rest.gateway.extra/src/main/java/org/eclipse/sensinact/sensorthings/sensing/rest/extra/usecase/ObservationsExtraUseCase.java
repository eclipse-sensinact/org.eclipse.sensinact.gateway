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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.DependentCommand;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.DependsOnUseCases;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * observation
 */
@DependsOnUseCases(value = { FeatureOfInterestExtraUseCase.class })
public class ObservationsExtraUseCase extends AbstractExtraUseCaseDtoDelete<ExpandedObservation, ServiceSnapshot> {

    private FeatureOfInterestExtraUseCase featureOfInterestUseCase;
    private IDtoMemoryCache<ExpandedObservation> cacheObs;
    private IDtoMemoryCache<FeatureOfInterest> cacheFoi;

    @SuppressWarnings("unchecked")
    public ObservationsExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        featureOfInterestUseCase = resolveUseCase(providers, FeatureOfInterestExtraUseCase.class);
        featureOfInterestUseCase.setObservationExtraUseCase(this);
        cacheObs = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
        cacheFoi = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);

    }

    private void checkNoInline(ExtraUseCaseRequest<ExpandedObservation> request) {
        if (request.model().featureOfInterest() != null
                && !DtoMapperSimple.isRecordOnlyField(request.model().featureOfInterest(), "id")) {
            throw new BadRequestException("featureOfInterest no expected for patch or update");
        }

    }

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedObservation> request) {
        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);
        ResourceSnapshot resource = getObservationForMemoryHistory(request.session(), getProviderId(request));

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }

        updateObservationMemoryHistory(cacheObs, cacheFoi, request.mapper(), resource);
        ServiceSnapshot service = serviceUseCase.read(request.session(), getProviderId(request), "datastream");
        if (service != null) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(getProviderId(request), service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<ExpandedObservation> request) {
        // read thing for each location and update it
        ExpandedObservation observation = request.model();
        checkRequireField(request);
        // parent can be datastream or featureOfInterest TODO
        String id = getProviderId(request);
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot provider = providerUseCase.read(request.session(), providerId);
        FeatureOfInterest foi = getFeatureOfInterest(observation);
        if (foi == null) {
            String thingId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(provider), "thingId",
                    String.class);
            ProviderSnapshot providerThing = providerUseCase.read(request.session(), thingId);
            GeoJsonObject feature = DtoMapperSimple.getResourceField(DtoMapperSimple.getAdminService(providerThing),
                    "location", GeoJsonObject.class);
            foi = new FeatureOfInterest(null, DtoToModelMapper.getNewId(), "default", "default feature of interest",
                    "application/vnd.geo+json", feature, Map.of(), null);

        }
        checkRequireField(foi);

        checkRequireLink(provider);
        SensorThingsUpdate update = DtoToModelMapper.toDatastreamUpdate(request.mapper(), providerId,
                getObservedArea(request.session(), providerId), null, DtoToModelMapper.toDatastream(provider), null,
                null, null, observation, foi);
        return List.of(update);

    }

    private String getProviderId(ExtraUseCaseRequest<ExpandedObservation> request) {
        String id = request.model().datastream() == null ? request.parentId()
                : (String) request.model().datastream().id();
        if (id == null) {
            id = DtoMapperSimple.extractFirstIdSegment(request.id());
        }
        return id;
    }

    private void checkRequireField(FeatureOfInterest foi) {
        try {
            DtoMapperSimple.checkRequireField(foi);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private FeatureOfInterest getFeatureOfInterest(ExpandedObservation observation) {
        FeatureOfInterest foi = null;
        // retrieve created sensor
        if (observation.featureOfInterest() != null) {
            if (DtoToModelMapper.isRecordOnlyField(observation.featureOfInterest(), "id")) {
                String idFoi = DtoToModelMapper.getIdFromRecord(observation.featureOfInterest());

                foi = featureOfInterestUseCase.getInMemoryFeatureOfInterest(idFoi);
            } else {
                foi = new FeatureOfInterest(null, DtoToModelMapper.getNewId(), observation.featureOfInterest().name(),
                        observation.featureOfInterest().description(), observation.featureOfInterest().encodingType(),
                        observation.featureOfInterest().feature(), Map.of(), null);
            }
        }
        return foi;
    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedObservation> request) {
        String observationId = request.id();
        Instant stamp = DtoMapperSimple.getTimestampFromId(observationId);
        String providerId = DtoMapperSimple.extractFirstIdSegment(observationId);
        checkNoInline(request);
        if (isHistoryMemory() && cacheObs.getDto(observationId) != null) {
            updateObservationMemoryHistory(cacheObs, request, cacheObs.getDto(observationId), stamp);
        }

        List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);
        // get old observation before update
        ResourceSnapshot resource = getObservationForMemoryHistory(request.session(), providerId);
        checkDeletedObservation(request, resource);
        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            throw new InternalServerErrorException(e);

        }
        updateObservationMemoryHistory(cacheObs, request, resource, stamp);
        String dataStreamId = getProviderId(request);
        ServiceSnapshot service = serviceUseCase.read(request.session(), dataStreamId, "datastream");
        if (service != null) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(observationId, service);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");
    }

    private void checkDeletedObservation(ExtraUseCaseRequest<ExpandedObservation> request, ResourceSnapshot resource) {
        if (resource.getValue() != null) {
            ExpandedObservation obs = parseObservation(request.mapper(), (String) resource.getValue().getValue());
            if (obs.deleted()) {
                throw new NotFoundException();
            }
        }
    }

    private void updateObservationMemoryHistory(IDtoMemoryCache<ExpandedObservation> cacheObs,
            ExtraUseCaseRequest<ExpandedObservation> request, ResourceSnapshot resource, Instant stamp) {
        if (resource != null) {
            ExpandedObservation oldObs = DtoMapperSimple.parseExpandObservation(request.mapper(),
                    resource.getValue().getValue());
            updateObservationMemoryHistory(cacheObs, request, oldObs, stamp);
        }
    }

    private void updateObservationMemoryHistory(IDtoMemoryCache<ExpandedObservation> cacheObs,
            ExtraUseCaseRequest<ExpandedObservation> request, ExpandedObservation oldObs, Instant stamp) {
        ExpandedObservation newObs = request.model();

        ExpandedObservation newOldObs = new ExpandedObservation(oldObs.selfLink(), oldObs.id(),
                newObs.phenomenonTime() == null ? oldObs.phenomenonTime() : newObs.phenomenonTime(),
                newObs.resultTime() == null ? oldObs.resultTime() : newObs.resultTime(),
                newObs.result() == null ? oldObs.result() : newObs.result(),
                newObs.resultQuality() == null ? oldObs.resultQuality() : newObs.resultQuality(),
                newObs.validTime() == null ? oldObs.validTime() : newObs.validTime(),
                newObs.parameters() == null ? oldObs.parameters() : newObs.parameters(),
                newObs.properties() == null ? oldObs.properties() : newObs.properties(), oldObs.datastreamLink(),
                oldObs.featureOfInterestLink(), oldObs.datastream(), oldObs.featureOfInterest(), oldObs.deleted());
        cacheObs.addDto(oldObs.id() + "~" + DtoMapperSimple.stampToId(stamp), newOldObs);

    }

    @Override
    public AbstractSensinactCommand<?> dtoToDelete(ExtraUseCaseRequest<ExpandedObservation> request) {
        String observationId = request.id();
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(observationId);

        ResourceSnapshot resourceSnapshot = resourceUseCase.read(request.session(), datastreamId,
                DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation");
        // allow to get old observatin

        Instant timestamp = DtoMapperSimple.getTimestampFromId(observationId);

        Instant milliTimestamp = resourceSnapshot.getValue().getTimestamp().truncatedTo(ChronoUnit.MILLIS);
        if (isHistoryMemory() && cacheObs.getDto(request.id()) != null) {
            ExpandedObservation obs = cacheObs.getDto(request.id());
            if (obs.deleted()) {
                throw new NotFoundException();
            }
            cacheObs.addDto(request.id(), getObservationDeleted(obs));
            return null;

        } else if (!milliTimestamp.equals(timestamp)) {
            throw new NotFoundException();
        }
        ResourceCommand<TimedValue<String>> parentCommand = new ResourceCommand<TimedValue<String>>(datastreamId,
                DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation") {
            @Override
            protected Promise<TimedValue<String>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue(String.class);

            }
        };
        return new DependentCommand<TimedValue<String>, Void>(parentCommand) {

            @Override
            protected Promise<Void> call(Promise<TimedValue<String>> parentResult, SensinactDigitalTwin twin,
                    SensinactModelManager modelMgr, PromiseFactory pf) {
                try {
                    if (parentResult.getFailure() == null) {
                        String obsStr = parentResult.getValue().getValue();
                        if (obsStr == null) {
                            return pf.failed(new NotFoundException());
                        }
                        ExpandedObservation obs = parseObservation(request.mapper(), obsStr);
                        if (obs == null) {
                            throw new InternalServerErrorException();
                        }
                        if (request.id() == null || !request.id().startsWith((String) obs.id())) {
                            // not the last one, we ask for delete historized obs
                            return pf.resolved(null);
                        }
                        String datastreamId = DtoMapperSimple.extractFirstIdSegment(request.id());

                        SensinactProvider sp = twin.getProvider(datastreamId);
                        SensinactResource resource = sp.getResource("datastream", "lastObservation");
                        ExpandedObservation obsDeleted = getObservationDeleted(obs);
                        cacheObs.addDto(observationId, obsDeleted);
                        return resource.setValue(null);
                    }
                    return pf.resolved(null);
                } catch (InvocationTargetException | InterruptedException e) {
                    return pf.failed(e);
                }

            }
        };
    }

}
