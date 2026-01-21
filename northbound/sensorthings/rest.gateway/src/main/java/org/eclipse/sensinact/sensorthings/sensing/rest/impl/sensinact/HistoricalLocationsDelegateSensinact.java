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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.getTimestampFromId;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class HistoricalLocationsDelegateSensinact extends AbstractDelegate {

    public HistoricalLocationsDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    public HistoricalLocation getHistoricalLocation(String id) {
        String provider = extractFirstIdSegment(id);
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

    public ResultList<Location> getHistoricalLocationLocations(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(getSession(), application,
                getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), providerSnapshot)));

        return list;
    }

    public Location getHistoricalLocationLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Location loc = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id2.equals(loc.id())) {
            throw new NotFoundException();
        }
        return loc;
    }

    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        return new ResultList<>(null, null, List.of(DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(THINGS), providerSnapshot)));
    }

    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensinact.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty())
                list = new ResultList<>(null, null,
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, providerSnapshot).map(List::of).orElse(List.of()));
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public Thing getHistoricalLocationThing(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t;
        try {
            t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                    providerSnapshot);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!t.id().equals(provider)) {
            throw new NotFoundException();
        }
        return t;
    }

    public ResultList<Datastream> getHistoricalLocationThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        return DatastreamsDelegateSensinact.getDataStreams(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), validateAndGetProvider(provider));
    }

    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensinact.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty())
                list = new ResultList<>(null, null,
                        DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                filter, providerSnapshot).map(List::of).orElse(List.of()));
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public ResultList<Location> getHistoricalLocationThingLocations(String id) {
        return getHistoricalLocationLocations(id);
    }

}
