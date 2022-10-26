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
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ThingsAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class ThingsAccessImpl implements ThingsAccess {

    @Context
    SensiNactSession userSession;

    @Context
    UriInfo uriInfo;

    @Context
    ObjectMapper mapper;

    @Override
    public Thing getThing(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toThing(userSession, uriInfo, provider);
    }

    @Override
    public ResultList<Datastream> getThingDatastreams(String id) {
        String provider = extractFirstIdSegment(id);

        ResultList<Datastream> list = new ResultList<>();

        list.value = userSession.describeProvider(provider).services.stream()
                .map(s -> userSession.describeService(provider, s))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toDatastream(userSession, uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public Datastream getThingDatastream(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        String service = extractFirstIdSegment(id2.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id2.substring(provider.length() + service.length() + 2));
        Datastream d = DtoMapper.toDatastream(userSession, uriInfo,
                userSession.describeResource(provider, service, resource));

        if (!id2.equals(d.id)) {
            throw new NotFoundException();
        }
        return d;
    }

    @Override
    public ResultList<Observation> getThingDatastreamObservations(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResultList<Observation> list = new ResultList<>();
        list.value = List
                .of(DtoMapper.toObservation(uriInfo, userSession.describeResource(provider, service, resource)));
        return list;
    }

    @Override
    public ObservedProperty getThingDatastreamObservedProperty(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ObservedProperty o = DtoMapper.toObservedProperty(uriInfo,
                userSession.describeResource(provider, service, resource));

        if (!id.equals(o.id)) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public Sensor getThingDatastreamSensor(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Sensor s = DtoMapper.toSensor(uriInfo, userSession.describeResource(provider, service, resource));

        if (!id.equals(s.id)) {
            throw new NotFoundException();
        }

        return s;
    }

    @Override
    public Thing getThingDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        return getThing(id);
    }

    @Override
    public ResultList<HistoricalLocation> getThingHistoricalLocations(String id) {
        String provider = extractFirstIdSegment(id);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(userSession, uriInfo, provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }

    @Override
    public HistoricalLocation getThingHistoricalLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }

        getTimestampFromId(id2);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(userSession, uriInfo, provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        return hl;
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

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(DtoMapper.toLocation(userSession, uriInfo, mapper, provider));

        return list;
    }

    @Override
    public ResultList<Location> getThingLocations(String id) {
        String provider = extractFirstIdSegment(id);

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(DtoMapper.toLocation(userSession, uriInfo, mapper, provider));

        return list;
    }

    @Override
    public Location getThingLocation(String id, String id2) {
        String provider = extractFirstIdSegment(id2);

        if (!id.equals(provider)) {
            throw new NotFoundException();
        }
        
        getTimestampFromId(id2);

        Location l = DtoMapper.toLocation(userSession, uriInfo, mapper, provider);
        
        if(!id2.equals(l.id)) {
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
        ResultList<Thing> list = new ResultList<>();
        list.value = List.of(DtoMapper.toThing(userSession, uriInfo, provider));
        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(String id, String id2) {
        String provider = extractFirstIdSegment(id);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(userSession, uriInfo, provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }
}
