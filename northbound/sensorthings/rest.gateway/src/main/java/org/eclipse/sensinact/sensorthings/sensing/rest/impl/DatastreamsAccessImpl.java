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
import org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.DatastreamsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.DatastreamsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.DatastreamsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.DatastreamsUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

public class DatastreamsAccessImpl extends AbstractAccess
        implements DatastreamsDelete, DatastreamsAccess, DatastreamsCreate, DatastreamsUpdate {

    @Override
    public Datastream getDatastream(String id) {
        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(id));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getDatastreamObservations(String id) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot provider = validateAndGetProvider(id);
        ResultList<Observation> observationList = RootResourceAccessImpl.getObservationList(getSession(), application,
                getMapper(), uriInfo, requestContext,
                provider.getResource(UtilDto.SERVICE_DATASTREAM, "lastObservation"), filter);
        return observationList;
    }

    @Override
    public Observation getDatastreamObservation(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id);
        String providerObs = UtilDto.extractFirstIdSegment(id2);
        if (!provider.equals(providerObs)) {
            throw new NotFoundException();
        }
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);
        ServiceSnapshot service = UtilDto.getDatastreamService(providerSnapshot);

        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, service.getResource("lastObservation"));

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
        String provider = UtilDto.extractFirstIdSegment(id);
        String providerFoi = UtilDto.extractFirstIdSegment(id2);
        if (!providerFoi.equals(provider)) {
            throw new NotFoundException();
        }
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), validateAndGetProvider(provider));
    }

    @Override
    public ObservedProperty getDatastreamObservedProperty(String id) {

        String provider = UtilDto.extractFirstIdSegment(id);
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(provider));

        return o;
    }

    @Override
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getDatastream(id)));
    }

    @Override
    public Sensor getDatastreamSensor(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(provider));

        return s;
    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    @Override
    public Thing getDatastreamThing(String id) {
        String thingId = getThingIdFromDatastream(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                validateAndGetProvider(thingId));
    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {

        return getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), id);
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        try {
            String thingId = getThingIdFromDatastream(id);
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);

            ProviderSnapshot providerThing = validateAndGetProvider(thingId);

            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerThing, 0);
            if (list.value().isEmpty())
                list = new ResultList<>(null, null, List.of());
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(String id) {

        List<Location> listLocation = List.of();
        String thingId = getThingIdFromDatastream(id);
        List<ProviderSnapshot> locationProviders = getLocationProvidersFromThing(thingId);
        listLocation = locationProviders.stream().map(p -> DtoMapper.toLocation(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(LOCATIONS), p)).toList();

        return new ResultList<>(null, null, listLocation);
    }

    static ResultList<Datastream> getDataStreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, String thingId) {
        List<ProviderSnapshot> datastreamProviders = getDatastreamProvidersFromThing(userSession, thingId);
        return new ResultList<>(null, null, datastreamProviders.stream().map(provider -> DtoMapper
                .toDatastream(userSession, application, mapper, uriInfo, expansions, filter, provider)).toList());
    }

    @Override
    public Response createDatastreamsObservation(String id, ExpandedObservation observation) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observation, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        Optional<Observation> createDto = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, snapshot.getResource("lastObservation"));
        if (createDto.isEmpty()) {
            throw new BadRequestException("fail to create observation");
        }
        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.get().id())).build();

        return Response.created(createdUri).entity(createDto).build();
    }

    @Override
    public Response createObservationRef(String id, RefId observation) {

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), observation, id,
                ExpandedDataStream.class, ExpandedObservation.class);

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

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), thing, id,
                ExpandedDataStream.class, ExpandedThing.class);

        return Response.noContent().build();
    }

    @Override
    public Response updateDatastreamSensorRef(String id, RefId sensor) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), sensor, id,
                ExpandedDataStream.class, ExpandedSensor.class);

        return Response.noContent().build();
    }

    @Override
    public Response updateDatastreamObservedPropertyRef(String id, RefId observedProperty) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), observedProperty,
                id, ExpandedDataStream.class, ExpandedObservedProperty.class);

        return Response.noContent().build();
    }

    @Override
    public Response patchDatastreams(String id, ExpandedDataStream dataStream) {
        return updateDatastreams(id, dataStream);
    }

    @Override
    public Response patchDatastreamsObservation(String id, String id2, Observation observation) {
        return updateDatastreamsObservation(id, id2, observation);
    }

    @Override
    public Response deleteDatastream(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedDataStream.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteDatastreamSensorRef(String id) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedDataStream.class,
                ExpandedSensor.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteDatastreamObservedPropertyRef(String id) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedDataStream.class,
                ExpandedObservedProperty.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteDatastreamObservationsRef(String id) {
        return Response.status(409).build();
    }
}
