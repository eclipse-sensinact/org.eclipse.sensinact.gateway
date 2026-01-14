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
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.LocationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.delete.LocationsDelete;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.update.LocationsUpdate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class LocationsAccessImpl extends AbstractAccess implements LocationsDelete, LocationsAccess, LocationsUpdate {

    @Override
    public Location getLocation(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id.equals(l.id())) {
            throw new NotFoundException();
        }

        return l;
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ProviderSnapshot providerThing = null;// TODO
            if (providerThing == null) {// TixME
                return new ResultList<HistoricalLocation>(null, null, List.of());
            }
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
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

    @Override
    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);

        if (hl.isEmpty() || !id2.equals(hl.get().id())) {
            throw new NotFoundException();
        }
        return hl.get();
    }

    @Override
    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        if (!id2.equals(id)) {
            throw new NotFoundException();
        }
        String provider = UtilDto.extractFirstIdSegment(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        String thingId = UtilDto.extractFirstIdSegment(id2);
        List<String> providerLocationIds = getLocationIdsFromThing(getSession(), thingId);
        return new ResultList<>(null, null,
                providerLocationIds.stream().map(idLocation -> getLocation(idLocation)).toList());
    }

    @Override
    public ResultList<Thing> getLocationThings(String id) {

        getLocationThingsProvider(id).stream().map(p -> DtoMapper.toThing(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(THINGS), p));
        return new ResultList<>(null, null, List.of(getLocationThing(id, null)));
    }

    @Override
    public Thing getLocationThing(String id, String id2) {
        if (!isLocationInThing(id2, id)) {
            throw new NotFoundException();
        }
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id2);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);

    }

    @Override
    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id);
        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), provider);
    }

    @Override
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerThing = validateAndGetProvider(id2);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
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

    @Override
    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        return new ResultList<>(null, null, List.of(getLocation(id)));
    }

    @Override
    public Response updateLocation(String id, ExpandedLocation location) {
        getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id, location);

        return Response.noContent().build();
    }

    @Override
    public Response patchLocation(String id, ExpandedLocation location) {
        return updateLocation(id, location);
    }

    @Override
    public Response deleteLocation(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedLocation.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteThingsRef(String id) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedLocation.class,
                ExpandedThing.class);

        return Response.noContent().build();
    }

    @Override
    public Response deleteThingRef(String id, String id2) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id2, id, ExpandedLocation.class,
                ExpandedThing.class);

        return Response.noContent().build();
    }

}
