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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.getTimestampFromId;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.HistoricalLocationsAccess;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

public class HistoricalLocationsAccessImpl extends AbstractAccess
        implements HistoricalLocationsAccess, HistoricalLocationsDelete {

    @Override
    public HistoricalLocation getHistoricalLocation(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        try {
            Optional<HistoricalLocation> historicalLocation = DtoMapperGet.toHistoricalLocation(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS),
                    providerSnapshot);
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
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapperGet.toLocation(getSession(),
                application, getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), providerSnapshot)));

        return list;
    }

    @Override
    public Location getHistoricalLocationLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Location loc = DtoMapperGet.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id2.equals(loc.id())) {
            throw new NotFoundException();
        }
        return loc;
    }

    @Override
    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        return new ResultList<>(null, null, List.of(DtoMapperGet.toThing(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(THINGS), providerSnapshot)));
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(String id, String id2) {
        String providerId = extractFirstIdSegment(id2);

        getTimestampFromId(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);

            ProviderSnapshot providerLocation = validateAndGetProvider(providerId);

            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, List.of(providerLocation), 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, List.of(providerLocation));
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public Thing getHistoricalLocationThing(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t;
        try {
            t = DtoMapperGet.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(THINGS), providerSnapshot);
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
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), provider);
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);
        // TODO find thing link to location then get locations providers

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, List.of(providerSnapshot), 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, List.of(providerSnapshot));
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
