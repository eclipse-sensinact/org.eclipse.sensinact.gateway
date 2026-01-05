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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.getTimestampFromId;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ThingsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.ThingsCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.ThingsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class ThingsAccessImpl extends AbstractAccess implements ThingsAccess, ThingsCreate, ThingsUpdate {

    @Override
    public Thing getThing(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    @Override
    public ResultList<Datastream> getThingDatastreams(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), providerSnapshot);
    }

    @Override
    public Datastream getThingDatastream(String id, String id2) {

        ProviderSnapshot providerThing = validateAndGetProvider(id);
        ServiceSnapshot serviceThing = providerThing.getService("thing");
        @SuppressWarnings("unchecked")
        List<String> listDatastream = UtilIds.getResourceField(serviceThing, "datastreamIds", List.class);
        if (listDatastream == null || !listDatastream.contains(id2)) {
            throw new NotFoundException(String.format("datastream %s not exists in %s", id2, id));
        }
        ProviderSnapshot providerDatastream = validateAndGetProvider(id2);

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), providerDatastream.getService("datastream"));

        if (!id2.equals(d.id())) {
            throw new NotFoundException();
        }
        return d;
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, validateAndGetResourceSnapshot(id2), parseFilter(OBSERVATIONS));
    }

    @Override
    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapperGet.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetResourceSnapshot(id2));

        if (!id2.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public Sensor getThingDatastreamSensor(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        Sensor s = DtoMapperGet.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetResourceSnapshot(id2));

        if (!id2.equals(s.id())) {
            throw new NotFoundException();
        }

        return s;
    }

    @Override
    public Thing getThingDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return getThing(id);
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = new ResultList<>(null, null,
                        DtoMapperGet.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), filter, providerSnapshot).map(List::of).orElse(List.of()));
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        try {
            Optional<HistoricalLocation> hl = DtoMapperGet.toHistoricalLocation(getSession(), application, getMapper(),
                    uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), validateAndGetProvider(provider));
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
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        return getThing(id);
    }

    @Override
    public ResultList<Location> getThingHistoricalLocationLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        ResultList<Location> list = new ResultList<>(null, null,
                List.of(DtoMapperGet.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        parseFilter(LOCATIONS), validateAndGetProvider(provider))));

        return list;
    }

    @Override
    public ResultList<Location> getThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        ResultList<Location> list = new ResultList<>(null, null,
                List.of(DtoMapperGet.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        parseFilter(LOCATIONS), validateAndGetProvider(provider))));

        return list;
    }

    @Override
    public Location getThingLocation(String id, String id2) {
        String providerThing = id;
        String providerLocation = id2;

        // check if thing exists
        if (validateAndGetProvider(providerThing) == null) {
            throw new NotFoundException(String.format("Thing identified by %s not found", id));
        }
        ProviderSnapshot provider = validateAndGetProvider(providerLocation);
        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), provider.getService("location"));

        if (!id2.equals(l.id())) {
            throw new NotFoundException();
        }
        return l;
    }

    @Override
    public ResultList<Thing> getThingLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        return new ResultList<>(null, null, List.of(getThing(id)));
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id2);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty()) {
                list = new ResultList<>(null, null,
                        DtoMapperGet.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), filter, providerSnapshot).map(List::of).orElse(List.of()));
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
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), datastream, id,
                ExpandedThing.class, ExpandedDataStream.class);

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

}
