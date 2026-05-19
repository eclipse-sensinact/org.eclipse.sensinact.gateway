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
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVED_PROPERTIES;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.SENSORS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;

public class ObservedPropertiesDelegateSensorthings extends AbstractDelegate {

    public ObservedPropertiesDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public ObservedProperty getObservedProperty(String id) {

        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        ObservedProperty o = getSensorThingDtoMapper().toObservedProperty(getSession(), getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(datastreamId));

        if (!id.equals(o.id())) {
            throw new NotFoundException();
        }

        return o;

    }

    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {
        ICriterion filter = parseFilter(DATASTREAMS);
        List<ProviderSnapshot> datastreamProviders = getDatastreamsFiltered(getFilterParser(), getSession(), filter, id,
                ObservedProperty.class);
        List<Datastream> list = datastreamProviders.stream()
                .map(provDatastream -> getObservedPropertyDatastream(id, provDatastream.getName())).toList();
        return new ResultList<>(list);
    }

    public Datastream getObservedPropertyDatastream(String id, String id2) {

        ProviderSnapshot sensorProv = validateAndGetProvider(id);

        if (!getDatastreamsIdsFromObservedProperty(sensorProv).contains(id2)) {
            throw new NotFoundException();
        }

        Datastream ds = getSensorThingDtoMapper().toDatastream(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(DATASTREAMS), validateAndGetProvider(id2));

        return ds;
    }

    @PaginationLimit(500)

    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {

        ProviderSnapshot providerDatastream = validateAndGetProvider(id2);
        String observedPropertyId = DtoMapperSimple.getResourceField(
                DtoMapperSimple.getDatastreamService(providerDatastream), "observedPropertyId", String.class);
        if (!observedPropertyId.equals(id)) {
            throw new NotFoundException();
        }
        return RootResourceDelegateSensorthings.getObservationList(getSession(), getSensorThingDtoMapper(), getMapper(),
                uriInfo, getExpansions(), getObservationResourceSnapshot(id2), parseFilter(OBSERVATIONS),
                getHistoryProvider(), getMaxResult(), getCacheObservationIfHistoryMemory());
    }

    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerDatastream = validateAndGetProvider(providerId);
        String observedPropertyId = DtoMapperSimple.getResourceField(
                DtoMapperSimple.getDatastreamService(providerDatastream), "observedPropertyId", String.class);
        if (!observedPropertyId.equals(id)) {
            throw new NotFoundException();
        }
        return getObservedProperty(id);
    }

    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerDatastream = validateAndGetProvider(providerId);
        String observedPropertyId = DtoMapperSimple.getResourceField(
                DtoMapperSimple.getDatastreamService(providerDatastream), "observedPropertyId", String.class);
        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(providerDatastream),
                "sensorId", String.class);

        if (!observedPropertyId.equals(id)) {
            throw new NotFoundException();
        }
        Sensor s = getSensorThingDtoMapper().toSensor(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(sensorId));
        return s;
    }

    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String providerOp = DtoMapperSimple.extractFirstIdSegment(id);
        String providerDatastream = DtoMapperSimple.extractFirstIdSegment(id2);
        String ObservedPropertyId = DtoMapperSimple.getResourceField(
                DtoMapperSimple.getDatastreamService(validateAndGetProvider(providerDatastream)), "observedPropertyId",
                String.class);
        if (!ObservedPropertyId.equals(providerOp)) {
            throw new NotFoundException();
        }
        String thingId = getThingIdFromDatastream(providerDatastream);

        return getSensorThingDtoMapper().toThing(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(THINGS), validateAndGetProvider(thingId));
    }

    public Response updateObservedProperties(String id, ObservedProperty observedProperty) {

        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, observedProperty);

        ObservedProperty createDto = getSensorThingDtoMapper().toObservedProperty(getSession(), getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), snapshot);

        return Response.ok().entity(createDto).build();
    }

    public Response patchObservedProperties(String id, ObservedProperty observedProperty) {

        return updateObservedProperties(id, observedProperty);
    }

    public Response deleteObservedProperty(String id) {

        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, ObservedProperty.class);

        return Response.ok().build();
    }

    public ResultList<Datastream> getObservedPropertyDatastreamThingDatastreams(String value, String value2) {
        String ThingId = getThingIdFromDatastream(value2);
        return new ResultList<Datastream>(
                getDatastreamsFiltered(getFilterParser(), getSession(), parseFilter(DATASTREAMS), ThingId, Thing.class)
                        .stream().map(p -> getSensorThingDtoMapper().toDatastream(getSession(), getMapper(), uriInfo,
                                getExpansions(), null, p))
                        .toList());
    }

    public ResultList<HistoricalLocation> getObservedPropertyDatastreamThingHistoricalLocations(String value,
            String value2) {
        String thingId = getThingIdFromDatastream(value2);
        return getHistoricalLocations(thingId);
    }

    public ResultList<Location> getObservedPropertyDatastreamThingLocations(String value, String value2) {
        return getDatastreamThingLocations(value2);
    }

    public ResultList<Observation> getObservedPropertyDatastreamObservationFeatureOfInterestObservations(String value,
            String value2, String value3) {
        ProviderSnapshot providerDatastream = validateDatastreamRelation(value, value2, value3,
                "observedPropertyId");
        // refacto same method in datastream
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResultList<Observation> observationList = RootResourceDelegateSensorthings.getObservationList(getSession(),
                getSensorThingDtoMapper(), getMapper(), uriInfo, requestContext,
                providerDatastream.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter,
                getHistoryProvider(), getMaxResult(25), getCacheObservationIfHistoryMemory());
        return observationList;
    }

    public FeatureOfInterest getObservedPropertyDatastreamObservationFeatureOfInterest(String value, String value2,
            String value3) {
        validateDatastreamRelation(value, value2, value3, "observedPropertyId");
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResourceSnapshot resource = getObservationResourceSnapshot(value3);
        String val = resource.getValue() != null ? (String) resource.getValue().getValue() : null;
        if (val == null) {
            throw new NotFoundException();
        }
        Instant stamp = resource.getValue().getTimestamp();

        ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(getMapper(), val);
        FeatureOfInterest o = getSensorThingDtoMapper().toFeatureOfInterest(getSession(), getMapper(), uriInfo,
                getExpansions(), filter, validateAndGetProvider(obs.featureOfInterest().id().toString()));

        return o;
    }

    public Observation getObservedPropertyDatastreamObservation(String value, String value2, String value3) {
        validateDatastreamRelation(value, value2, value3, "observedPropertyId");
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(extractFirstIdSegment(value3));
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);

        Optional<Observation> o = getSensorThingDtoMapper().toObservation(getSession(), getMapper(), uriInfo,
                getExpansions(), filter, service.getResource("lastObservation"));

        return o.get();
    }

}
