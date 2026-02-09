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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class DatastreamsDelegateSensorthings extends AbstractDelegate {

    public DatastreamsDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public Datastream getDatastream(String id) {

        Optional<Datastream> ds = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), validateAndGetProvider(id));
        if (ds.isEmpty())
            throw new NotFoundException();
        return ds.get();
    }

    public ResultList<Observation> getDatastreamObservations(String id) {

        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot provider = validateAndGetProvider(id);
        ResultList<Observation> observationList = RootResourceDelegateSensorthings.getObservationList(getSession(),
                application, getMapper(), uriInfo, requestContext,
                provider.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter);
        return observationList;
    }

    public Observation getDatastreamObservation(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String providerObs = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(providerObs)) {
            throw new NotFoundException();
        }
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);

        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, service.getResource("lastObservation"));

        if (o.isEmpty() || !id2.equals(o.get().id())) {
            throw new NotFoundException();
        }
        return o.get();
    }

    public Datastream getDatastreamObservationDatastream(String id, String id2) {

        return getDatastream(id);
    }

    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String providerFoi = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!providerFoi.equals(provider)) {
            throw new NotFoundException();
        }
        ResourceSnapshot resource = getObservationResourceSnapshot(id2);
        Instant stamp = resource.getValue().getTimestamp();

        ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(getMapper(), resource.getValue().getValue());
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), stamp, obs);
    }

    public ObservedProperty getDatastreamObservedProperty(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(provider)).get();

        return o;
    }

    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {

        return new ResultList<>(null, null, List.of(getDatastream(id)));
    }

    public Sensor getDatastreamSensor(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(provider)).get();

        return s;
    }

    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {

        return getDatastreamObservedPropertyDatastreams(id);
    }

    public Thing getDatastreamThing(String id) {

        String thingId = getThingIdFromDatastream(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                validateAndGetProvider(thingId));
    }

    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {

        String thingId = getThingIdFromDatastream(id);

        return getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), thingId);
    }

    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {

        try {
            String thingId = getThingIdFromDatastream(id);
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);

            ProviderSnapshot providerThing = validateAndGetProvider(thingId);

            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, providerThing, 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThing);
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

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
        return new ResultList<>(null, null,
                datastreamProviders
                        .stream().map(provider -> DtoMapper.toDatastream(userSession, application, mapper, uriInfo,
                                expansions, filter, provider))
                        .filter(ds -> ds.isPresent()).map(ds -> ds.get()).toList());
    }

    public Response createDatastreamsObservation(String id, ExpandedObservation observation) {

        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observation, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        Optional<Observation> createDto = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, snapshot.getResource("lastObservation"));
        if (createDto.isEmpty()) {
            throw new BadRequestException("fail to create observation");
        }
        URI createdUri = getCreatedUri(createDto.get());
        return Response.created(createdUri).entity(createDto).build();
    }

    public Response createObservationRef(String id, RefId observation) {

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), observation, id,
                ExpandedDataStream.class, ExpandedObservation.class);

        return Response.ok().build();
    }

    public Response updateDatastreams(String id, ExpandedDataStream dataStream) {

        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, dataStream);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        Datastream createDto = DtoMapper
                .toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, snapshot)
                .get();

        return Response.ok().entity(createDto).build();
    }

    public Response updateDatastreamsObservation(String id, String id2, Observation observation) {

        ServiceSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id2, observation, id);
        ICriterion criterion = parseFilter(EFilterContext.OBSERVATIONS);
        Optional<Observation> createDto = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, snapshot.getResource("lastObservation"));
        if (createDto.isEmpty()) {
            throw new BadRequestException("fail to create observation");
        }
        return Response.ok().entity(createDto).build();
    }

    public Response updateDatastreamThingRef(String id, RefId thing) {

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), thing, id,
                ExpandedDataStream.class, ExpandedThing.class);

        return Response.ok().build();
    }

    public Response updateDatastreamSensorRef(String id, RefId sensor) {

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), sensor, id,
                ExpandedDataStream.class, Sensor.class);

        return Response.ok().build();
    }

    public Response updateDatastreamObservedPropertyRef(String id, RefId observedProperty) {

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), observedProperty,
                id, ExpandedDataStream.class, ObservedProperty.class);

        return Response.ok().build();
    }

    public Response patchDatastreams(String id, ExpandedDataStream dataStream) {

        return updateDatastreams(id, dataStream);
    }

    public Response patchDatastreamsObservation(String id, String id2, Observation observation) {

        return updateDatastreamsObservation(id, id2, observation);
    }

    public Response deleteDatastream(String id) {

        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedDataStream.class);

        return Response.ok().build();
    }

    public Response deleteDatastreamSensorRef(String id) {

        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedDataStream.class, Sensor.class);

        return Response.ok().build();
    }

    public Response deleteDatastreamObservedPropertyRef(String id) {

        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedDataStream.class,
                ObservedProperty.class);

        return Response.ok().build();
    }

    public Response deleteDatastreamObservationsRef(String id) {

        return Response.status(409).build();
    }

    public ResultList<Observation> getDatastreamThingDatastreamObserations(String id, String id2) {
        String thingId = getDatastreamThingId(id);
        ProviderSnapshot providerThing = validateAndGetProvider(thingId);
        ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(providerThing);
        List<?> datastreamIds = DtoMapperSimple.getResourceField(serviceThing, "datastreamIds", List.class);
        if (!datastreamIds.contains(id2)) {
            throw new NotFoundException();
        }
        return getDatastreamObservations(id2);
    }

    private String getDatastreamThingId(String id) {
        ProviderSnapshot providerDatastream = validateAndGetProvider(id);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerDatastream);
        String thingId = DtoMapperSimple.getResourceField(service, "thingId", String.class);
        return thingId;
    }

    public ResultList<Thing> getDatastreamThingLocationThings(String id, String id2) {
        String thingId = getDatastreamThingId(id);
        ProviderSnapshot providerThing = validateAndGetProvider(thingId);
        ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(providerThing);
        List<?> locationIds = DtoMapperSimple.getResourceField(serviceThing, "locationIds", List.class);
        if (!locationIds.contains(id2)) {
            throw new NotFoundException();
        }

        return new ResultList<>(null, null,
                getLocationThingsProvider(id2).stream().map(p -> DtoMapper.toThing(getSession(), application,
                        getMapper(), uriInfo, getExpansions(), parseFilter(THINGS), p)).toList());

    }
}
