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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
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

        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.SENSORS), validateAndGetProvider(providerId));

    }

    public ResultList<Datastream> getSensorDatastreams(String id) {

        ResultList<Datastream> list = new ResultList<>(null, null, List.of(getSensorDatastream(id, id)));
        return list;
    }

    public Datastream getSensorDatastream(String id, String id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2);

        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        return DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.DATASTREAMS), validateAndGetProvider(providerId2));
    }

    @PaginationLimit(500)

    public ResultList<Observation> getSensorDatastreamObservations(String id, String id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }
        ProviderSnapshot provider = validateAndGetProvider(providerId2);
        return RootResourceDelegateSensorthings.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), provider.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"),
                parseFilter(EFilterContext.OBSERVATIONS), 0);
    }

    public ObservedProperty getSensorDatastreamObservedProperty(String id, String id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        ObservedProperty o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(EFilterContext.OBSERVED_PROPERTIES), validateAndGetProvider(providerId2));

        return o;
    }

    public Sensor getSensorDatastreamSensor(String id, String id2) {

        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        return getSensor(id);
    }

    public Thing getSensorDatastreamThing(String id, String id2) {
        String thingId = getThingIdFromDatastream(id);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(thingId);

        Thing t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(EFilterContext.THINGS), providerSnapshot);
        if (!thingId.equals(t.id())) {
            throw new NotFoundException();
        }
        return t;
    }

    public ResultList<Datastream> getSensorDatastreamThingDatastreams(String value, String value2) {
        String ThingId = getThingIdFromDatastream(value2);
        return new ResultList<Datastream>(null, null,
                getDatastreamProvidersFromThing(getSession(), ThingId).stream().map(p -> DtoMapper
                        .toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(), null, p))
                        .toList());
    }

    public Response updateSensor(String id, Sensor sensor) {

        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, sensor);
        ICriterion criterion = parseFilter(EFilterContext.SENSORS);

        Sensor createDto = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

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
        // same method in thing -> TODO refacto
        String ThingId = getThingIdFromDatastream(value2);

        ProviderSnapshot providerThing = validateAndGetProvider(ThingId);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, providerThing, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThing);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public ResultList<Location> getSensorDatastreamThingLocations(String value, String value2) {
        // same method in thing TODO refacto
        String ThingId = getThingIdFromDatastream(value2);
        ResultList<Location> list = new ResultList<>(
                null, null, getLocationIdsFromThing(getSession(), ThingId).stream()
                        .map(idLoc -> validateAndGetProvider(idLoc)).map(p -> DtoMapper.toLocation(getSession(),
                                application, getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), p))
                        .toList());

        return list;
    }

    public Observation getSensorDatastreamObservation(String value, String value2, String value3) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(value);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(value2);

        String providerObs = DtoMapperSimple.extractFirstIdSegment(value3);
        if (!providerId.equals(providerId2) || !providerObs.equals(providerId)) {
            throw new NotFoundException();
        }
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(providerObs);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);

        Optional<Observation> o = DtoMapper.toObservation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, service.getResource("lastObservation"));

        return o.get();
    }

    public FeatureOfInterest getSensorDatastreamObservationFeatureOfInterest(String value, String value2,
            String value3) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(value);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(value2);

        String providerObs = DtoMapperSimple.extractFirstIdSegment(value3);
        if (!providerId.equals(providerId2) || !providerObs.equals(providerId)) {
            throw new NotFoundException();
        }
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerSnapshot = validateAndGetProvider(providerObs);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(providerSnapshot);

        FeatureOfInterest o = DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, providerSnapshot);

        return o;
    }

    public ResultList<Observation> getSensorDatastreamObservationFeatureOfInterestObservations(String value,
            String value2, String value3) {
        String provider = extractFirstIdSegment(value);
        String provider2 = extractFirstIdSegment(value2);
        String provider3 = extractFirstIdSegment(value3);

        if (!provider.equals(provider2) || !provider3.equals(provider)) {
            throw new NotFoundException();
        }
        // refacto same method in datastream
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ProviderSnapshot providerDatastream = validateAndGetProvider(provider3);
        ResultList<Observation> observationList = RootResourceDelegateSensorthings.getObservationList(getSession(),
                application, getMapper(), uriInfo, requestContext,
                providerDatastream.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), filter);
        return observationList;
    }
}
