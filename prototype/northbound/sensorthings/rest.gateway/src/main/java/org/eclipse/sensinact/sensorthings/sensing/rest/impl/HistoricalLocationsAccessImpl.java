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

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.getTimestampFromId;

import java.util.List;

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.HistoricalLocationsAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class HistoricalLocationsAccessImpl implements HistoricalLocationsAccess {

    @Context
    SensiNactSession userSession;

    @Context
    UriInfo uriInfo;

    @Context
    ObjectMapper mapper;

    @Override
    public HistoricalLocation getHistoricalLocation(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(userSession, uriInfo, provider);
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

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(DtoMapper.toLocation(userSession, uriInfo, mapper, provider));

        return list;
    }

    @Override
    public Location getHistoricalLocationLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        Location loc = DtoMapper.toLocation(userSession, uriInfo, mapper, provider);

        if (!id2.equals(loc.id)) {
            throw new NotFoundException();
        }
        return loc;
    }

    @Override
    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ResultList<Thing> list = new ResultList<>();
        list.value = List.of(DtoMapper.toThing(userSession, uriInfo, provider));

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

        Thing t;
        try {
            t = DtoMapper.toThing(userSession, uriInfo, provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!t.id.equals(id)) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ResultList<Datastream> list = new ResultList<>();

        list.value = userSession.describeProvider(provider).services.stream()
                .map(s -> userSession.describeService(provider, s))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toDatastream(userSession, uriInfo, r)).collect(toList());

        return list;
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
