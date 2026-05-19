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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.extractFirstIdSegment;

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

public class SensorsDelegateSensorthings extends AbstractDelegate {

    public SensorsDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    public Sensor getSensor(String id) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        return getSensor(validateAndGetProvider(providerId));
    }

    private Sensor getSensor(ProviderSnapshot provider) {

        Sensor s = getSensorThingDtoMapper().toSensor(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.SENSORS), provider);

        return s;
    }

    public ResultList<Datastream> getSensorDatastreams(String id) {
        List<ProviderSnapshot> datastreamProv = getDatastreamsFiltered(getFilterParser(), getSession(),
                parseFilter(EFilterContext.DATASTREAMS), id, Sensor.class);
        List<Datastream> list = datastreamProv.stream()
                .map(provDatastream -> getSensorDatastream(id, provDatastream.getName())).toList();
        return new ResultList<>(list);
    }

    public Datastream getSensorDatastream(String id, String id2) {

        ProviderSnapshot provider = validateAndGetProvider(id2);

        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(provider), "sensorId",
                String.class);
        if (!sensorId.equals(id)) {
            throw new NotFoundException();
        }
        Datastream od = getSensorThingDtoMapper().toDatastream(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.DATASTREAMS), validateAndGetProvider(id2));

        return od;
    }

    @PaginationLimit(500)

    public ResultList<Observation> getSensorDatastreamObservations(String id, String id2) {

        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot provider = validateAndGetProvider(providerId2);

        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(provider), "sensorId",
                String.class);
        if (!sensorId.equals(id)) {
            throw new NotFoundException();
        }
        return RootResourceDelegateSensorthings.getObservationList(getSession(), getSensorThingDtoMapper(), getMapper(),
                uriInfo, getExpansions(), provider.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"),
                parseFilter(EFilterContext.OBSERVATIONS), getHistoryProvider(), getMaxResult(),
                getCacheObservationIfHistoryMemory());
    }

    public ObservedProperty getSensorDatastreamObservedProperty(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerDatastream = validateAndGetProvider(providerId);
        ProviderSnapshot provider = getEntityIdFromDatastream("observedPropertyId", providerDatastream);
        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(providerDatastream),
                "sensorId", String.class);

        if (!sensorId.equals(id)) {
            throw new NotFoundException();
        }
        ObservedProperty o = getSensorThingDtoMapper().toObservedProperty(getSession(), getMapper(), uriInfo,
                getExpansions(), parseFilter(EFilterContext.OBSERVED_PROPERTIES), provider);

        return o;
    }

    public Sensor getSensorDatastreamSensor(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id2);
        ProviderSnapshot providerDatastream = validateAndGetProvider(providerId);
        ProviderSnapshot provider = getEntityIdFromDatastream("sensorId", providerDatastream);

        if (!provider.getName().equals(id)) {
            throw new NotFoundException();
        }

        return getSensor(provider);
    }

    public Thing getSensorDatastreamThing(String id, String id2) {
        String thingId = getThingIdFromDatastream(id2);
        ProviderSnapshot providerDatastream = validateAndGetProvider(id2);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(thingId);

        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(providerDatastream),
                "sensorId", String.class);

        if (!sensorId.equals(id)) {
            throw new NotFoundException();
        }
        Thing t = getSensorThingDtoMapper().toThing(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.THINGS), providerSnapshot);
        if (!thingId.equals(t.id())) {
            throw new NotFoundException();
        }
        return t;
    }

    public ResultList<Datastream> getSensorDatastreamThingDatastreams(String value, String value2) {

        ProviderSnapshot provider = validateAndGetProvider(value2);

        String sensorId = DtoMapperSimple.getResourceField(DtoMapperSimple.getDatastreamService(provider), "sensorId",
                String.class);
        if (!sensorId.equals(value)) {
            throw new NotFoundException();
        }
        String ThingId = getThingIdFromDatastream(value2);
        return new ResultList<Datastream>(
                getDatastreamProvidersFromThing(getSession(), ThingId).stream().map(p -> getSensorThingDtoMapper()
                        .toDatastream(getSession(), getMapper(), uriInfo, getExpansions(), null, p)).toList());
    }

    public Response updateSensor(String id, Sensor sensor) {

        ProviderSnapshot provider = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, sensor);

        Sensor createDto = getSensorThingDtoMapper().toSensor(getSession(), getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.SENSORS), provider);

        return Response.ok().entity(createDto).build();
    }

    public Response patchSensor(String id, Sensor sensor) {

        return updateSensor(id, sensor);
    }

    public Response deleteSensor(String id) {

        getExtraDelegate().delete(getSession(), getMapper(), uriInfo, id, Sensor.class);

        return Response.ok().build();
    }

    public ResultList<HistoricalLocation> getSensorDatastreamThingHistoricalLocations(String value, String value2) {
        String thingId = getThingIdFromDatastream(value2);

        return getHistoricalLocations(thingId);

    }

    public ResultList<Location> getSensorDatastreamThingLocations(String value, String value2) {
        return getDatastreamThingLocations(value2);
    }

    public Observation getSensorDatastreamObservation(String value, String value2, String value3) {
        validateDatastreamRelation(value, value2, value3, "sensorId");
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(extractFirstIdSegment(value3));
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);

        Optional<Observation> o = getSensorThingDtoMapper().toObservation(getSession(), getMapper(), uriInfo,
                getExpansions(), filter, service.getResource("lastObservation"));

        return o.get();
    }

    public FeatureOfInterest getSensorDatastreamObservationFeatureOfInterest(String value, String value2,
            String value3) {
        validateDatastreamRelation(value, value2, value3, "sensorId");
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResourceSnapshot resource = getObservationResourceSnapshot(value3);
        String val = resource.getValue() != null ? (String) resource.getValue().getValue() : null;
        if (val == null) {
            throw new NotFoundException();
        }

        ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(getMapper(), val);
        FeatureOfInterest o = getSensorThingDtoMapper().toFeatureOfInterest(getSession(), getMapper(), uriInfo,
                getExpansions(), filter, validateAndGetProvider(obs.featureOfInterest().id().toString()));

        return o;
    }

    public ResultList<Observation> getSensorDatastreamObservationFeatureOfInterestObservations(String value,
            String value2, String value3) {
        ProviderSnapshot providerDatastream = validateDatastreamRelation(value, value2, value3, "sensorId");
        // refacto same method in datastream
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResultList<Observation> observationList = RootResourceDelegateSensorthings.getObservationList(getSession(),
                getSensorThingDtoMapper(), getMapper(), uriInfo, requestContext,
                providerDatastream.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter,
                getHistoryProvider(), getMaxResult(), getCacheObservationIfHistoryMemory());
        return observationList;
    }
}
