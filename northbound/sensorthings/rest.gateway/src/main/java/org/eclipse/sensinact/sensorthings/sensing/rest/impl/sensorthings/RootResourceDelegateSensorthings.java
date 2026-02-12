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

import static org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings.EMPTY;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedHistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class RootResourceDelegateSensorthings extends AbstractDelegate {

    public RootResourceDelegateSensorthings(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
        // TODO Auto-generated constructor stub
    }

    public ResultList<Thing> getThings() {
        ICriterion criterion = parseFilter(EFilterContext.THINGS);

        List<ProviderSnapshot> providers = listProviders(criterion);
        ResultList<Thing> result = new ResultList<>(null, null,
                providers.stream().filter(p -> DtoMapperSimple.getThingService(p) != null)
                        .map(p -> getSensorThingDtoMapper().toThing(getSession(), getMapper(), uriInfo, getExpansions(),
                                criterion, p))
                        .toList());

        return result;
    }

    public ResultList<Location> getLocations() {
        ICriterion criterion = parseFilter(EFilterContext.LOCATIONS);
        List<ProviderSnapshot> providers = listProviders(criterion);
        ResultList<Location> result = new ResultList<>(null, null,
                providers.stream().filter(p -> DtoMapperSimple.getLocationService(p) != null)
                        .map(p -> getSensorThingDtoMapper().toLocation(getSession(), getMapper(), uriInfo,
                                getExpansions(), criterion, p))
                        .toList());
        return result;
    }

    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ICriterion criterion = parseFilter(EFilterContext.HISTORICAL_LOCATIONS);

        List<ProviderSnapshot> providers = listProviders(criterion);
        Stream<HistoricalLocation> cacheHl = Stream.empty();
        if (isHistoryMemory()) {
            cacheHl = getCacheHistoricalLocation().keySet().stream()
                    .map(id -> DtoMapper.toHistoricalLocation(getSession(), getMapper(), uriInfo, getExpansions(),
                            criterion, id, getCacheHistoricalLocation().getDto(id)));
        }
        Stream<HistoricalLocation> liveHl = providers
                .stream().filter(p -> DtoMapperSimple.getThingService(p) != null).map(p -> getSensorThingDtoMapper()
                        .toHistoricalLocation(getSession(), getMapper(), uriInfo, getExpansions(), criterion, p))
                .filter(Optional::isPresent).map(Optional::get);
        ResultList<HistoricalLocation> result = new ResultList<>(null, null, Stream.concat(liveHl, cacheHl).toList());
        return result;

    }

    public ResultList<Datastream> getDatastreams() {
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        List<ProviderSnapshot> providers = listProviders(criterion);
        List<ProviderSnapshot> providersDatastreams = providers.stream()
                .filter(p -> DtoMapperSimple.getDatastreamService(p) != null).toList();
        ResultList<Datastream> result = new ResultList<>(null, null,
                providersDatastreams.stream()
                        .map(p -> getSensorThingDtoMapper().toDatastream(getSession(), getMapper(), uriInfo,
                                getExpansions(), criterion, p))
                        .filter(ds -> ds.isPresent()).map(ds -> ds.get()).toList());
        return result;

    }

    public ResultList<Sensor> getSensors() {
        ICriterion criterion = parseFilter(EFilterContext.SENSORS);

        List<ProviderSnapshot> providers = listProviders(criterion);
        List<ProviderSnapshot> providersDatastreams = providers.stream()
                .filter(p -> DtoMapperSimple.getDatastreamService(p) != null).toList();

        ResultList<Sensor> result = new ResultList<>(null, null,
                providersDatastreams.stream()
                        .map(p -> getSensorThingDtoMapper().toSensor(getSession(), getMapper(), uriInfo,
                                getExpansions(), criterion, p))
                        .filter(ds -> ds.isPresent()).map(ds -> ds.get()).toList());
        return result;
    }

    // No history as it is *live* observation data not a data stream

    public ResultList<Observation> getObservations() {
        ICriterion criterion = parseFilter(EFilterContext.OBSERVATIONS);

        List<ProviderSnapshot> providers = listProviders(criterion);
        Stream<? extends Observation> listStoreObservation = providers.stream()
                .map(p -> DtoMapperSimple.getDatastreamService(p)).filter(Objects::nonNull)
                .map(s -> s.getResource("lastObservation"))
                .flatMap(r -> getObservationList(getSession(), getSensorThingDtoMapper(), getMapper(), uriInfo,
                        requestContext, r, criterion, getHistoryProvider(), getMaxResult(25),
                        getCacheObservationIfHistoryMemory()).value().stream());

        ResultList<Observation> result = new ResultList<>(null, null, listStoreObservation.toList());
        return result;
    }

    public ResultList<ObservedProperty> getObservedProperties() {
        ICriterion criterion = parseFilter(EFilterContext.OBSERVED_PROPERTIES);

        List<ProviderSnapshot> providers = listProviders(criterion);
        List<ProviderSnapshot> providersDatastreams = providers.stream()
                .filter(p -> DtoMapperSimple.getDatastreamService(p) != null).toList();

        ResultList<ObservedProperty> result = new ResultList<>(null, null,
                providersDatastreams.stream()
                        .map(r -> getSensorThingDtoMapper().toObservedProperty(getSession(), getMapper(), uriInfo,
                                getExpansions(), criterion, r))
                        .filter(ds -> ds.isPresent()).map(ds -> ds.get()).toList());
        return result;
    }

    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        ICriterion criterion = parseFilter(EFilterContext.FEATURES_OF_INTEREST);

        List<ProviderSnapshot> providers = listProviders(criterion);
        List<ResourceSnapshot> resources = providers.stream().map(p -> DtoMapperSimple.getDatastreamService(p))
                .filter(Objects::nonNull).map(s -> s.getResource("lastObservation")).toList();
        Stream<FeatureOfInterest> foisCache = Stream.empty();
        if (isHistoryMemory()) {
            foisCache = getCacheObservation().keySet().stream().map(obsId -> {

                Instant stamp = DtoMapperSimple.getTimestampFromId(obsId);
                ExpandedObservation expObs = getCacheObservation().getDto(obsId);

                return getSensorThingDtoMapper().toFeatureOfInterest(getSession(), getMapper(), uriInfo,
                        getExpansions(), criterion, stamp, expObs);
            });
        }
        Stream<FeatureOfInterest> fois = resources.stream().map(r -> {
            if (r.getValue() != null) {
                ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(getMapper(), r.getValue().getValue());

                return getSensorThingDtoMapper().toFeatureOfInterest(getSession(), getMapper(), uriInfo,
                        getExpansions(), criterion, r.getValue().getTimestamp(), obs);
            }
            return null;
        }).filter(Objects::nonNull);

        ResultList<FeatureOfInterest> result = new ResultList<>(null, null, Stream.concat(fois, foisCache).toList());
        return result;
    }

    public static ResultList<Observation> getObservationList(SensiNactSession userSession, DtoMapper dtoMapper,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ResourceSnapshot resourceSnapshot, ICriterion filter, String historyProvider, int localResultLimit,
            IDtoMemoryCache<ExpandedObservation> cacheObs) {

        ExpansionSettings es = (ExpansionSettings) requestContext.getProperty(IFilterConstants.EXPAND_SETTINGS_STRING);
        return getObservationList(userSession, dtoMapper, mapper, uriInfo, es == null ? EMPTY : es, resourceSnapshot,
                filter, historyProvider, 0, cacheObs);
    }

    public static ResultList<Observation> getObservationList(SensiNactSession userSession, DtoMapper dtoMapper,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resourceSnapshot,
            ICriterion filter, String historyProvider, int localResultLimit,
            IDtoMemoryCache<ExpandedObservation> cacheObs) {

        ResultList<Observation> list = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                dtoMapper, mapper, uriInfo, expansions, resourceSnapshot, filter, historyProvider, localResultLimit,
                cacheObs);

        if (list.value().isEmpty()) {
            list = new ResultList<Observation>(null, null,
                    dtoMapper.toObservation(userSession, mapper, uriInfo, expansions, filter, resourceSnapshot)
                            .map(List::of).orElse(List.of()));
        }

        return list;
    }

    public Response createDatastream(ExpandedDataStream datastream) {
        ProviderSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), datastream);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        Datastream createDto = getSensorThingDtoMapper()
                .toDatastream(getSession(), getMapper(), uriInfo, getExpansions(), criterion, snapshot).get();
        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();
    }

    public Response createFeaturesOfInterest(FeatureOfInterest featuresOfInterest) {
        FeatureOfInterest createDto = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), featuresOfInterest);

        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();

    }

    public Response createObservation(ExpandedObservation observation) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observation);
        ICriterion criterion = parseFilter(EFilterContext.OBSERVATIONS);
        Optional<Observation> createDto = getSensorThingDtoMapper().toObservation(getSession(), getMapper(), uriInfo,
                EMPTY, criterion, snapshot.getResource("lastObservation"));
        if (createDto.isEmpty()) {
            throw new NotFoundException();
        }
        URI createdUri = getCreatedUri(createDto.get());

        return Response.created(createdUri).entity(createDto.get()).build();

    }

    public Response createHistoricalLocation(ExpandedHistoricalLocation historicalLocation) {
        String thingId = (String) historicalLocation.Thing().id();
        Thing thing = getThing(thingId);
        ExpandedThing ExpThing = new ExpandedThing(null, thingId, thing.name(), null, null, null, null, null, null,
                historicalLocation.locations().stream().map(
                        ref -> new ExpandedLocation(null, ref.id(), null, null, null, null, null, null, null, null))
                        .toList(),
                null);
        updateThing(thingId, ExpThing);

        HistoricalLocation createDto = getHistoricalLocationFromThing(thingId);
        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();
    }

    public Response createLocation(ExpandedLocation location) {
        ProviderSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), location);
        ICriterion criterion = parseFilter(EFilterContext.LOCATIONS);
        ICriterion criterionThing = parseFilter(EFilterContext.THINGS);

        Location createDto = getSensorThingDtoMapper().toLocation(getSession(), getMapper(), uriInfo, getExpansions(),
                criterion, snapshot, criterionThing);

        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();

    }

    public Response createObservedProperties(ObservedProperty observedProperty) {
        ObservedProperty createDto = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observedProperty);

        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();

    }

    public Response createSensors(Sensor sensor) {
        Sensor createDto = getExtraDelegate().create(getSession(), getMapper(), uriInfo, requestContext.getMethod(),
                sensor);

        URI createdUri = getCreatedUri(createDto);

        return Response.created(createdUri).entity(createDto).build();

    }

    public Response createThing(ExpandedThing thing) {

        ProviderSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), thing);
        ICriterion criterion = parseFilter(EFilterContext.THINGS);

        Thing createDto = getSensorThingDtoMapper().toThing(getSession(), getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);

        URI createdUri = getCreatedUri(createDto);
        return Response.created(createdUri).entity(createDto).build();
    }

    public ResultList<Thing> getThingsRef() {
        ICriterion criterion = parseFilter(EFilterContext.THINGS);
        List<ProviderSnapshot> providers = listProviders(criterion);
        ResultList<Thing> result = new ResultList<>(null, null,
                providers.stream().filter(p -> DtoMapperSimple.getThingService(p) != null)
                        .map(p -> getSensorThingDtoMapper().toThing(getSession(), getMapper(), uriInfo, getExpansions(),
                                criterion, p))
                        .toList());
        return result;
    }

}
