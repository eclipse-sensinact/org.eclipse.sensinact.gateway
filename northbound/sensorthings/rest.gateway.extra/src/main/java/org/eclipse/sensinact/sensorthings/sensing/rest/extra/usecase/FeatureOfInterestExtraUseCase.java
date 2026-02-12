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
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * FeatureOfInterest
 */
public class FeatureOfInterestExtraUseCase extends AbstractExtraUseCaseDto<FeatureOfInterest, Object> {

    private final IDtoMemoryCache<FeatureOfInterest> cacheFoi;
    private final IDtoMemoryCache<ExpandedObservation> obsCache;
    private ObservationsExtraUseCase observationExtraUseCase;

    @SuppressWarnings("unchecked")
    public FeatureOfInterestExtraUseCase(Providers providers, Application application) {
        super(providers, application);
        cacheFoi = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);
        obsCache = resolve(providers, IDtoMemoryCache.class, ExpandedObservation.class);
    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<FeatureOfInterest> request) {
        FeatureOfInterest featureOfInterest = request.model();
        checkRequireField(request);
        String featureOfInterestId = request.id();

        String selfLink = getLink(request.uriInfo(), DtoMapperSimple.VERSION, "FeaturesOfInterest({id})",
                featureOfInterestId);
        String observationLink = getLink(request.uriInfo(), selfLink, "Observations");
        FeatureOfInterest createFoi = new FeatureOfInterest(selfLink, featureOfInterestId, featureOfInterest.name(),
                featureOfInterest.description(), featureOfInterest.encodingType(), featureOfInterest.feature(),
                featureOfInterest.properties(), observationLink);
        cacheFoi.addDto(featureOfInterestId, createFoi);
        return new ExtraUseCaseResponse<Object>(featureOfInterestId, createFoi);

    }

    @Override
    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<FeatureOfInterest> request) {
        if (cacheFoi.getDto(request.id()) != null) {
            cacheFoi.removeDto(request.id());
            return new ExtraUseCaseResponse<Object>(true, "feature of interest deleted");
        }
        String[] split = request.id().split("~");
        if (split.length > 3) {
            String idObs = String.format("%s~%s~%s", split[0], split[1], split[3]);
            if (isHistoryMemory() && obsCache.getDto(idObs) != null) {
                obsCache.removeDto(idObs);
                return new ExtraUseCaseResponse<Object>(true, "feature of interest deleted");
            }
            ExtraUseCaseRequest<ExpandedObservation> requestObs = new ExtraUseCaseRequest<ExpandedObservation>(
                    request.session(), request.mapper(), request.uriInfo(), HttpMethod.DELETE, idObs);
            ExtraUseCaseResponse<ServiceSnapshot> result = observationExtraUseCase.delete(requestObs);
            if (result.success()) {
                if (isHistoryMemory() && obsCache.getDto(idObs) != null) {
                    obsCache.removeDto(idObs);
                }
                return new ExtraUseCaseResponse<Object>(true, "feature of interest deleted");
            }
        }
        if (isHistoryMemory())
            throw new NotFoundException();
        else
            throw new WebApplicationException("foi is link to observation so it is immutable", 409);

    }

    @Override
    public List<SensorThingsUpdate> dtosToCreateUpdate(ExtraUseCaseRequest<FeatureOfInterest> request) {
        String providerId = DtoToModelMapper.extractFirstIdSegment(request.id());
        String observationId = DtoToModelMapper.extractSecondIdSegment(request.id());
        String foiId = DtoToModelMapper.extractFouthIdSegment(request.id());

        if (providerId == null || observationId == null || foiId == null) {
            throw new BadRequestException("bad id format");
        }
        FeatureOfInterest receiveFoi = request.model();
        checkRequireField(request);
        FeatureOfInterest foiToUpdate = new FeatureOfInterest(null, foiId, receiveFoi.name(), receiveFoi.description(),
                receiveFoi.encodingType(), receiveFoi.feature(), receiveFoi.properties(), null);
        GeoJsonObject observedArea = getObservedArea(request.session(), providerId);

        ExpandedObservation lastObservation = getExpandedObservationFromService(request,
                serviceUseCase.read(request.session(), providerId, DtoMapperSimple.SERVICE_DATASTREAM));
        return List.of(DtoToModelMapper.toDatastreamUpdate(request.mapper(), providerId, observedArea, null, null, null,
                null, null, lastObservation, foiToUpdate));

    }

    private FeatureOfInterest updateInMemoryFoi(ExtraUseCaseRequest<FeatureOfInterest> request, FeatureOfInterest foi) {
        FeatureOfInterest updateFoi = request.model();
        String selfLink = DtoToModelMapper.getLink(request.uriInfo(), DtoMapperSimple.VERSION,
                "/FeaturesofInterest({id})", request.id());

        String observationsLink = DtoToModelMapper.getLink(request.uriInfo(), selfLink, "/Observationos");
        FeatureOfInterest createFoi = new FeatureOfInterest(selfLink, request.id(),
                updateFoi.name() != null ? updateFoi.name() : foi.name(),
                updateFoi.description() != null ? updateFoi.description() : foi.description(),
                updateFoi.encodingType() != null ? updateFoi.encodingType() : foi.encodingType(),
                updateFoi.feature() != null ? updateFoi.feature() : foi.feature(),
                updateFoi.properties() != null ? updateFoi.properties() : foi.properties(), observationsLink);
        cacheFoi.addDto(request.id(), createFoi);
        return createFoi;
    }

    public ExtraUseCaseResponse<Object> update(ExtraUseCaseRequest<FeatureOfInterest> request) {
        // check if sensor is in cached map
        FeatureOfInterest property = cacheFoi.getDto(request.id());
        if (property != null) {
            FeatureOfInterest createdProperty = updateInMemoryFoi(request, property);
            return new ExtraUseCaseResponse<Object>(request.id(), createdProperty);
        } else {
            String providerId = DtoMapperSimple.extractFirstIdSegment(request.id());

            List<SensorThingsUpdate> listDtoModels = dtosToCreateUpdate(request);
            ResourceSnapshot resource = getObservationForMemoryHistory(request.session(), providerId);

            // update/create provider
            try {
                dataUpdate.pushUpdate(listDtoModels).getValue();

            } catch (InvocationTargetException | InterruptedException e) {
                throw new InternalServerErrorException(e);
            }
            updateObservationMemoryHistory(obsCache, cacheFoi, request.mapper(), resource);

            ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), providerId, "datastream");
            if (serviceSnapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            ExpandedObservation obsUpdate = getExpandedObservationFromService(request, serviceSnapshot);
            String selfLink = DtoToModelMapper.getLink(request.uriInfo(), DtoMapperSimple.VERSION,
                    "/FeaturesOfInterest({})", (String) obsUpdate.featureOfInterest().id());
            String observationsLink = DtoToModelMapper.getLink(request.uriInfo(), selfLink, "/Observations");

            return new ExtraUseCaseResponse<Object>(request.id(), DtoMapperSimple.toFeatureOfInterest(obsUpdate,
                    (String) obsUpdate.featureOfInterest().id(), selfLink, observationsLink));

        }

    }

    public void removeInMemoryFeatureOfInterest(String id) {
        cacheFoi.removeDto(id);

    }

    public FeatureOfInterest getInMemoryFeatureOfInterest(String id) {
        return cacheFoi.getDto(id);
    }

    public void setObservationExtraUseCase(ObservationsExtraUseCase useCase) {
        observationExtraUseCase = useCase;
    }

}
