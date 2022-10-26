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

import java.util.List;

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ObservedPropertiesAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class ObservedPropertiesAccessImpl implements ObservedPropertiesAccess {

    @Context
    SensiNactSession userSession;

    @Context
    UriInfo uriInfo;

    @Context
    ObjectMapper mapper;

    @Override
    public ObservedProperty getObservedProperty(String id) {
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
    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>();
        list.value = List.of(getObservedPropertyDatastream(id, id));
        return list;
    }

    @Override
    public Datastream getObservedPropertyDatastream(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toDatastream(userSession, uriInfo, userSession.describeResource(provider, service, resource));
    }

    @Override
    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResultList<Observation> list = new ResultList<>();
        list.value = List
                .of(DtoMapper.toObservation(uriInfo, userSession.describeResource(provider, service, resource)));
        return list;
    }

    @Override
    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return getObservedProperty(id);
    }

    @Override
    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toSensor(uriInfo, userSession.describeResource(provider, service, resource));
    }

    @Override
    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        if (!provider.equals(id2)) {
            throw new NotFoundException();
        }
        return DtoMapper.toThing(userSession, uriInfo, provider);
    }
}
