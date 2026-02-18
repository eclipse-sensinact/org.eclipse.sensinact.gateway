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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings;

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.FEATURES_OF_INTEREST;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class FeaturesOfInterestDelegateSensorthings extends AbstractDelegate {

    public FeaturesOfInterestDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public FeatureOfInterest getFeatureOfInterest(String id) {

        Instant timestamp = DtoMapperSimple.getTimestampFromId(id);
        ExpandedObservation obs = null;

        if (isHistoryMemory() && getCacheFeatureOfInterest().getDto(id) != null) {
            return getCacheFeatureOfInterest().getDto(id);

        } else {
            ResourceSnapshot resourceSnapshot = getObservationResourceSnapshot(id);
            Instant stampResource = resourceSnapshot.getValue().getTimestamp();

            if (stampResource.equals(timestamp)) {
                String val = (String) resourceSnapshot.getValue().getValue();
                obs = DtoMapperSimple.parseExpandObservation(getMapper(), val);

            } else {
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
                        obs = DtoMapperSimple.parseExpandObservation(getMapper(), t.getValue());
                    }
                }
            }
        }
        if (obs == null)
            throw new NotFoundException();
        FeatureOfInterest foi;
        try {
            foi = DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(FEATURES_OF_INTEREST), timestamp, obs);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (foi == null) {
            throw new NotFoundException();
        }
        if (!foi.id().equals(id)) {
            throw new NotFoundException();
        }
        return foi;
    }

    // No history as it is *live* observation data not a data stream

    public ResultList<Observation> getFeatureOfInterestObservations(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);

        return getLiveObservations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.OBSERVATIONS), validateAndGetProvider(provider));
    }

    static ResultList<Observation> getLiveObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {

        ServiceSnapshot datastreamService = DtoMapperSimple.getDatastreamService(provider);

        return new ResultList<>(null, null, DtoMapper.toObservation(userSession, application, mapper, uriInfo,
                expansions, filter, datastreamService.getResource("lastObservation")).map(List::of).orElse(List.of()));

    }

    public Observation getFeatureOfInterestObservation(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);
        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(EFilterContext.FEATURES_OF_INTEREST),
                service.getResource("lastObservation"));

        return o.get();
    }

    public Datastream getFeatureOfInterestObservationDatastream(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Datastream d;
        try {
            d = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(EFilterContext.DATASTREAMS), providerSnapshot).get();
        } catch (Exception e) {
            throw new NotFoundException();
        }

        return d;

    }

    public Response updateFeaturesOfInterest(String id, FeatureOfInterest foi) {

        FeatureOfInterest createDto = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, foi);

        return Response.ok().entity(createDto).build();
    }

    public Response patchFeaturesOfInterest(String id, FeatureOfInterest foi) {

        return updateFeaturesOfInterest(id, foi);
    }

    public Response deleteFeatureOfInterest(String id) {
        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, FeatureOfInterest.class);

        return Response.ok().build();
    }

    public Thing getFeatureOfInterestObservationDatastreamThing(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);
        String thingId = DtoMapperSimple.getResourceField(service, "thingId", String.class);
        ProviderSnapshot providerThing = validateAndGetProvider(thingId);
        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), null, providerThing);
    }

    public Sensor getFeatureOfInterestObservationDatastreamSensor(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper
                .toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(), null, providerSnapshot)
                .get();
    }

    public ObservedProperty getFeatureOfInterestObservationDatastreamObservedProperty(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new BadRequestException("The ids for the FeatureOfInterest and the Observation are inconsistent");
        }

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        return DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(), null,
                providerSnapshot).get();
    }

    public ResultList<Observation> getFeatureOfInterestObservationDatastreamObservations(String id, String id2) {
        // TODO refacto
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        String providerId = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot provider = validateAndGetProvider(providerId);
        ResultList<Observation> observationList = RootResourceDelegateSensorthings.getObservationList(getSession(),
                application, getMapper(), uriInfo, requestContext,
                provider.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter);
        return observationList;
    }

}
