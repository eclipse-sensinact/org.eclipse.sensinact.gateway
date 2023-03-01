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

import static java.util.stream.Collectors.toList;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.getTimestampFromId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ObservationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class ObservationsAccessImpl implements ObservationsAccess {

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
     *
     * @return
     */
    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }

    @Override
    public Observation getObservation(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Instant timestamp = DtoMapper.getTimestampFromId(id);
        ResourceDescription description = getSession().describeResource(provider, service, resource);

        Observation result = null;
        if (description != null) {
            Instant milliTimestamp = description.timestamp.truncatedTo(ChronoUnit.MILLIS);
            if (timestamp.isBefore(milliTimestamp)) {
                String history = (String) application.getProperties().get("sensinact.history.provider");
                if (history != null) {
                    TimedValue<?> t = (TimedValue<?>) getSession().actOnResource(history, "history", "single",
                            Map.of("provider", provider, "service", service, "resource", resource, "time", timestamp));
                    if (timestamp.equals(t.getTimestamp())) {
                        result = DtoMapper.toObservation(uriInfo, provider, service, resource, t);
                    }
                }
            } else if (timestamp.equals(milliTimestamp)) {
                result = DtoMapper.toObservation(uriInfo, description);
            }
        }

        if (result == null) {
            throw new NotFoundException();
        }
        return result;
    }

    @Override
    public Datastream getObservationDatastream(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        Datastream d = DtoMapper.toDatastream(userSession, getMapper(), uriInfo,
                userSession.describeResource(provider, service, resource));

        if (!String.join("~", provider, service, resource).equals(d.id)) {
            throw new NotFoundException();
        }

        return d;
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        SensiNactSession userSession = getSession();
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));
        return RootResourceAccessImpl.getObservationList(userSession, uriInfo, application, provider, service,
                resource);
    }

    @Override
    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toObservedProperty(uriInfo, getSession().describeResource(provider, service, resource));
    }

    @Override
    public Sensor getObservationDatastreamSensor(String id) {
        String provider = extractFirstIdSegment(id);
        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        return DtoMapper.toSensor(uriInfo, getSession().describeResource(provider, service, resource));
    }

    @Override
    public Thing getObservationDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toThing(getSession(), uriInfo, provider);
    }

    @Override
    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        return DtoMapper.toFeatureOfInterest(getSession(), uriInfo, getMapper(), provider);
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);
        getTimestampFromId(id);

        SensiNactSession userSession = getSession();

        ResultList<Observation> list = new ResultList<>();
        list.value = userSession.describeProvider(provider).services.stream()
                .map(s -> userSession.describeService(provider, s))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toObservation(uriInfo, r)).collect(toList());

        return list;
    }
}
