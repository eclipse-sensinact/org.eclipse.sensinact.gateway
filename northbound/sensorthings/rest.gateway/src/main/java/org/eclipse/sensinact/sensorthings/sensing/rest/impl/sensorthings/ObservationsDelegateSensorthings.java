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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;

public class ObservationsDelegateSensorthings extends AbstractDelegate {

    public ObservationsDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public Observation getObservation(String id) {

        ResourceSnapshot resourceSnapshot = getObservationResourceSnapshot(id);
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
                                getExpansions(), criterion, resourceSnapshot, t);
                    }
                }
            } else if (timestamp.equals(milliTimestamp)) {
                result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        criterion, resourceSnapshot);
            }
        } else {
            result = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    criterion, resourceSnapshot);
        }

        if (result.isEmpty()) {
            throw new NotFoundException();
        }
        return result.get();
    }

    public Datastream getObservationDatastream(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(datastreamId);

        Datastream d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), providerSnapshot);

        if (!datastreamId.equals(String.valueOf(d.id()))) {
            throw new NotFoundException();
        }

        return d;
    }

    @PaginationLimit(500)

    public ResultList<Observation> getObservationDatastreamObservations(String id) {
        return RootResourceDelegateSensorthings.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), getObservationResourceSnapshot(id), parseFilter(OBSERVATIONS), 0);
    }

    public ObservedProperty getObservationDatastreamObservedProperty(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(datastreamId);
        return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(OBSERVED_PROPERTIES), providerSnapshot);
    }

    public Sensor getObservationDatastreamSensor(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(datastreamId);

        Sensor s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), providerSnapshot);

        return s;
    }

    public Thing getObservationDatastreamThing(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);

        String idThing = getThingIdFromDatastream(datastreamId);

        Thing t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), validateAndGetProvider(idThing));

        return t;
    }

    public FeatureOfInterest getObservationFeatureOfInterest(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(datastreamId);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), providerSnapshot);
    }

    // No history as it is *live* observation data not a data stream

    public ResultList<Observation> getObservationFeatureOfInterestObservations(String id) {
        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        ICriterion criterion = parseFilter(OBSERVATIONS);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(datastreamId);
        ServiceSnapshot serviceDatastream = DtoMapperSimple.getDatastreamService(providerSnapshot);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(), serviceDatastream);
        if (obs == null || obs.featureOfInterest() == null) {
            throw new NotFoundException();
        }
        Optional<Observation> lastObs = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, serviceDatastream.getResource("lastObservation"));

        return new ResultList<>(null, null, lastObs.isEmpty() ? List.of() : List.of(lastObs.get()));

    }

    public Response updateObservationDatastreamRef(String id, RefId datastream) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), datastream, id,
                ExpandedObservation.class, ExpandedDataStream.class);

        return Response.noContent().build();
    }

    public Response updateObservationFeatureOfInterestRef(String id, RefId foi) {
        getExtraDelegate().updateRef(getSession(), getMapper(), uriInfo, requestContext.getMethod(), foi, id,
                ExpandedObservation.class, FeatureOfInterest.class);

        return Response.noContent().build();
    }

    public Response deleteObservation(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ExpandedObservation.class);

        return Response.noContent().build();
    }

    public Response deleteObservationFeatureOfInterest(String id) {
        getExtraDelegate().deleteRef(getSession(), getMapper(), uriInfo, id, ExpandedObservation.class,
                FeatureOfInterest.class);

        return Response.noContent().build();
    }

}
