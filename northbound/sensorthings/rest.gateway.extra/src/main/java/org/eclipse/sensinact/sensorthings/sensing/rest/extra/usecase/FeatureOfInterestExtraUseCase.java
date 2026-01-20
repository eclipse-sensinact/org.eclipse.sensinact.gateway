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

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper.DtoToModelMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * FeatureOfInterest
 */
public class FeatureOfInterestExtraUseCase extends AbstractExtraUseCaseDto<FeatureOfInterest, Object> {

    private final IDtoMemoryCache<FeatureOfInterest> cacheFoi;

    @SuppressWarnings("unchecked")
    public FeatureOfInterestExtraUseCase(Providers providers) {
        super(providers);
        cacheFoi = resolve(providers, IDtoMemoryCache.class, FeatureOfInterest.class);

    }

    public ExtraUseCaseResponse<Object> create(ExtraUseCaseRequest<FeatureOfInterest> request) {
        FeatureOfInterest featureOfInterest = request.model();
        checkRequireField(request);
        String featureOfInterestId = request.id();
        FeatureOfInterest createFoi = new FeatureOfInterest(featureOfInterest.selfLink(), featureOfInterestId,
                featureOfInterest.name(), featureOfInterest.description(), featureOfInterest.encodingType(),
                featureOfInterest.feature(), null);
        cacheFoi.addDto(featureOfInterestId, createFoi);
        return new ExtraUseCaseResponse<Object>(featureOfInterestId, createFoi);

    }

    public ExtraUseCaseResponse<Object> delete(ExtraUseCaseRequest<FeatureOfInterest> request) {

        if (cacheFoi.getDto(request.id()) != null) {
            cacheFoi.removeDto(request.id());
            return new ExtraUseCaseResponse<Object>(true, "feature of interest deleted");

        } else {
            throw new WebApplicationException("FeatureOfInterest is mandatory for Observation",
                    Response.Status.CONFLICT);
        }

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
                receiveFoi.encodingType(), receiveFoi.feature(), null);
        GeoJsonObject observedArea = getObservedArea(request.session(), providerId);

        ExpandedObservation lastObservation = getExpandedObservationFromService(request,
                serviceUseCase.read(request.session(), providerId, DtoMapperSimple.SERVICE_DATASTREAM));
        return List.of(DtoToModelMapper.toDatastreamUpdate(request.mapper(), providerId, observedArea, null, null, null,
                null, null, lastObservation, foiToUpdate));

    }

    private FeatureOfInterest updateInMemoryFoi(ExtraUseCaseRequest<FeatureOfInterest> request, FeatureOfInterest foi) {
        FeatureOfInterest updateFoi = request.model();
        FeatureOfInterest createFoi = new FeatureOfInterest(null, request.id(),
                updateFoi.name() != null ? updateFoi.name() : foi.name(),
                updateFoi.description() != null ? updateFoi.description() : foi.description(),
                updateFoi.encodingType() != null ? updateFoi.encodingType() : foi.encodingType(),
                updateFoi.feature() != null ? updateFoi.feature() : foi.feature(), null);
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

            // update/create provider
            try {
                dataUpdate.pushUpdate(listDtoModels).getValue();

            } catch (InvocationTargetException | InterruptedException e) {
                throw new InternalServerErrorException(e);
            }
            ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), providerId, "datastream");
            if (serviceSnapshot == null) {
                return new ExtraUseCaseResponse<Object>(false, "can't find sensor");
            }
            return new ExtraUseCaseResponse<Object>(request.id(), serviceSnapshot);

        }

    }

    public void removeInMemoryFeatureOfInterest(String id) {
        cacheFoi.removeDto(id);

    }

    public FeatureOfInterest getInMemoryFeatureOfInterest(String id) {
        return cacheFoi.getDto(id);
    }

}
