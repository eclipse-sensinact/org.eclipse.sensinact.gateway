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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper.getTimestampFromId;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.HistoricalLocationsAccess;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class HistoricalLocationsAccessImpl extends AbstractAccess
        implements HistoricalLocationsAccess, HistoricalLocationsDelete {

    @Override
    public HistoricalLocation getHistoricalLocation(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        try {
            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(getSession(), application,
                    getMapper(), uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);
            if (historicalLocation.isEmpty() || !historicalLocation.get().id().equals(id)) {
                throw new NotFoundException();
            }
            return historicalLocation.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
    }

    @Override
    public ResultList<Location> getHistoricalLocationLocations(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        validateAndGetProvider(provider);

        ResultList<Location> list = new ResultList<>(null, null,
                getLocationProvidersFromThing(provider).stream().map(p -> DtoMapper.toLocation(getSession(),
                        application, getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), p)).toList());

        return list;
    }

    @Override
    public Location getHistoricalLocationLocation(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Location loc = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id2.equals(loc.id())) {
            throw new NotFoundException();
        }
        return loc;
    }

    @Override
    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        return new ResultList<>(null, null, List.of(DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(THINGS), providerSnapshot)));
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(String id, String id2) {
        String providerId = UtilDto.extractFirstIdSegment(id2);

        getTimestampFromId(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);

            ProviderSnapshot providerThing = validateAndGetProvider(providerId);

            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerThing, 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThing);
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public Thing getHistoricalLocationThing(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        validateAndGetProvider(provider);
        // TODO review manage of historical location we should store link
        // location<->thing as historical location but don't get simple historical
        ProviderSnapshot providerThing = getLocationThingsProvider(provider).stream().findFirst().get();
        Thing t;
        try {
            t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                    providerThing);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!t.id().equals(provider)) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), provider);
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        getTimestampFromId(id);

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerSnapshot);
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getHistoricalLocationThingLocations(String id) {
        return getHistoricalLocationLocations(id);
    }

    @Override
    public Response deleteHistoricalLocation(String id) {
        // we don't support delete of historical location
        return Response.status(409).build();
    }

}
