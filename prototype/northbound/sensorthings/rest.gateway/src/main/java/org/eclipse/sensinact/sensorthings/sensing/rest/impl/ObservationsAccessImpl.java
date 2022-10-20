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
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ObservationsAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class ObservationsAccessImpl implements ObservationsAccess {

    @Context
    SensiNactSession userSession;

    @Context
    UriInfo uriInfo;

    @Context
    ObjectMapper mapper;

    @Override
    public Observation getObservation(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Observation o = DtoMapper.toObservation(uriInfo, userSession.describeResource(provider, service, resource));
        
        if(!id.equals(o.id)) {
            throw new NotFoundException();
        }
        
        return o;
    }

    @Override
    public Datastream getObservationDatastream(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Datastream d = DtoMapper.toDatastream(userSession, uriInfo, userSession.describeResource(provider, service, resource));
        
        if(!id.equals(d.id)) {
            throw new NotFoundException();
        }
        
        return d;
    }

    @Override
    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        ResultList<Observation> list = new ResultList<>();
        list.value = List.of(getObservation(id));
        return list;
    }

    @Override
    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toObservedProperty(uriInfo, userSession.describeResource(provider, service, resource));
    }

    @Override
    public Sensor getObservationDatastreamSensor(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toSensor(uriInfo, userSession.describeResource(provider, service, resource));
    }

    @Override
    public Thing getObservationDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toThing(userSession, uriInfo, provider);
    }

    @Override
    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toFeatureOfInterest(userSession, uriInfo, mapper, provider);
    }

    @Override
    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        ResultList<Observation> list = new ResultList<>();

        list.value = userSession.describeProvider(provider).services.stream()
                .map(s -> userSession.describeService(provider, s))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toObservation(uriInfo, r)).collect(toList());

        return list;
    }
}
