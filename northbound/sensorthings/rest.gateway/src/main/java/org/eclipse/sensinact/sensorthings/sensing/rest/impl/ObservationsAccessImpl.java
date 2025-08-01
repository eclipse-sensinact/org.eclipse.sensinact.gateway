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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ObservationsAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import jakarta.ws.rs.NotFoundException;

public class ObservationsAccessImpl extends AbstractAccess implements ObservationsAccess {

    @Override
    public Observation getObservation(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);
        Instant timestamp = DtoMapper.getTimestampFromId(id);

        Observation result = null;
        if (resourceSnapshot.isSet()) {
            Instant milliTimestamp = resourceSnapshot.getValue().getTimestamp().truncatedTo(ChronoUnit.MILLIS);
            if (timestamp.isBefore(milliTimestamp)) {
                String history = (String) application.getProperties().get("sensinact.history.provider");
                if (history != null) {
                    String provider = resourceSnapshot.getService().getProvider().getName();
                    String service = resourceSnapshot.getService().getName();
                    String resource = resourceSnapshot.getName();
                    // +1 milli as 00:00:00.123456 (db) is always greater than 00:00:00.123000 (timestamp)
                    Instant timestampPlusOneMilli = timestamp.plusMillis(1);
                    TimedValue<?> t = (TimedValue<?>) getSession().actOnResource(history, "history", "single",
                            Map.of("provider", provider, "service", service, "resource", resource, "time", timestampPlusOneMilli));
                    if (timestamp.equals(t.getTimestamp().truncatedTo(ChronoUnit.MILLIS))) {
                        result = DtoMapper.toObservation(getSession(), application, getMapper(),
                                uriInfo, getExpansions(), resourceSnapshot, Optional.of(t));
                    }
                }
            } else if (timestamp.equals(milliTimestamp)) {
                result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                        getExpansions(), resourceSnapshot);
            }
        } else {
            result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                    getExpansions(), resourceSnapshot, Optional.empty());
        }

        if (result == null) {
            throw new NotFoundException();
        }
        return result;
    }

    @Override
    public Datastream getObservationDatastream(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(),
                uriInfo, getExpansions(), resourceSnapshot);

        if (!id.startsWith(String.valueOf(d.id))) {
            throw new NotFoundException();
        }

        return d;
    }

    @PaginationLimit(500)
    @Override
    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        return RootResourceAccessImpl.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, validateAndGetResourceSnapshot(id));
    }

    @Override
    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);
        return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), resourceSnapshot);
    }

    @Override
    public Sensor getObservationDatastreamSensor(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo,
                getExpansions(), resourceSnapshot);
        if (!id.startsWith(String.valueOf(s.id))) {
            throw new NotFoundException();
        }
        return s;
    }

    @Override
    public Thing getObservationDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), providerSnapshot);
        if (!provider.equals(t.id)) {
            throw new NotFoundException();
        }
        return t;
    }

    @Override
    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo,
                getExpansions(), providerSnapshot);
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ResultList<Observation> list = new ResultList<>();
        list.value =  providerSnapshot.getServices().stream()
                .flatMap(s -> s.getResources().stream())
                .filter(ResourceSnapshot::isSet)
                .map(r -> DtoMapper.toObservation(getSession(), application, getMapper(),
                        uriInfo, getExpansions(), r)).collect(toList());
        return list;
    }
}
