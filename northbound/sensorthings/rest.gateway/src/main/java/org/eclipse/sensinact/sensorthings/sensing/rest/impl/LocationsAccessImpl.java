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

import java.util.List;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.LocationsAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class LocationsAccessImpl implements LocationsAccess {

    @Context
    UriInfo uriInfo;

    @Context
    Providers providers;

    /**
     * Returns a user session
     */
    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns an object mapper
     * @return
     */
    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }


    @Override
    public Location getLocation(String id) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        Location l = DtoMapper.toLocation(getSession(), uriInfo, getMapper(), provider);

        if(!id.equals(l.id)) {
            throw new NotFoundException();
        }

        return l;
    }

    @Override
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(String id) {
        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(getLocationHistoricalLocation(id, id));
        return list;
    }

    @Override
    public HistoricalLocation getLocationHistoricalLocation(String id, String id2) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        HistoricalLocation hl = DtoMapper.toHistoricalLocation(getSession(), getMapper(), uriInfo, provider);

        if(!id2.equals(hl.id)) {
            throw new NotFoundException();
        }
        return hl;
    }

    @Override
    public Thing getLocationHistoricalLocationsThing(String id, String id2) {
        if(!id2.equals(id)) {
            throw new NotFoundException();
        }
        String provider = DtoMapper.extractFirstIdSegment(id);
        return DtoMapper.toThing(getSession(), uriInfo, provider);
    }

    @Override
    public ResultList<Location> getLocationHistoricalLocationLocations(String id, String id2) {
        ResultList<Location> list = new ResultList<>();
        list.value = List.of(getLocation(id));
        return list;
    }

    @Override
    public ResultList<Thing> getLocationThings(String id) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        ResultList<Thing> list = new ResultList<>();
        list.value = List.of(DtoMapper.toThing(getSession(), uriInfo, provider));
        return list;
    }

    @Override
    public Thing getLocationThing(String id, String id2) {
        String provider = DtoMapper.extractFirstIdSegment(id);
        return DtoMapper.toThing(getSession(), uriInfo, provider);
    }

    @Override
    public ResultList<Datastream> getLocationThingDatastreams(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        SensiNactSession userSession = getSession();

        ResultList<Datastream> list = new ResultList<>();
        list.value = userSession.filteredSnapshot(new SnapshotFilter(provider)).stream()
                .flatMap(p -> p.getServices().stream()).flatMap(s -> s.getResources().stream())
                .map(r -> DtoMapper.toDatastream(getMapper(), uriInfo, r)).collect(toList());
        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(getSession(), getMapper(), uriInfo, provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }

    @Override
    public ResultList<Location> getLocationThingLocations(String id, String id2) {
        ResultList<Location> list = new ResultList<>();
        list.value = List.of(getLocation(id));
        return list;
    }
}
