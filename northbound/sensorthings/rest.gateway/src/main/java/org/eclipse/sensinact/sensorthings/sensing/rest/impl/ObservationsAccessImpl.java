/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ObservationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ObservationsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class ObservationsAccessImpl extends AbstractAccess implements ObservationsAccess, ObservationsUpdate {

    @Override
    public Observation getObservation(String id) {
        ServiceSnapshot serviceSnapshot = validateAndGeService(id);

        ICriterion criterion = parseFilter(OBSERVATIONS);

        ExpandedObservation observation = (ExpandedObservation) UtilIds.getResourceField(serviceSnapshot,
                "lastObservation", Object.class);
        return DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion,
                serviceSnapshot, observation);

    }

    @Override
    public Datastream getObservationDatastream(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);

        Datastream d = DtoMapperGet.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                resourceSnapshot, parseFilter(DATASTREAMS));

        if (!id.startsWith(String.valueOf(d.id()))) {
            throw new NotFoundException();
        }

        return d;
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        ProviderSnapshot provider = validateAndGetProvider(id);
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), UtilIds.getDatastreamService(provider), parseFilter(OBSERVATIONS), 0);
    }

    @Override
    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);
        return DtoMapperGet.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(OBSERVED_PROPERTIES), resourceSnapshot);
    }

    @Override
    public Sensor getObservationDatastreamSensor(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);

        Sensor s = DtoMapperGet.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), resourceSnapshot);
        if (!id.startsWith(String.valueOf(s.id()))) {
            throw new NotFoundException();
        }
        return s;
    }

    @Override
    public Thing getObservationDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t = DtoMapperGet.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), providerSnapshot);
        if (!provider.equals(t.id())) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapperGet.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), providerSnapshot);
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ICriterion criterion = parseFilter(OBSERVATIONS);
        return new ResultList<>(null, null, providerSnapshot.getServices().stream()
                .flatMap(s -> s.getResources().stream()).filter(ResourceSnapshot::isSet).map(r -> DtoMapperGet
                        .toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, r))
                .filter(Optional::isPresent).map(Optional::get).toList());
    }

    @Override
    public Response updateObservationDatastreamRef(String id, RefId datastream) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), datastream, id,
                ExpandedObservation.class, ExpandedDataStream.class);

        return Response.noContent().build();
    }

    @Override
    public Response updateObservationFeatureOfInterestRef(String id, RefId foi) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), foi, id,
                ExpandedObservation.class, FeatureOfInterest.class);

        return Response.noContent().build();
    }

}
