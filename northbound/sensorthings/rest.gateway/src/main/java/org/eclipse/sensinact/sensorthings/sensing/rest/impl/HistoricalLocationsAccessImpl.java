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

import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.getTimestampFromId;

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.HistoricalLocationsAccess;

import jakarta.ws.rs.NotFoundException;

public class HistoricalLocationsAccessImpl extends AbstractAccess implements HistoricalLocationsAccess {

    @Override
    public HistoricalLocation getHistoricalLocation(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), providerSnapshot);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!hl.id.equals(id)) {
            throw new NotFoundException();
        }
        return hl;
    }

    @Override
    public ResultList<Location> getHistoricalLocationLocations(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), providerSnapshot));

        return list;
    }

    @Override
    public Location getHistoricalLocationLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Location loc = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), providerSnapshot);

        if (!id2.equals(loc.id)) {
            throw new NotFoundException();
        }
        return loc;
    }

    @Override
    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ResultList<Thing> list = new ResultList<>();
        list.value = List.of(DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), providerSnapshot));

        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(String id, String id2) {
        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(getHistoricalLocation(id));
        return list;
    }

    @Override
    public Thing getHistoricalLocationThing(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t;
        try {
            t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), providerSnapshot);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!t.id.equals(provider)) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        return DatastreamsAccessImpl.getDataStreams(getSession(), application, getMapper(), uriInfo, getExpansions(),
                validateAndGetProvider(provider));
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(String id) {
        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(getHistoricalLocation(id));
        return list;
    }

    @Override
    public ResultList<Location> getHistoricalLocationThingLocations(String id) {
        return getHistoricalLocationLocations(id);
    }

}
