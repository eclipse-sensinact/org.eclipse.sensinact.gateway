/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.snapshot.GenericResourceSnapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;

/**
 * new DtoMapper for use for Post
 */
public class DtoMapper {
    public static final String VERSION = "v1.1";

    private String historyProvider;
    private int maxResult;
    private final IDtoMemoryCache<ExpandedObservation> cacheObs;
    private final IDtoMemoryCache<Instant> cacheHl;

    public DtoMapper(String historyProvider, int maxResult, IDtoMemoryCache<ExpandedObservation> cacheObs,
            IDtoMemoryCache<Instant> cacheHl) {
        this.historyProvider = historyProvider;
        this.maxResult = maxResult;
        this.cacheObs = cacheObs;
        this.cacheHl = cacheHl;
    }

    public static ServiceSnapshot getServiceSnapshot(ProviderSnapshot provider, String name) {
        return provider.getServices().stream().filter(s -> name.equals(s.getName())).findFirst().get();
    }

    private static String getLink(UriInfo uriInfo, String baseUri, String path) {
        String sensorLink = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).build().toString();
        return sensorLink;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path, String id) {
        if (id == null) {
            id = "null";
        }
        String link = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).resolveTemplate("id", id).build().toString();
        return link;
    }

    public List<Observation> toObservationList(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resourceSnapshot,
            List<TimedValue<?>> observations) {
        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }

        List<Observation> list = new ArrayList<>(observations.size());
        for (TimedValue<?> tv : observations) {
            toObservation(userSession, mapper, uriInfo, expansions, filter, resourceSnapshot, tv).ifPresent(list::add);
        }

        return list;
    }

    public ServiceSnapshot validateAndGeService(SensiNactSession session, String id, String serviceName) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);

        Optional<ProviderSnapshot> provider = UtilDto.getProviderSnapshot(session, providerId);

        if (provider != null && provider.isPresent() && serviceName != null) {
            return DtoMapper.getServiceSnapshot(provider.get(), serviceName);
        }
        throw new NotFoundException(String.format("can't find model identified by %s", providerId));
    }

    public ResourceSnapshot validateAndGetResourceSnapshot(SensiNactSession session, String id) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = DtoMapper.validateAndGetProvider(session, provider);

        String service = DtoMapperSimple.extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = DtoMapperSimple.extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResourceSnapshot resourceSnapshot = providerSnapshot.getResource(service, resource);

        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }
        return resourceSnapshot;
    }

    public static ProviderSnapshot validateAndGetProvider(SensiNactSession session, String id) {
        DtoMapper.validatedProviderId(id);

        Optional<ProviderSnapshot> providerSnapshot = UtilDto.getProviderSnapshot(session, id);

        if (providerSnapshot.isEmpty()) {
            throw new NotFoundException("Unknown provider");
        }
        return providerSnapshot.get();
    }

    public static void validatedProviderId(String id) {
        if (id.contains("~")) {
            throw new BadRequestException("Multi-segments ID found");
        }
    }

    @SuppressWarnings("unchecked")
    public Thing toThing(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        String id = provider.getName();

        String selfLink = getLink(uriInfo, VERSION, "Things({id})", provider.getName());
        String datastreamsLink = getLink(uriInfo, selfLink, "Datastreams");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
        String locationsLink = getLink(uriInfo, selfLink, "Locations");
        Thing thing = DtoMapperSimple.toThing(provider, id, selfLink, datastreamsLink, historicalLocationsLink,
                locationsLink);
        List<String> locationIds = getResourceField(DtoMapperSimple.getThingService(provider), "locationIds",
                List.class);
        if (expansions.shouldExpand("Datastreams", thing)) {
            List<String> listDatastreamId = getResourceField(DtoMapperSimple.getThingService(provider), "datastreamIds",
                    List.class);
            expansions.addExpansion("Datastreams", thing, toDatastreams(userSession, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastreams"), filter, listDatastreamId));
        }

        if (expansions.shouldExpand("HistoricalLocations", thing)) {
            ResultList<HistoricalLocation> historyHls = HistoryResourceHelperSensorthings.loadHistoricalLocations(
                    userSession, this, mapper, uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter,
                    provider, historyProvider, maxResult, cacheHl);
            Stream<HistoricalLocation> cacheHls = Stream.empty();
            if (cacheHl != null) {
                cacheHls = cacheHl.keySet().stream().filter(idHl -> idHl.startsWith(id)).map(idHl -> {
                    return toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, idHl,
                            cacheHl.getDto(idHl), provider);
                }).filter(hl -> hl.isPresent()).map(hl -> hl.get());
            }
            ResultList<HistoricalLocation> list = new ResultList<>(
                    Stream.concat(cacheHls, historyHls.value().stream()).toList());
            expansions.addExpansion("HistoricalLocations", thing, list);

        }
        if (expansions.shouldExpand("Locations", thing)) {
            if (locationIds != null) {

                List<ProviderSnapshot> providers = locationIds.stream()
                        .map(idLocation -> UtilDto.getProviderSnapshot(userSession, idLocation))
                        .flatMap(Optional::stream).toList();
                List<Location> locations = providers.stream()
                        .filter(pLocation -> DtoMapperSimple.getLocationService(pLocation) != null)
                        .map(p -> toLocation(userSession, mapper, uriInfo, expansions.getExpansionSettings("Locations"),
                                filter, p))
                        .toList();
                ResultList<Location> list = new ResultList<>(locations);
                expansions.addExpansion("Locations", thing, list);
            }
        }

        return thing;
    }

    public Datastream toDatastream(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {

        String id = getResourceField(DtoMapperSimple.getDatastreamService(provider), "id", String.class);

        String selfLink = getLink(uriInfo, VERSION, "Datastreams({id})", id);
        String observationsLink = getLink(uriInfo, selfLink, "Observations");
        String observedPropertyLink = getLink(uriInfo, selfLink, "ObservedProperty");
        String sensorLink = getLink(uriInfo, selfLink, "Sensor");

        String thingLink = getLink(uriInfo, selfLink, "Thing");
        // get sensor
        String sensorId = getResourceField(DtoMapperSimple.getDatastreamService(provider), "sensorId", String.class);
        String observedPorpertyId = getResourceField(DtoMapperSimple.getDatastreamService(provider),
                "observedPropertyId", String.class);

        Datastream datastream = DtoMapperSimple.toDatastream(provider, selfLink, observationsLink, observedPropertyLink,
                sensorLink, thingLink);
        String thingId = getResourceField(DtoMapperSimple.getDatastreamService(provider), "thingId", String.class);

        if (expansions.shouldExpand("Observations", datastream)) {
            expansions.addExpansion("Observations", datastream,
                    RootResourceDelegateSensorthings.getObservationList(userSession, this, mapper, uriInfo,
                            expansions.getExpansionSettings("Observations"),
                            DtoMapperSimple.getDatastreamService(provider).getResource("lastObservation"), filter,
                            historyProvider, maxResult, cacheObs));
        }

        if (expansions.shouldExpand("ObservedProperty", datastream)) {

            ObservedProperty op = toObservedProperty(userSession, mapper, uriInfo,
                    expansions.getExpansionSettings("ObservedProperty"), filter,
                    validateAndGetProvider(userSession, observedPorpertyId));
            expansions.addExpansion("ObservedProperty", datastream, op);
        }

        if (expansions.shouldExpand("Sensor", datastream)) {

            Sensor sensor = toSensor(userSession, mapper, uriInfo, expansions.getExpansionSettings("Sensor"), filter,
                    validateAndGetProvider(userSession, sensorId));
            expansions.addExpansion("Sensor", datastream, sensor);
        }

        if (expansions.shouldExpand("Thing", datastream)) {
            ProviderSnapshot providerThing = validateAndGetProvider(userSession, thingId);
            expansions.addExpansion("Thing", datastream, toThing(userSession, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, providerThing));
        }

        return datastream;
    }

    public Sensor toSensor(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {

        String id = provider.getName();
        String sensorLink = null;
        String datastreamLink = null;
        if (uriInfo != null) {
            sensorLink = getLink(uriInfo, VERSION, "/Sensors({id})", id);
            datastreamLink = getLink(uriInfo, sensorLink, "Datastreams");
        }

        Sensor sensor = DtoMapperSimple.toSensor(provider, sensorLink, datastreamLink);
        if (expansions.shouldExpand("Datastreams", sensor)) {
            @SuppressWarnings("unchecked")
            List<String> listDatastreamId = getResourceField(DtoMapperSimple.getSensorService(provider),
                    "datastreamIds", List.class);
            expansions.addExpansion("Datastreams", sensor, toDatastreams(userSession, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastreams"), filter, listDatastreamId));
        }
        return sensor;
    }

    public ObservedProperty toObservedProperty(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {

        String id = provider.getName();

        String observedPropertyLink = null;
        String datastreamLink = null;
        if (uriInfo != null) {
            observedPropertyLink = getLink(uriInfo, VERSION, "/ObservedProperties({id})", id);
            datastreamLink = getLink(uriInfo, observedPropertyLink, "Datastreams");
        }
        ObservedProperty observedProperty = DtoMapperSimple.toObservedProperty(provider, observedPropertyLink,
                datastreamLink);
        if (expansions.shouldExpand("Datastreams", observedProperty)) {
            @SuppressWarnings("unchecked")
            List<String> listDatastreamId = getResourceField(DtoMapperSimple.getObservedPropertyService(provider),
                    "datastreamIds", List.class);
            expansions.addExpansion("Datastreams", observedProperty, toDatastreams(userSession, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastreams"), filter, listDatastreamId));
        }
        return observedProperty;
    }

    public Optional<Observation> toObservation(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resource) {
        if (resource.getValue() == null) {
            return Optional.empty();
        }
        return toObservation(userSession, mapper, uriInfo, expansions, filter, resource, resource.getValue());
    }

    public List<HistoricalLocation> toHistoricalLocationList(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider,
            String locationId, List<TimedValue<?>> historicalLocations) {
        if (provider == null) {
            throw new NotFoundException();
        }

        List<HistoricalLocation> list = new ArrayList<>(historicalLocations.size());
        for (TimedValue<?> tv : historicalLocations) {
            toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, provider, locationId,
                    Optional.of(tv)).ifPresent(list::add);
        }

        return list;
    }

    public Optional<Observation> toObservation(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resource, TimedValue<?> t) {
        if (resource == null) {
            return Optional.empty();
        }
        final Instant timestamp = t.getTimestamp();

        ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
        if (rvf != null) {
            ResourceSnapshot rs = new GenericResourceSnapshot(resource, t);
            if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                return Optional.empty();
            }
        }
        Object val = t.getValue();
        if (val != null && val instanceof String) {
            ExpandedObservation obs = DtoMapperSimple.parseExpandObservation(mapper, val);
            if (obs != null) {
                if (obs.deleted()) {
                    return Optional.empty();
                }
                return toObservation(userSession, mapper, uriInfo, expansions, filter, resource, timestamp, obs);
            }
        }
        return Optional.empty();
    }

    public Optional<Observation> toObservation(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resource, final Instant timestamp,
            ExpandedObservation obs) {
        if (obs.deleted())
            return Optional.empty();
        String id = String.format("%s~%s", obs.id(), DtoMapperSimple.stampToId(timestamp));
        try {
            ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
            if (rvf != null) {
                ResourceSnapshot rs;

                rs = new GenericResourceSnapshot(resource,
                        new DefaultTimedValue<String>(mapper.writeValueAsString(obs), timestamp));
                if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                    return Optional.empty();
                }
            }
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("fail to apply filter on history in memory observation");
        }
        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})").resolveTemplate("id", id)
                .build().toString();
        String datastreamLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastream").build().toString();
        String featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("FeatureOfInterest").build()
                .toString();
        Observation observation = DtoMapperSimple.toObservation(id, selfLink, datastreamLink, featureOfInterestLink,
                obs);

        if (expansions.shouldExpand("Datastream", observation)) {
            expansions.addExpansion("Datastream", observation, toDatastream(userSession, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastream"), filter, resource.getService().getProvider()));
        }

        if (expansions.shouldExpand("FeatureOfInterest", observation)) {
            ExpandedObservation expObs = DtoMapperSimple.parseExpandObservation(mapper, resource.getValue().getValue());
            String foiId = expObs.featureOfInterest().id().toString();

            expansions.addExpansion("FeatureOfInterest", observation,
                    toFeatureOfInterest(userSession, mapper, uriInfo,
                            expansions.getExpansionSettings("FeatureOfInterest"), filter,
                            validateAndGetProvider(userSession, foiId)));
        }
        return Optional.of(observation);
    }

    public static <T> T getResourceField(ServiceSnapshot service, String resourceName, Class<T> expectedType) {

        return DtoMapperSimple.getResourceField(service, resourceName, expectedType);
    }

    public Location toLocation(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        // check service is container correct type

        String selfLink = getLink(uriInfo, VERSION, "Locations({id})", provider.getName());
        String thingsLink = getLink(uriInfo, selfLink, "Things");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");

        Location location = DtoMapperSimple.toLocation(mapper, provider, selfLink, thingsLink, historicalLocationsLink);
        if (expansions.shouldExpand("Things", location)) {
            List<ProviderSnapshot> listProviderThing = userSession.filteredSnapshot(null).stream()
                    .filter(p -> DtoMapperSimple.getThingService(p) != null)
                    .filter(p -> DtoMapperSimple
                            .getResourceField(DtoMapperSimple.getThingService(p), "locationIds", List.class)
                            .contains(provider.getName()))
                    .toList();

            ResultList<Thing> list = new ResultList<>(listProviderThing.stream().map(
                    p -> toThing(userSession, mapper, uriInfo, expansions.getExpansionSettings("Things"), filter, p))
                    .toList());
            expansions.addExpansion("Things", location, list);
        }
        if (expansions.shouldExpand("HistoricalLocations", location)) {
            Stream<ProviderSnapshot> providerThings = userSession.filteredSnapshot(filter).stream()
                    .filter(p -> DtoMapperSimple.getThingService(p) != null)
                    .filter(p -> DtoMapperSimple
                            .getResourceField(DtoMapperSimple.getThingService(p), "locationIds", List.class)
                            .contains(provider.getName()));

            List<HistoricalLocation> hls = providerThings
                    .map(p -> toHistoricalLocation(userSession, mapper, uriInfo,
                            expansions.getExpansionSettings("HistoricalLocations"), filter, p))
                    .filter(Optional::isPresent).map(hl -> hl.get()).toList();
            ResultList<HistoricalLocation> list = new ResultList<>(hls);
            expansions.addExpansion("HistoricalLocations", location, list);

        }

        return location;
    }

    public ResultList<HistoricalLocation> toHistoricalLocations(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, List<ProviderSnapshot> providerThings,
            String locationId) {
        List<HistoricalLocation> listHl = providerThings.stream()
                .map(p -> toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, p, locationId))
                .filter(Optional::isPresent).map(hl -> hl.get()).toList();
        return new ResultList<>(listHl);

    }

    public ResultList<HistoricalLocation> toHistoricalLocations(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot providerThing) {
        final TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(providerThing, mapper, true);

        Optional<HistoricalLocation> optHl = toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter,
                providerThing, Optional.of(location));
        return new ResultList<>(optHl.isEmpty() ? List.of() : List.of(optHl.get()));

    }

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        final TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(provider, mapper, true);
        return toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, provider, null,
                Optional.of(location));
    }

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider,
            Optional<TimedValue<?>> t) {
        return toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, provider, null, t);
    }

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider,
            String locationId) {
        final TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(provider, mapper, true);

        return toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, provider, locationId,
                Optional.of(location));
    }

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, String id, Instant timestamp,
            ProviderSnapshot provider) {

        ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
        if (rvf != null) {
            ResourceSnapshot rs = new GenericResourceSnapshot(
                    provider.getResource(DtoMapperSimple.SERVICE_ADMIN, "location"),
                    new DefaultTimedValue<String>(id, timestamp));
            if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                return Optional.empty();
            }
        }
        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();
        HistoricalLocation historicalLocation = new HistoricalLocation(selfLink, id, timestamp, locationsLink,
                thingLink);
        if (expansions.shouldExpand("Thing", historicalLocation)) {
            expansions.addExpansion("Thing", historicalLocation,
                    toThing(userSession, mapper, uriInfo, expansions.getExpansionSettings("Thing"), filter, provider));
        }
        if (expansions.shouldExpand("Locations", historicalLocation)) {
            @SuppressWarnings("unchecked")
            List<String> locationIds = getResourceField(DtoMapperSimple.getThingService(provider), "locationIds",
                    List.class);
            if (locationIds != null) {
                List<ProviderSnapshot> providers = locationIds.stream()
                        .map(idLocation -> UtilDto.getProviderSnapshot(userSession, idLocation))
                        .flatMap(Optional::stream).toList();
                List<Location> locations = providers.stream()
                        .filter(pLocation -> DtoMapperSimple.getLocationService(pLocation) != null)
                        .map(p -> toLocation(userSession, mapper, uriInfo, expansions, filter, p)).toList();
                ResultList<Location> list = new ResultList<>(locations);
                expansions.addExpansion("Locations", historicalLocation, list);
            }
        }
        return Optional.of(historicalLocation);
    }

    public Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider,
            String locationId, Optional<TimedValue<?>> t) {
        if (locationId != null) {
            ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(provider);
            if (!DtoMapperSimple.getResourceField(serviceThing, "locationIds", List.class).contains(locationId)) {
                return Optional.empty();
            }
        }
        ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
        if (rvf != null) {
            ResourceSnapshot rs = provider.getResource(DtoMapperSimple.SERVICE_ADMIN, "location");
            if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                return Optional.empty();
            }
        }
        final Instant time = t.map(TimedValue::getTimestamp).orElse(Instant.EPOCH);
        final Object location = t.map(TimedValue::getValue).orElse(null);
        if (location == null) {
            return Optional.empty();
        }
        String id = String.format("%s~%s", provider.getName(), DtoMapperSimple.stampToId(time));

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();

        HistoricalLocation historicalLocation = DtoMapperSimple.toHistoricalLocation(provider, t, selfLink,
                locationsLink, thingLink);
        if (expansions.shouldExpand("Thing", historicalLocation)) {
            expansions.addExpansion("Thing", historicalLocation,
                    toThing(userSession, mapper, uriInfo, expansions.getExpansionSettings("Thing"), filter, provider));
        }
        if (expansions.shouldExpand("Locations", historicalLocation)) {
            @SuppressWarnings("unchecked")
            List<String> locationIds = getResourceField(DtoMapperSimple.getThingService(provider), "locationIds",
                    List.class);
            if (locationIds != null) {
                List<ProviderSnapshot> providers = locationIds.stream()
                        .map(idLocation -> UtilDto.getProviderSnapshot(userSession, idLocation))
                        .flatMap(Optional::stream).toList();
                List<Location> locations = providers.stream()
                        .filter(pLocation -> DtoMapperSimple.getLocationService(pLocation) != null)
                        .map(p -> toLocation(userSession, mapper, uriInfo, expansions.getExpansionSettings("Locations"),
                                filter, p))
                        .toList();
                ResultList<Location> list = new ResultList<>(locations);
                expansions.addExpansion("Locations", historicalLocation, list);
            }
        }
        return Optional.of(historicalLocation);
    }

    public HistoricalLocation toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider, String id, Instant time) {

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();

        HistoricalLocation historicalLocation = DtoMapperSimple.toHistoricalLocation(id, time, selfLink, locationsLink,
                thingLink);
        if (expansions.shouldExpand("Thing", historicalLocation)) {
            expansions.addExpansion("Thing", historicalLocation,
                    toThing(userSession, mapper, uriInfo, expansions.getExpansionSettings("Thing"), filter, provider));
        }
        if (expansions.shouldExpand("Locations", historicalLocation)) {
            // get locations providers
            ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(provider);
            @SuppressWarnings("unchecked")
            List<String> locationIds = DtoMapperSimple.getResourceField(serviceThing, "locationIds", List.class);
            List<ProviderSnapshot> providerLocations = locationIds.stream()
                    .map(idloc -> userSession.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)))
                    .filter(Objects::nonNull).toList();
            ResultList<Location> list = new ResultList<>(providerLocations.stream().map(pLoc -> toLocation(userSession,
                    mapper, uriInfo, expansions.getExpansionSettings("Locations"), filter, pLoc)).toList());
            expansions.addExpansion("Locations", historicalLocation, list);
        }
        return historicalLocation;
    }

    public List<Datastream> toDatastreams(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, List<String> datastreamids) {
        if (datastreamids == null) {
            throw new NotFoundException();
        }

        return datastreamids.stream().map(datastreamId -> UtilDto.getProviderSnapshot(userSession, datastreamId))
                .flatMap(Optional::stream).map(p -> toDatastream(userSession, mapper, uriInfo, expansions, filter, p))
                .toList();

    }

    public FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {

        String selfLink = getLink(uriInfo, VERSION, "FeaturesOfInterest({id})", provider.getName());
        String observationLink = getLink(uriInfo, selfLink, "Observations");
        FeatureOfInterest foi = DtoMapperSimple.toFeatureOfInterest(provider, provider.getName(), selfLink,
                observationLink);

        if (expansions.shouldExpand("Observations", foi)) {
            @SuppressWarnings("unchecked")
            List<String> datastreamId = DtoMapperSimple.getResourceField(
                    DtoMapperSimple.getFeatureofInterestService(provider), "datastreamIds", List.class);
            List<Observation> listObs = datastreamId.stream()
                    .map(id -> userSession.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)))
                    .map(p -> RootResourceDelegateSensorthings.getObservationList(userSession, this, mapper, uriInfo,
                            expansions.getExpansionSettings("Observations"),
                            DtoMapperSimple.getDatastreamService(p).getResource("lastObservation"), filter,
                            historyProvider, maxResult, cacheObs))
                    .flatMap(rs -> rs.value().stream()).map(obs -> (Observation) obs).toList();
            expansions.addExpansion("Observations", foi, new ResultList<Observation>(listObs));
        }

        return foi;
    }

}
