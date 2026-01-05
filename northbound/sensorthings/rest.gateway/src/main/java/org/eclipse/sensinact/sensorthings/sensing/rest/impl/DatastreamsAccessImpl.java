/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.DatastreamsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.DatastreamsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.DatastreamsUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

public class DatastreamsAccessImpl extends AbstractAccess
        implements DatastreamsAccess, DatastreamsCreate, DatastreamsUpdate {

    @Override
    public Datastream getDatastream(String id) {
        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGeService(id));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getDatastreamObservations(String id) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResultList<Observation> observationList = RootResourceAccessImpl.getObservationList(getSession(), application,
                getMapper(), uriInfo, requestContext, validateAndGetResourceSnapshot(id), filter);
        return observationList;
    }

    @Override
    public Observation getDatastreamObservation(String id, String id2) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        Optional<Observation> o = DtoMapperGet.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, validateAndGetResourceSnapshot(id));

        if (o.isEmpty() || !id2.equals(o.get().id())) {
            throw new NotFoundException();
        }
        return o.get();
    }

    @Override
    public Datastream getDatastreamObservationDatastream(String id, String id2) {
        return getDatastream(id);
    }

    @Override
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        return DtoMapperGet.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), validateAndGetProvider(provider));
    }

    @Override
    public ObservedProperty getDatastreamObservedProperty(String id) {
        ObservedProperty o = DtoMapperGet.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }
        return o;
    }

    @Override
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getDatastream(id)));
    }

    @Override
    public Sensor getDatastreamSensor(String id) {
        Sensor s = DtoMapperGet.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id));

        if (!id.equals(s.id())) {
            throw new NotFoundException();
        }
        return s;
    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    @Override
    public Thing getDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapperGet.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), validateAndGetProvider(provider));
    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);

        return getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(provider));
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty())
                list = new ResultList<>(null, null,
                        DtoMapperGet.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), filter, providerSnapshot).map(List::of).orElse(List.of()));
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        Location hl;
        try {
            hl = DtoMapperGet.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(LOCATIONS), validateAndGetProvider(provider));
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        return new ResultList<>(null, null, List.of(hl));
    }

    static ResultList<Datastream> getDataStreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot providerSnapshot) {
        return new ResultList<Datastream>(null, null,
                providerSnapshot.getServices().stream().map(
                        s -> DtoMapper.toDatastream(userSession, application, mapper, uriInfo, expansions, filter, s))
                        .collect(toList()));
    }

    @Override
    public Response createDatastreamsObservation(String id, ExpandedObservation observation) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observation, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);
        String datastreamLink = DtoMapper.getLink(uriInfo, DtoMapper.VERSION, "/Datastreams({id})", id);

        Observation createDto = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, snapshot, datastreamLink);
        if (createDto == null) {
            throw new BadRequestException("fail to create observation");
        }
        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();
    }

    @Override
    public Response createObservationRef(String id, RefId observation) {
        ExpandedObservation expandedObservation = new ExpandedObservation(null, observation.id(), null, null, null,
                null, null, null, null, null, null, null, null);
        getExtraDelegate().create(getSession(), getMapper(), uriInfo, requestContext.getMethod(), expandedObservation,
                id);

        return Response.noContent().build();
    }

    @Override
    public Response updateDatastreams(String id, ExpandedDataStream dataStream) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, dataStream);

        return Response.noContent().build();
    }

    @Override
    public Response updateDatastreamsObservation(String id, String id2, Observation observation) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id2, observation, id);

        return Response.noContent().build();
    }

    @Override
    public Response updateDatastreamThingRef(String id, RefId thing) {
        // TODO
        return null;
    }

    @Override
    public Response updateDatastreamSensorRef(String id, RefId sensor) {
        // TODO
        return null;
    }

    @Override
    public Response updateDatastreamObservedPropertyRef(String id, RefId observedProperty) {
        // TODO
        return null;
    }

    @Override
    public Response patchDatastreams(String id, ExpandedDataStream dataStream) {
        return updateDatastreams(id, dataStream);
    }

    @Override
    public Response patchDatastreamsObservation(String id, String id2, Observation observation) {
        return updateDatastreamsObservation(id, id2, observation);
    }
}
