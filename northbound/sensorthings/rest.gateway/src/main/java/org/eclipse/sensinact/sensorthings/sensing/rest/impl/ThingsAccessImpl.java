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
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ThingsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.ThingsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.ThingsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ThingsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class ThingsAccessImpl extends AbstractAccess implements ThingsDelete, ThingsAccess, ThingsCreate, ThingsUpdate {

    @Override
    public Thing getThing(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    @Override
    public ResultList<Datastream> getThingDatastreams(String id) {

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), id);
    }

    @Override
    public Datastream getThingDatastream(String id, String id2) {
        String providerThingId = UtilDto.extractFirstIdSegment(id);
        String providerDatastreamId = UtilDto.extractFirstIdSegment(id2);
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
    @Override
    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {
        String providerThingId = UtilDto.extractFirstIdSegment(id);
        String providerDatastreamId = UtilDto.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, getObservationResourceSnapshot(id2), parseFilter(OBSERVATIONS));
    }

    @Override
    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {
        String providerThingId = UtilDto.extractFirstIdSegment(id);
        String providerDatastreamId = UtilDto.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(providerDatastreamId));

        return o;
    }

    @Override
    public Sensor getThingDatastreamSensor(String id, String id2) {
        String providerThingId = UtilDto.extractFirstIdSegment(id);
        String providerDatastreamId = UtilDto.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(providerDatastreamId));

        return s;
    }

    @Override
    public Thing getThingDatastreamThing(String id, String id2) {
        String providerThingId = UtilDto.extractFirstIdSegment(id);
        String providerDatastreamId = UtilDto.extractFirstIdSegment(id2);
        if (!isDatastreamInThing(providerThingId, providerDatastreamId)) {
            throw new NotFoundException();
        }

        return getThing(id);
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {
        String providerThingId = UtilDto.extractFirstIdSegment(id);
        List<ProviderSnapshot> locationProviders = getLocationProvidersFromThing(providerThingId);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, locationProviders, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, locationProviders);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id2);
        ProviderSnapshot providerLocation = validateAndGetProvider(provider);
        ServiceSnapshot serviceLocation = UtilDto.getLocationService(providerLocation);
        DtoMapper.getTimestampFromId(id2);

        try {
            Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                    uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), serviceLocation);
            if (hl.isEmpty()) {
                throw new NotFoundException();
            }
            return hl.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public Thing getThingHistoricalLocationsThing(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id2);
        return getThing(id);
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationLocations(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id2);

        DtoMapper.getTimestampFromId(id2);

        ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(getSession(), application,
                getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), validateAndGetProvider(provider))));

        return list;
    }

    @Override
    public ResultList<Location> getThingLocations(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        List<ProviderSnapshot> providersLocation = getLocationProvidersFromThing(provider);
        ResultList<Location> list = new ResultList<>(null, null,
                providersLocation.stream().map(p -> DtoMapper.toLocation(getSession(), application, getMapper(),
                        uriInfo, getExpansions(), parseFilter(LOCATIONS), p)).toList());

        return list;
    }

    @Override
    public Location getThingLocation(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id2);
        if (!isLocationInThing(id, id2)) {
            throw new NotFoundException();
        }

        DtoMapper.getTimestampFromId(id2);

        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), validateAndGetProvider(provider));

        if (!id2.equals(l.id())) {
            throw new NotFoundException();
        }
        return l;
    }

    @Override
    public ResultList<Thing> getThingLocationThings(String id, String id2) {
        String providerThindId = UtilDto.extractFirstIdSegment(id);
        String providerLocationId = UtilDto.extractFirstIdSegment(id2);

        if (!isLocationInThing(providerThindId, providerLocationId)) {
            throw new NotFoundException();
        }
        // TODO get all thing link to location
        return new ResultList<>(null, null, List.of(getThing(id)));
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id2);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, List.of(providerSnapshot), 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, List.of(providerSnapshot));
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public Response createDatastream(String id, ExpandedDataStream datastream) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), datastream, id);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);
        Datastream createDto = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();

    }

    @Override
    public Response createLocation(String id, ExpandedLocation location) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), location, id);
        ICriterion criterion = parseFilter(EFilterContext.FEATURES_OF_INTEREST);

        Location createDto = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();

    }

    @Override
    public Response updateDatastream(String id, String id2, ExpandedDataStream datastream) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id2, datastream, id);

        return Response.noContent().build();
    }

    @Override
    public Response updateLocation(String id, String id2, ExpandedLocation location) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id2, location, id);

        return Response.noContent().build();
    }

    @Override
    public Response updateThing(String id, ExpandedThing thing) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, thing);

        return Response.noContent().build();
    }

    @Override
    public Response updateLocationRef(String id, RefId location) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), location, id,
                ExpandedThing.class, ExpandedLocation.class);

        return Response.noContent().build();
    }

    @Override
    public Response updateDatastreamRef(String id, RefId datastream) {
        RefId thingId = new RefId(id);
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), thingId,
                (String) datastream.id(), ExpandedDataStream.class, ExpandedThing.class);

        return Response.noContent().build();
    }

    @Override
    public Response patchDatastream(String id, String id2, ExpandedDataStream datastream) {
        return updateDatastream(id, id2, datastream);
    }

    @Override
    public Response patchLocation(String id, String id2, ExpandedLocation location) {
        return updateLocation(id, id2, location);
    }

    @Override
    public Response patchThing(String id, ExpandedThing thing) {
        return updateThing(id, thing);
    }

    @Override
    public Response deleteThing(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedThing.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteDatastreamRef(String id, String id2) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, id2, ExpandedThing.class,
                ExpandedDataStream.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteLocationRef(String id, String id2) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, id2, ExpandedThing.class,
                ExpandedLocation.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteLocationsRef(String id) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedThing.class,
                ExpandedLocation.class);

        return Response.noContent().build();
    }

}
