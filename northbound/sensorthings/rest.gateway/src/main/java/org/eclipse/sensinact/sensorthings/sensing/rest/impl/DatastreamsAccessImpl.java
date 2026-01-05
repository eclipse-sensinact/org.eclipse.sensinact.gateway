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
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.net.URI;
import java.util.List;
import java.util.Objects;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
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
        ProviderSnapshot provider = validateAndGetProvider(extractFirstIdSegment(id));

        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), UtilIds.getDatastreamService(provider));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getDatastreamObservations(String id) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot provider = validateAndGetProvider(id);
        ServiceSnapshot service = UtilIds.getDatastreamService(provider);
        if (service == null) {
            throw new NotFoundException();
        }
        ResultList<Observation> observationList = RootResourceAccessImpl.getObservationList(getSession(), application,
                getMapper(), uriInfo, requestContext, service, filter);
        return observationList;
    }

    @Override
    public Observation getDatastreamObservation(String id, String id2) {
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot provider = validateAndGetProvider(id);
        ServiceSnapshot service = UtilIds.getDatastreamService(provider);
        Observation o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                filter, service);

        if (!id2.equals(o.id())) {
            throw new NotFoundException();
        }
        return o;
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
        ProviderSnapshot provider = validateAndGetProvider(extractFirstIdSegment(id));

        return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(OBSERVED_PROPERTIES), UtilIds.getDatastreamService(provider));

    }

    @Override
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        return new ResultList<>(null, null, List.of(getDatastream(id)));
    }

    @Override
    public Sensor getDatastreamSensor(String id) {
        ProviderSnapshot provider = validateAndGetProvider(extractFirstIdSegment(id));

        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), UtilIds.getDatastreamService(provider));

    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    @Override
    public Thing getDatastreamThing(String id) {
        String providerId = extractFirstIdSegment(id);
        ProviderSnapshot datastreamProvider = validateAndGetProvider(providerId);
        ServiceSnapshot serviceDatastream = UtilIds.getDatastreamService(datastreamProvider);
        String thingId = UtilIds.getResourceField(serviceDatastream, "thingId", String.class);
        ProviderSnapshot provider = validateAndGetProvider(thingId);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                UtilIds.getThingService(provider));
    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot datastreamProvider = validateAndGetProvider(provider);
        ServiceSnapshot serviceDatastream = UtilIds.getDatastreamService(datastreamProvider);
        String thingId = UtilIds.getResourceField(serviceDatastream, "thingId", String.class);
        List<ServiceSnapshot> listServiceSnapshot = getListDatastreamServices(getSession(), thingId);
        return getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), listServiceSnapshot);
    }

    public static List<ServiceSnapshot> getListDatastreamServices(SensiNactSession session, String thingId) {

        ProviderSnapshot providerThing = DtoMapper.validateAndGetProvider(session, thingId);
        ServiceSnapshot serviceThing = providerThing.getService("thing");

        @SuppressWarnings("unchecked")
        List<String> listIdDatastream = UtilIds.getResourceField(serviceThing, "datastreamIds", List.class);
        List<ServiceSnapshot> listServiceSnapshot = listIdDatastream.stream()
                .map(idDatastream -> DtoMapper.validateAndGetProvider(session, idDatastream))
                .map(providerDatastream -> UtilIds.getDatastreamService(providerDatastream)).toList();
        return listServiceSnapshot;
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(id);
            ServiceSnapshot serviceDatastream = UtilIds.getDatastreamService(providerSnapshot);
            String thingId = UtilIds.getResourceField(serviceDatastream, "thingId", String.class);
            ProviderSnapshot providerSnapshotThing = validateAndGetProvider(thingId);
            ServiceSnapshot serviceThing = UtilIds.getThingService(providerSnapshotThing);
            @SuppressWarnings("unchecked")
            List<String> locationIds = (List<String>) UtilIds.getResourceField(serviceThing, "locationIds",
                    Object.class);
            String locationId = locationIds.size() > 0 ? locationIds.get(0) : null;
            // TODO manage multi location provider for pagination. today only first
            // historical is return
            Optional<ResultList<HistoricalLocation>> list = locationIds.stream().map(this::validateAndGetProvider)
                    .map(s -> HistoryResourceHelper.loadHistoricalLocations(getSession(), application, getMapper(),
                            uriInfo, getExpansions(), filter, s, 0))
                    .findFirst();

            if (list.isEmpty() || list.get().value().isEmpty()) {
                List<HistoricalLocation> locs = List.of(
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, UtilIds.getLocationService(validateAndGetProvider(locationId))).get());
                return new ResultList<HistoricalLocation>(null, null, locs);
            }
            return list.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(String id) {
        ProviderSnapshot datastream = validateAndGetProvider(id);
        ServiceSnapshot service = UtilIds.getDatastreamService(datastream);
        if (service == null) {
            throw new NotFoundException();
        }
        String thingId = UtilIds.getResourceField(service, "thingId", String.class);
        if (thingId == null) {
            throw new NotFoundException();
        }
        ProviderSnapshot thingProvider = validateAndGetProvider(thingId);
        ServiceSnapshot serviceThing = UtilIds.getThingService(thingProvider);
        @SuppressWarnings("unchecked")
        List<String> locationIds = (List<String>) UtilIds.getResourceField(serviceThing, "locationIds", Object.class);
        ICriterion criterion = parseFilter(EFilterContext.LOCATIONS);

        return new ResultList<>(null, null, locationIds.stream().map(idLoc -> validateAndGetProvider(idLoc))
                .map(p -> UtilIds.getLocationService(p)).filter(Objects::nonNull).map(s -> DtoMapper
                        .toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, s))
                .toList());
    }

    static ResultList<Datastream> getDataStreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            List<ServiceSnapshot> listServiceDatastreams) {
        return new ResultList<Datastream>(null, null,
                listServiceDatastreams.stream().map(
                        s -> DtoMapper.toDatastream(userSession, application, mapper, uriInfo, expansions, filter, s))
                        .collect(toList()));
    }

    @Override
    public Response createDatastreamsObservation(String id, ExpandedObservation observation) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observation, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);
        ExpandedObservation lastObservation = (ExpandedObservation) UtilIds.getResourceField(snapshot,
                "lastObservation", Object.class);
        Observation createDto = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, snapshot, lastObservation);
        if (createDto == null) {
            throw new BadRequestException("fail to create observation");
        }
        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

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
}
