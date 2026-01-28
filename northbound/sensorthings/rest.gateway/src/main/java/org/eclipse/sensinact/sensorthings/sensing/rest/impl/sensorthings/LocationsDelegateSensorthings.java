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
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class LocationsDelegateSensorthings extends AbstractDelegate {

    public LocationsDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public Location getLocation(String id) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id.equals(l.id())) {
            throw new NotFoundException();
        }

        return l;
    }

    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            List<ProviderSnapshot> providerThings = getLocationThingsProvider(id);
            if (providerThings.size() == 0) {// TixME
                return new ResultList<HistoricalLocation>(null, null, List.of());
            }
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerThings, id, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThings, id);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);

        if (hl.isEmpty() || !id2.equals(hl.get().id())) {
            throw new NotFoundException();
        }
        return hl.get();
    }

    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        if (!id2.equals(id)) {
            throw new NotFoundException();
        }
        String provider = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        String thingId = DtoMapperSimple.extractFirstIdSegment(id2);
        List<String> providerLocationIds = getLocationIdsFromThing(getSession(), thingId);
        return new ResultList<>(null, null,
                providerLocationIds.stream().map(idLocation -> getLocation(idLocation)).toList());
    }

    public ResultList<Thing> getLocationThings(String id) {

        return new ResultList<>(null, null,
                getLocationThingsProvider(id).stream().map(p -> DtoMapper.toThing(getSession(), application,
                        getMapper(), uriInfo, getExpansions(), parseFilter(THINGS), p)).toList());
    }

    public Thing getLocationThing(String id, String id2) {
        if (!isLocationInThing(id2, id)) {
            throw new NotFoundException();
        }
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id2);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);

    }

    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {
        if (!isLocationInThing(id2, id)) {
            throw new BadRequestException();
        }
        String provider = DtoMapperSimple.extractFirstIdSegment(id2);

        return DatastreamsDelegateSensorthings.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), provider);
    }

    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerThing = validateAndGetProvider(id2);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerThing, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThing);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        return new ResultList<>(null, null, List.of(getLocation(id)));
    }

    public Response updateLocation(String id, ExpandedLocation location) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, location);

        return Response.noContent().build();
    }

    public Response patchLocation(String id, ExpandedLocation location) {
        return updateLocation(id, location);
    }

    public Response deleteLocation(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedLocation.class);

        return Response.noContent().build();
    }

    public Response deleteThingsRef(String id) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedLocation.class,
                ExpandedThing.class);

        return Response.noContent().build();
    }

    public Response deleteThingRef(String id, String id2) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id2, id, ExpandedLocation.class,
                ExpandedThing.class);

        return Response.noContent().build();
    }

}
