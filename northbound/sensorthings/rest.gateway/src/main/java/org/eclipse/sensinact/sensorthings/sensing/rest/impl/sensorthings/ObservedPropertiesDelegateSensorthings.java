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

import jakarta.ws.rs.InternalServerErrorException;
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
        Optional<ObservedProperty> o = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(OBSERVED_PROPERTIES), validateAndGetProvider(datastreamId));

        if (o.isEmpty())
            throw new NotFoundException();

        if (!id.equals(o.get().id())) {
            throw new NotFoundException();
        }

        return o.get();

    }

    public ResultList<Datastream> getObservedPropertyDatastreams(String id) {

        return new ResultList<>(null, null, List.of(getObservedPropertyDatastream(id, id)));
    }

    public Datastream getObservedPropertyDatastream(String id, String id2) {

        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        String datastreamId2 = DtoMapperSimple.extractFirstIdSegment(id2);

        if (!datastreamId.equals(datastreamId2)) {
            throw new NotFoundException();
        }

        Optional<Datastream> ds = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), validateAndGetProvider(datastreamId));
        if (ds.isEmpty())
            throw new NotFoundException();
        return ds.get();
    }

    @PaginationLimit(500)

    public ResultList<Observation> getObservedPropertyDatastreamObservations(String id, String id2) {

        String datastreamId = DtoMapperSimple.extractFirstIdSegment(id);
        String datastreamId2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!datastreamId.equals(datastreamId2)) {
            throw new NotFoundException();
        }
        return RootResourceDelegateSensorthings.getObservationList(getSession(), application, getMapper(), uriInfo,
                getExpansions(), getObservationResourceSnapshot(datastreamId2), parseFilter(OBSERVATIONS), 0);
    }

    public ObservedProperty getObservedPropertyDatastreamObservedProperty(String id, String id2) {

        String providerDatastream2 = DtoMapperSimple.extractFirstIdSegment(id2);
        String providerDatastream = DtoMapperSimple.extractFirstIdSegment(id);

        if (!providerDatastream.equals(providerDatastream2)) {
            throw new NotFoundException();
        }
        return getObservedProperty(id);
    }

    public Sensor getObservedPropertyDatastreamSensor(String id, String id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2);

        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }

        Optional<Sensor> s = DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(SENSORS), validateAndGetProvider(providerId2));
        if (s.isEmpty())
            throw new NotFoundException();
        return s.get();
    }

    public Thing getObservedPropertyDatastreamThing(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        String provider2 = DtoMapperSimple.extractFirstIdSegment(id2);
        if (!provider.equals(provider2)) {
            throw new NotFoundException();
        }
        String thingId = getThingIdFromDatastream(provider);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                validateAndGetProvider(thingId));
    }

    public Response updateObservedProperties(String id, ObservedProperty observedProperty) {

        Object result = getExtraDelegate().update(getSession(), getMapper(), uriInfo, requestContext.getMethod(), id,
                observedProperty);
        ObservedProperty createDto = null;
        if (result instanceof ProviderSnapshot) {
            ProviderSnapshot snapshot = (ProviderSnapshot) result;
            createDto = DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    parseFilter(OBSERVED_PROPERTIES), snapshot).get();
        } else if (result instanceof ObservedProperty) {
            createDto = (ObservedProperty) result;
        } else {
            throw new InternalServerErrorException();
        }

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
        return new ResultList<Datastream>(null, null,
                getDatastreamProvidersFromThing(getSession(), ThingId).stream().map(p -> DtoMapper
                        .toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(), null, p))
                        .filter(ds -> ds.isPresent()).map(ds -> ds.get()).toList());
    }

    public ResultList<HistoricalLocation> getObservedPropertyDatastreamThingHistoricalLocations(String value,
            String value2) {
        // same method in thing -> TODO refacto
        String ThingId = getThingIdFromDatastream(value2);

        ProviderSnapshot providerThing = validateAndGetProvider(ThingId);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, providerThing,
                    isHistoryMemory() ? getCacheHistoricalLocation() : null, 0);
            if (list.value().isEmpty()) {
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerThing);
            }
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public ResultList<Location> getObservedPropertyDatastreamThingLocations(String value, String value2) {
        // same method in thing TODO refacto
        String ThingId = getThingIdFromDatastream(value2);
        ResultList<Location> list = new ResultList<>(
                null, null, getLocationIdsFromThing(getSession(), ThingId).stream()
                        .map(idLoc -> validateAndGetProvider(idLoc)).map(p -> DtoMapper.toLocation(getSession(),
                                application, getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), p))
                        .toList());

        return list;
    }

    public ResultList<Observation> getObservedPropertyDatastreamObservationFeatureOfInterestObservations(String value,
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

    public FeatureOfInterest getObservedPropertyDatastreamObservationFeatureOfInterest(String value, String value2,
            String value3) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(value);
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(value2);

        String providerObs = DtoMapperSimple.extractFirstIdSegment(value3);
        if (!providerId.equals(providerId2) || !providerObs.equals(providerId)) {
            throw new NotFoundException();
        }
        ICriterion filter = parseFilter(EFilterContext.OBSERVATIONS);
        ResourceSnapshot resource = getObservationResourceSnapshot(value3);
        String val = resource.getValue() != null ? (String) resource.getValue().getValue() : null;
        if (val == null) {
            throw new NotFoundException();
        }
        Instant stamp = resource.getValue().getTimestamp();

        ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(getMapper(), val);
        FeatureOfInterest o = DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo,
                getExpansions(), filter, stamp, obs);

        return o;
    }

    public Observation getObservedPropertyDatastreamObservation(String value, String value2, String value3) {
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

}
