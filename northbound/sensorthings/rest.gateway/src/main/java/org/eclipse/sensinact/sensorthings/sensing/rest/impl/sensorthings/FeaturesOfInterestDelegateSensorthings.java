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

import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class FeaturesOfInterestDelegateSensorthings extends AbstractDelegate {

    public FeaturesOfInterestDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    public FeatureOfInterest getFeatureOfInterest(String id) {

        return getSensorThingDtoMapper().toFeatureOfInterest(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(FEATURES_OF_INTEREST), validateAndGetProvider(id));

    }

    // No history as it is *live* observation data not a data stream

    public ResultList<Observation> getFeatureOfInterestObservations(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot providerFoi = validateAndGetProvider(provider);
        @SuppressWarnings("unchecked")
        List<String> datastreamIds = DtoMapperSimple.getResourceField(
                DtoMapperSimple.getFeatureofInterestService(providerFoi), "datastreamIds", List.class);
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        List<ResultList<Observation>> list = datastreamIds.stream()
                .map(idDatastream -> validateAndGetProvider(idDatastream))
                .map(p -> RootResourceDelegateSensorthings.getObservationList(getSession(), getSensorThingDtoMapper(),
                        getMapper(), uriInfo, requestContext,
                        p.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter,
                        getHistoryProvider(), getMaxResult(), getCacheObservationIfHistoryMemory()))
                .toList();
        return new ResultList<Observation>(list.stream().flatMap(l -> l.value().stream()).toList());
    }

    static ResultList<Observation> getLiveObservations(SensiNactSession userSession, DtoMapper dtoMapper,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        return getLiveObservations(userSession, dtoMapper, mapper, uriInfo, expansions, filter, provider, null);
    }

    static ResultList<Observation> getLiveObservations(SensiNactSession userSession, DtoMapper dtoMapper,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider, String foiId) {

        ServiceSnapshot datastreamService = DtoMapperSimple.getDatastreamService(provider);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(mapper, datastreamService);
        if (foiId != null && obs != null) {
            if (!foiId.equals(obs.featureOfInterest().id())) {
                return new ResultList<Observation>(List.of());
            }
        }
        // TODO return list of obs with history that match the foi
        return new ResultList<>(dtoMapper.toObservation(userSession, mapper, uriInfo, expansions, filter,
                datastreamService.getResource("lastObservation")).map(List::of).orElse(List.of()));

    }

    public Observation getFeatureOfInterestObservation(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);

        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(),
                DtoMapperSimple.getDatastreamService(providerSnapshot));
        if (obs.featureOfInterest() == null || !obs.featureOfInterest().id().equals(provider)) {
            throw new BadRequestException(String.format(
                    "observations %s~%s are not associate with feature of interest %s", provider2, obs.id(), provider));
        }
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);
        Optional<Observation> o = getSensorThingDtoMapper().toObservation(getSession(), getMapper(), uriInfo,
                getExpansions(), parseFilter(EFilterContext.FEATURES_OF_INTEREST),
                service.getResource("lastObservation"));

        return o.get();
    }

    public Datastream getFeatureOfInterestObservationDatastream(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(),
                DtoMapperSimple.getDatastreamService(providerSnapshot));
        if (obs.featureOfInterest() == null || !obs.featureOfInterest().id().equals(provider)) {
            throw new BadRequestException(String.format(
                    "observations %s~%s are not associate with feature of interest %s", provider2, obs.id(), provider));
        }
        Datastream d = getSensorThingDtoMapper().toDatastream(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.DATASTREAMS), providerSnapshot);

        return d;

    }

    public Response updateFeaturesOfInterest(String id, FeatureOfInterest foi) {

        ProviderSnapshot provider = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, foi);
        FeatureOfInterest createDto = getSensorThingDtoMapper().toFeatureOfInterest(getSession(), getMapper(), uriInfo,
                getExpansions(), null, provider);
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
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(),
                DtoMapperSimple.getDatastreamService(providerSnapshot));
        if (obs.featureOfInterest() == null || !obs.featureOfInterest().id().equals(provider)) {
            throw new BadRequestException(String.format(
                    "observations %s~%s are not associate with feature of interest %s", provider2, obs.id(), provider));
        }
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);
        String thingId = DtoMapperSimple.getResourceField(service, "thingId", String.class);
        ProviderSnapshot providerThing = validateAndGetProvider(thingId);
        return getSensorThingDtoMapper().toThing(getSession(), getMapper(), uriInfo, getExpansions(), null,
                providerThing);
    }

    public Sensor getFeatureOfInterestObservationDatastreamSensor(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(),
                DtoMapperSimple.getDatastreamService(providerSnapshot));
        if (obs.featureOfInterest() == null || !obs.featureOfInterest().id().equals(provider)) {
            throw new BadRequestException(String.format(
                    "observations %s~%s are not associate with feature of interest %s", provider2, obs.id(), provider));
        }
        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(providerSnapshot),
                "sensorId", String.class);
        ProviderSnapshot providerSensorId = validateAndGetProvider(sensorId);
        return getSensorThingDtoMapper().toSensor(getSession(), getMapper(), uriInfo, getExpansions(), null,
                providerSensorId);
    }

    public ObservedProperty getFeatureOfInterestObservationDatastreamObservedProperty(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(),
                DtoMapperSimple.getDatastreamService(providerSnapshot));
        if (obs.featureOfInterest() == null || !obs.featureOfInterest().id().equals(provider)) {
            throw new BadRequestException(String.format(
                    "observations %s~%s are not associate with feature of interest %s", provider2, obs.id(), provider));
        }
        String observedPropertyId = DtoMapperSimple.getResourceField(
                DtoMapperSimple.getDatastreamService(providerSnapshot), "observedPropertyId", String.class);
        ProviderSnapshot providerOp = validateAndGetProvider(observedPropertyId);
        return getSensorThingDtoMapper().toObservedProperty(getSession(), getMapper(), uriInfo, getExpansions(), null,
                providerOp);
    }

    public ResultList<Observation> getFeatureOfInterestObservationDatastreamObservations(String id, String id2) {
        // TODO refacto
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        String provider = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider2);
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(),
                DtoMapperSimple.getDatastreamService(providerSnapshot));
        if (obs.featureOfInterest() == null || !obs.featureOfInterest().id().equals(provider)) {
            throw new BadRequestException(String.format(
                    "observations %s~%s are not associate with feature of interest %s", provider2, obs.id(), provider));
        }
        ResultList<Observation> observationList = RootResourceDelegateSensorthings.getObservationList(getSession(),
                getSensorThingDtoMapper(), getMapper(), uriInfo, requestContext,
                providerSnapshot.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter,
                getHistoryProvider(), getMaxResult(), getCacheObservationIfHistoryMemory());
        return observationList;
    }

}
