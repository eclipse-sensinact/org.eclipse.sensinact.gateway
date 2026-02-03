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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class ThingsDelegateSensorthings extends AbstractDelegate {

    public ThingsDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public Thing getThing(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    public ResultList<Datastream> getThingDatastreams(String id) {

        return DatastreamsDelegateSensorthings.getDataStreams(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), id);
    }

    public Datastream getThingDatastream(String id, String id2) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(providerDatastreamId));

        if (!id2.equals(d.id())) {
            throw new NotFoundException();
        }
        return d;
    }

    @PaginationLimit(500)

    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        return RootResourceDelegateSensorthings.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, getObservationResourceSnapshot(id2), parseFilter(OBSERVATIONS));
    }

    public ResultList<Thing> getThingHistoricalLocationLocationThings(ODataId id, ODataId id2, ODataId id3) {

        List<ProviderSnapshot> providerLocations = AbstractDelegate.getLocationThingsProvider(getSession(),
                id3.value());
        return new ResultList<Thing>(null, null, providerLocations.stream()
                .map(p -> DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), null, p))
                .toList());
    }

    public Observation getThingDatastreamObservation(String id, String id2, String id3) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        Optional<Observation> obs = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), null, getObservationResourceSnapshot(id3));
        if (obs.isEmpty()) {
            throw new NotFoundException();
        }
        return obs.get();

    }

    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(providerDatastreamId));

        return o;
    }

    public Sensor getThingDatastreamSensor(String id, String id2) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(providerDatastreamId));

        return s;
    }

    public Thing getThingDatastreamThing(String id, String id2) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastreamId = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        return getThing(id);
    }

    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {

        String providerThingId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot providerThing = validateAndGetProvider(providerThingId);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, providerThing, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThing);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerThing = validateAndGetProvider(provider);
        DtoMapper.getTimestampFromId(id2);

        try {
            Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                    uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerThing);
            if (hl.isEmpty()) {
                throw new NotFoundException();
            }
            return hl.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public Thing getThingHistoricalLocationsThing(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id2);
        // check if the location exists
        validateAndGetProvider(provider);
        return getThing(id);
    }

    public ResultList<Location> getThingHistoricalLocationLocations(String id, String id2) {

        DtoMapper.getTimestampFromId(id2);
        ResultList<Location> list = new ResultList<>(
                null, null, getLocationIdsFromThing(getSession(), id).stream()
                        .map(idLoc -> validateAndGetProvider(idLoc)).map(p -> DtoMapper.toLocation(getSession(),
                                application, getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), p))
                        .toList());

        return list;
    }

    public ResultList<Location> getThingLocations(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        List<ProviderSnapshot> providersLocation = getLocationProvidersFromThing(provider);
        ResultList<Location> list = new ResultList<>(null, null,
                providersLocation.stream().map(p -> DtoMapper.toLocation(getSession(), application, getMapper(),
                        uriInfo, getExpansions(), parseFilter(LOCATIONS), p)).toList());

        return list;
    }

    public Location getThingLocation(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!isLocationInThing(id, id2)) {
            throw new NotFoundException();
        }

        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), validateAndGetProvider(provider));

        if (!id2.equals(l.id())) {
            throw new NotFoundException();
        }
        return l;
    }

    public ResultList<Thing> getThingLocationThings(String id, String id2) {

        String providerThindId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerLocationId = DtoMapperSimple.extractFirstIdSegment(id2);

        if (!isLocationInThing(providerThindId, providerLocationId)) {
            throw new NotFoundException();
        }
        return new ResultList<Thing>(null, null,
                getLocationThingsProvider(id2).stream().map(p -> DtoMapper.toThing(getSession(), application,
                        getMapper(), uriInfo, getExpansions(), parseFilter(THINGS), p)).toList());
    }

    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        if (!getLocationIdsFromThing(getSession(), id).contains(id2)) {
            throw new NotFoundException();
        }
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerSnapshot);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public Response createDatastream(String id, ExpandedDataStream datastream) {

        ProviderSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), datastream, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);
        Datastream createDto = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();

    }

    public Response createLocation(String id, ExpandedLocation location) {

        ProviderSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), location, id);
        ICriterion criterion = parseFilter(EFilterContext.FEATURES_OF_INTEREST);

        Location createDto = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();

    }

    public Response updateDatastream(String id, String id2, ExpandedDataStream datastream) {

        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id2, datastream, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        Datastream createDto = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

        return Response.ok().entity(createDto).build();
    }

    public Response updateLocation(String id, String id2, ExpandedLocation location) {

        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id2, location, id);

        return Response.ok().build();
    }

    public Response updateThing(String id, ExpandedThing thing) {

        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, thing);
        ICriterion criterion = parseFilter(EFilterContext.THINGS);

        Thing createDto = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion,
                snapshot);

        return Response.ok().entity(createDto).build();
    }

    public Response updateLocationRef(String id, RefId location) {

        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), location, id,
                ExpandedThing.class, ExpandedLocation.class);

        return Response.ok().build();
    }

    public Response updateDatastreamRef(String id, RefId datastream) {

        RefId thingId = new RefId(id);
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), thingId,
                (String) datastream.id(), ExpandedDataStream.class, ExpandedThing.class);

        return Response.ok().build();
    }

    public Response patchDatastream(String id, String id2, ExpandedDataStream datastream) {

        return updateDatastream(id, id2, datastream);
    }

    public Response patchLocation(String id, String id2, ExpandedLocation location) {

        return updateLocation(id, id2, location);
    }

    public Response patchThing(String id, ExpandedThing thing) {

        return updateThing(id, thing);
    }

    public Response deleteThing(String id) {

        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedThing.class);

        return Response.ok().build();
    }

    public Response deleteDatastreamRef(String id, String id2) {

        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, id2, ExpandedThing.class,
                ExpandedDataStream.class);

        return Response.ok().build();
    }

    public Response deleteLocationRef(String id, String id2) {

        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, id2, ExpandedThing.class,
                ExpandedLocation.class);

        return Response.ok().build();
    }

    public Response deleteLocationsRef(String id) {

        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedThing.class,
                ExpandedLocation.class);

        return Response.ok().build();
    }

}
