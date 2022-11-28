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

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.DatastreamsAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class DatastreamsAccessImpl implements DatastreamsAccess {

    @Context
    UriInfo uriInfo;

    @Context
    Providers providers;

    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    @Override
    public Datastream getDatastream(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));
        return DtoMapper.toDatastream(userSession, getMapper(), uriInfo, userSession.describeResource(provider, service, resource));
    }

    @Override
    public ResultList<Observation> getDatastreamObservations(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResultList<Observation> list = new ResultList<>();
        list.value = List
                .of(DtoMapper.toObservation(uriInfo, userSession.describeResource(provider, service, resource)));
        return list;
    }

    @Override
    public Observation getDatastreamObservation(String id, String id2) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Observation o = DtoMapper.toObservation(uriInfo, userSession.describeResource(provider, service, resource));

        if (!id2.equals(o.id)) {
            throw new NotFoundException();
        }
        return o;
    }

    @Override
    public Datastream getDatastreamObservationDatastream(String id, String id2) {
        return getDatastream(id);
    }

    @Override
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(String id, String id2) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toFeatureOfInterest(userSession, uriInfo, getMapper(), provider);
    }

    @Override
    public ObservedProperty getDatastreamObservedProperty(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
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
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>();
        list.value = List.of(getDatastream(id));
        return list;
    }

    @Override
    public Sensor getDatastreamSensor(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Sensor s = DtoMapper.toSensor(uriInfo, userSession.describeResource(provider, service, resource));

        if (!id.equals(s.id)) {
            throw new NotFoundException();
        }
        return s;
    }

    @Override
    public ResultList<Datastream> getDatastreamSensorDatastreams(String id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    @Override
    public Thing getDatastreamThing(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toThing(userSession, uriInfo, provider);
    }

    @Override
    public ResultList<Datastream> getDatastreamThingDatastreams(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);

        ResultList<Datastream> list = new ResultList<>();

        list.value = userSession.describeProvider(provider).services.stream()
                .map(s -> userSession.describeService(provider, s))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toDatastream(userSession, getMapper(), uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);

        HistoricalLocation hl;
        try {
            hl = DtoMapper.toHistoricalLocation(userSession, getMapper(), uriInfo, provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }

    @Override
    public ResultList<Location> getDatastreamThingLocations(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);

        Location hl;
        try {
            hl = DtoMapper.toLocation(userSession, uriInfo, getMapper(), provider);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }

        ResultList<Location> list = new ResultList<>();
        list.value = List.of(hl);
        return list;
    }
}
