/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
import org.eclipse.sensinact.sensorthings.sensing.rest.SensorsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class SensorsAccessImpl implements SensorsAccess {

    @Context
    UriInfo uriInfo;

    @Context
    Providers providers;

    @Context
    Application application;

    /**
     * Returns a user session
     */
    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns an object mapper
     */
    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }

    @Override
    public Sensor getSensor(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));
        return DtoMapper.toSensor(uriInfo, getSession().describeResource(provider, service, resource));
    }

    @Override
    public ResultList<Datastream> getSensorDatastreams(String id) {
        ResultList<Datastream> list = new ResultList<>();
        list.value = List.of(getSensorDatastream(id, id));
        return list;
    }

    @Override
    public Datastream getSensorDatastream(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toDatastream(userSession, getMapper(), uriInfo,
                userSession.describeResource(provider, service, resource));
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getSensorDatastreamObservations(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return RootResourceAccessImpl.getObservationList(getSession(), uriInfo, application, provider, service,
                resource);
    }

    @Override
    public ObservedProperty getSensorDatastreamObservedProperty(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ObservedProperty o = DtoMapper.toObservedProperty(uriInfo,
                getSession().describeResource(provider, service, resource));

        if (!id.equals(o.id)) {
            throw new NotFoundException();
        }

        return o;
    }

    @Override
    public Sensor getSensorDatastreamSensor(String id, String id2) {
        if (!id.equals(id2)) {
            throw new NotFoundException();
        }
        return getSensor(id);
    }

    @Override
    public Thing getSensorDatastreamThing(String id, String id2) {
        String provider = extractFirstIdSegment(id);
        Thing t = DtoMapper.toThing(getSession(), uriInfo, provider);
        if (!provider.equals(t.id)) {
            throw new NotFoundException();
        }
        return t;
    }
}
