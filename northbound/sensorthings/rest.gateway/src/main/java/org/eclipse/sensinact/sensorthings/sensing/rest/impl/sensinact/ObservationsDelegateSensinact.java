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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class ObservationsDelegateSensinact extends AbstractDelegate {

    public ObservationsDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    public Observation getObservation(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);
        Instant timestamp = DtoMapper.getTimestampFromId(id);

        ICriterion criterion = parseFilter(OBSERVATIONS);
        Optional<Observation> result = null;
        if (resourceSnapshot.isSet()) {
            Instant milliTimestamp = resourceSnapshot.getValue().getTimestamp().truncatedTo(ChronoUnit.MILLIS);
            if (timestamp.isBefore(milliTimestamp)) {
                String history = (String) application.getProperties().get("sensinact.history.provider");
                if (history != null) {
                    String provider = resourceSnapshot.getService().getProvider().getName();
                    String service = resourceSnapshot.getService().getName();
                    String resource = resourceSnapshot.getName();
                    // +1 milli as 00:00:00.123456 (db) is always greater than 00:00:00.123000
                    // (timestamp)
                    Instant timestampPlusOneMilli = timestamp.plusMillis(1);
                    TimedValue<?> t = (TimedValue<?>) getSession().actOnResource(history, "history", "single",
                            Map.of("provider", provider, "service", service, "resource", resource, "time",
                                    timestampPlusOneMilli));
                    if (timestamp.equals(t.getTimestamp().truncatedTo(ChronoUnit.MILLIS))) {
                        result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), criterion, resourceSnapshot, Optional.of(t));
                    }
                }
            } else if (timestamp.equals(milliTimestamp)) {
                result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        criterion, resourceSnapshot);
            }
        } else {
            result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    criterion, resourceSnapshot, Optional.empty());
        }

        if (result.isEmpty()) {
            throw new NotFoundException();
        }
        return result.get();
    }

    public Datastream getObservationDatastream(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                resourceSnapshot, parseFilter(DATASTREAMS));

        if (!id.startsWith(String.valueOf(d.id()))) {
            throw new NotFoundException();
        }

        return d;
    }

    @PaginationLimit(500)

    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        return RootResourceDelegateSensinact.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), validateAndGetResourceSnapshot(id), parseFilter(OBSERVATIONS), 0);
    }

    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);
        return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(OBSERVED_PROPERTIES), resourceSnapshot);
    }

    public Sensor getObservationDatastreamSensor(String id) {
        ResourceSnapshot resourceSnapshot = validateAndGetResourceSnapshot(id);

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), resourceSnapshot);
        if (!id.startsWith(String.valueOf(s.id()))) {
            throw new NotFoundException();
        }
        return s;
    }

    public Thing getObservationDatastreamThing(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Thing t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), providerSnapshot);
        if (!provider.equals(t.id())) {
            throw new NotFoundException();
        }
        return t;
    }

    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), providerSnapshot);
    }

    // No history as it is *live* observation data not a data stream

    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String provider = extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        ICriterion criterion = parseFilter(OBSERVATIONS);
        return new ResultList<>(null, null, providerSnapshot.getServices().stream()
                .flatMap(s -> s.getResources().stream()).filter(ResourceSnapshot::isSet).map(r -> DtoMapper
                        .toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, r))
                .filter(Optional::isPresent).map(Optional::get).toList());
    }

}
