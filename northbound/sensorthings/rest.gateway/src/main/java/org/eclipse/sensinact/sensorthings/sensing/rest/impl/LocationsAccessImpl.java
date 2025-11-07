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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.LocationsAccess;

import jakarta.ws.rs.NotFoundException;

public class LocationsAccessImpl extends AbstractAccess implements LocationsAccess {

    @Override
    public Location getLocation(String id) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Location l = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(LOCATIONS), providerSnapshot);

        if(!id.equals(l.id)) {
            throw new NotFoundException();
        }

        return l;
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value.isEmpty()) {
                DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                        getExpansions(), filter, providerSnapshot).ifPresent(list.value::add);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        Optional<HistoricalLocation> hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(),
                uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);

        if(hl.isEmpty() || !id2.equals(hl.get().id)) {
            throw new NotFoundException();
        }
        return hl.get();
    }

    @Override
    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        if(!id2.equals(id)) {
            throw new NotFoundException();
        }
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(THINGS), providerSnapshot);
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        ResultList<Location> list = new ResultList<>();
        list.value = List.of(getLocation(id));
        return list;
    }

    @Override
    public ResultList<Thing> getLocationThings(String id) {
        ResultList<Thing> list = new ResultList<>();
        list.value = List.of(getLocationThing(id, id));
        return list;
    }

    @Override
    public Thing getLocationThing(String id, String id2) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(THINGS), providerSnapshot);
    }

    @Override
    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), validateAndGetProvider(provider));
    }

    @Override
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelper.loadHistoricalLocations(getSession(),
                    application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value.isEmpty()) {
                DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                        getExpansions(), filter, providerSnapshot).ifPresent(list.value::add);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    @Override
    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        ResultList<Location> list = new ResultList<>();
        list.value = List.of(getLocation(id));
        return list;
    }
}
