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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.DATASTREAMS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple.getTimestampFromId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class HistoricalLocationsDelegateSensorthings extends AbstractDelegate {

    public HistoricalLocationsDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    @Override
    public HistoricalLocation getHistoricalLocation(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        Instant timestamp = getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        ResourceSnapshot location = providerSnapshot.getResource(DtoMapperSimple.SERVICE_ADMIN,
                DtoMapperSimple.LOCATION);
        Instant resourceStamp = location.getValue().getTimestamp();
        if (isHistoryMemory() && getCacheHistoricalLocation().getDto(id) != null) {
            return DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                    null, id, getCacheHistoricalLocation().getDto(id));
        } else if (!timestamp.equals(resourceStamp)) {
            throw new NotFoundException();
        }
        try {
            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(getSession(), application,
                    getMapper(), uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);
            if (historicalLocation.isEmpty() || !historicalLocation.get().id().equals(id)) {
                throw new NotFoundException();
            }
            return historicalLocation.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
    }

    public ResultList<Location> getHistoricalLocationLocations(String id) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        getTimestampFromId(id);

        validateAndGetProvider(provider);

        ResultList<Location> list = new ResultList<>(null, null,
                getLocationProvidersFromThing(provider).stream().map(p -> DtoMapper.toLocation(getSession(),
                        application, getMapper(), uriInfo, getExpansions(), parseFilter(LOCATIONS), p)).toList());

        return list;
    }

    public Location getHistoricalLocationLocation(String id, String id2) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id2);
        getTimestampFromId(id);
        String thingId = DtoMapperSimple.extractFirstIdSegment(id);
        if (!isLocationInThing(thingId, id2)) {
            throw new BadRequestException();
        }
        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        Location loc = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                parseFilter(LOCATIONS), providerSnapshot);

        if (!id2.equals(loc.id())) {
            throw new NotFoundException();
        }
        return loc;
    }

    public ResultList<Thing> getHistoricalLocationLocationThings(String id, String id2) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        return new ResultList<>(null, null, List.of(DtoMapper.toThing(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(THINGS), providerSnapshot)));
    }

    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(String id, String id2) {

        String thingId = DtoMapperSimple.extractFirstIdSegment(id);

        getTimestampFromId(id);
        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot thingProvider = validateAndGetProvider(thingId);
            if (!isLocationInThing(thingId, id2)) {
                throw new BadRequestException();
            }
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, List.of(thingProvider),
                    id2, 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, List.of(thingProvider), id2);
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public Thing getHistoricalLocationThing(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        getTimestampFromId(id);

        ProviderSnapshot providerThing = validateAndGetProvider(provider);

        Thing t;
        try {
            t = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                    providerThing);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
        if (!t.id().equals(provider)) {
            throw new NotFoundException();
        }
        return t;
    }

    public ResultList<Datastream> getHistoricalLocationThingDatastreams(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        getTimestampFromId(id);

        return DatastreamsDelegateSensorthings.getDataStreams(getSession(), application, getMapper(), uriInfo,
                getExpansions(), parseFilter(DATASTREAMS), provider);
    }

    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        getTimestampFromId(id);

        try {
            ICriterion filter = parseFilter(HISTORICAL_LOCATIONS);
            ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
            ResultList<HistoricalLocation> list = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    getSession(), application, getMapper(), uriInfo, getExpansions(), filter, providerSnapshot, 0);
            if (list.value().isEmpty())
                list = DtoMapper.toHistoricalLocations(getSession(), application, getMapper(), uriInfo, getExpansions(),
                        filter, providerSnapshot);
            return list;
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException();
        }
    }

    public ResultList<Location> getHistoricalLocationThingLocations(String id) {
        return getHistoricalLocationLocations(id);
    }

    public FeatureOfInterest getHistoricalLocationThingDatastreamObservationFeatureOfInterest(String value) {

        ResourceSnapshot resource = getObservationResourceSnapshot(value);
        ICriterion criterion = parseFilter(HISTORICAL_LOCATIONS);
        String val = resource.getValue() != null ? (String) resource.getValue().getValue() : null;
        Instant stamp = resource.getValue().getTimestamp();
        if (val == null) {
            throw new NotFoundException();
        }
        ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(getMapper(), val);
        return DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, stamp, obs);

    }

    public ObservedProperty getHistoricalLocationThingDatastreamObservedProperty(String value) {
        ProviderSnapshot provider = validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(value));
        return DtoMapper
                .toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(), null, provider)
                .get();
    }

    public ResultList<Observation> getHistoricalLocationThingDatastreamObservations(String value) {
        ProviderSnapshot provider = validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(value));

        return RootResourceDelegateSensorthings.getObservationList(getSession(), application, getMapper(), uriInfo,
                requestContext, provider.getResource(DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation"), null);
    }

    public Sensor getHistoricalLocationThingDatastreamSensor(String value) {
        ProviderSnapshot provider = validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(value));
        return DtoMapper.toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(), null, provider)
                .get();
    }

    public Response deleteHistoricalLocation(String value) {
        return getExtraDelegate().delete(getSession(), getMapper(), uriInfo, value, HistoricalLocation.class);
    }

    public Response updateHistoricalLocation(String value, HistoricalLocation hl) {
        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), value, hl);
        ICriterion criterion = parseFilter(EFilterContext.HISTORICAL_LOCATIONS);
        HistoricalLocation createDto = DtoMapper.toHistoricalLocation(getSession(), application, getMapper(), uriInfo,
                getExpansions(), criterion, snapshot, value, getCacheHistoricalLocation().getDto(value));

        return Response.ok().entity(createDto).build();
    }
}
