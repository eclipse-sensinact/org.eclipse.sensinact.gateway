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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.snapshot.GenericResourceSnapshot;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

/**
 * new DtoMapper for use for Post
 */
public class DtoMapper {
    public static final String VERSION = "v1.1";

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

    public static List<Observation> toObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resourceSnapshot, List<TimedValue<?>> observations) {
        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }

        List<Observation> list = new ArrayList<>(observations.size());
        for (TimedValue<?> tv : observations) {
            toObservation(userSession, application, mapper, uriInfo, expansions, filter, resourceSnapshot, tv)
                    .ifPresent(list::add);
        }

        return list;
    }

    public static ServiceSnapshot validateAndGeService(SensiNactSession session, String id, String serviceName) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id);

        Optional<ProviderSnapshot> provider = UtilDto.getProviderSnapshot(session, providerId);

        if (provider != null && provider.isPresent() && serviceName != null) {
            return DtoMapper.getServiceSnapshot(provider.get(), serviceName);
        }
        throw new NotFoundException(String.format("can't find model identified by %s", providerId));
    }

    public static ResourceSnapshot validateAndGetResourceSnapshot(SensiNactSession session, String id) {
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
    public static Thing toThing(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
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
            expansions.addExpansion("Datastreams", thing, toDatastreams(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastreams"), filter, listDatastreamId));
        }

        if (expansions.shouldExpand("HistoricalLocations", thing)) {
            ResultList<HistoricalLocation> list = new ResultList<>(null, null,
                    locationIds.stream().map(idLoc -> validateAndGetProvider(userSession, idLoc))
                            .filter(Objects::nonNull)
                            .map(p -> DtoMapper.toHistoricalLocation(userSession, application, mapper, uriInfo,
                                    expansions.getExpansionSettings("HistoricalLocations"), filter, p))
                            .flatMap(Optional::stream).toList());
            expansions.addExpansion("HistoricalLocations", thing, list);

        }
        if (expansions.shouldExpand("Locations", thing)) {
            if (locationIds != null) {

                List<ProviderSnapshot> providers = locationIds.stream()
                        .map(idLocation -> UtilDto.getProviderSnapshot(userSession, idLocation))
                        .flatMap(Optional::stream).toList();
                List<Location> locations = providers.stream()
                        .filter(pLocation -> DtoMapperSimple.getLocationService(pLocation) != null)
                        .map(p -> toLocation(userSession, application, mapper, uriInfo, expansions, filter, p))
                        .toList();
                ResultList<Location> list = new ResultList<>(null, null, locations);
                expansions.addExpansion("Locations", thing, list);
            }
        }

        return thing;
    }

    public static Datastream toDatastream(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        String id = provider.getName();

        String selfLink = getLink(uriInfo, VERSION, "Datastreams({id})", id);
        String observationsLink = getLink(uriInfo, selfLink, "Observations");
        String observedPropertyLink = getLink(uriInfo, selfLink, "ObservedProperty");
        String sensorLink = getLink(uriInfo, selfLink, "Sensor");

        String thingLink = getLink(uriInfo, selfLink, "Thing");
        Datastream datastream = DtoMapperSimple.toDatastream(provider, selfLink, observationsLink, observedPropertyLink,
                sensorLink, thingLink);
        String thingId = getResourceField(DtoMapperSimple.getDatastreamService(provider), "thingId", String.class);

        if (expansions.shouldExpand("Observations", datastream)) {
            expansions.addExpansion("Observations", datastream,
                    RootResourceDelegateSensorthings.getObservationList(userSession, application, mapper, uriInfo,
                            expansions.getExpansionSettings("Observations"),
                            DtoMapperSimple.getDatastreamService(provider).getResource("lastObservation"), filter, 25));
        }

        if (expansions.shouldExpand("ObservedProperty", datastream)) {
            expansions.addExpansion("ObservedProperty", datastream, toObservedProperty(userSession, application, mapper,
                    uriInfo, expansions.getExpansionSettings("ObservedProperty"), filter, provider));
        }

        if (expansions.shouldExpand("Sensor", datastream)) {
            expansions.addExpansion("Sensor", datastream, toSensor(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Sensor"), filter, provider));
        }

        if (expansions.shouldExpand("Thing", datastream)) {
            ProviderSnapshot providerThing = validateAndGetProvider(userSession, thingId);
            expansions.addExpansion("Thing", datastream, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, providerThing));
        }

        return datastream;
    }

    public static Sensor toSensor(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(provider);

        String sensorId = String.format("%s~%s", provider.getName(),

                getResourceField(service, "sensorId", String.class));
        String sensorLink = null;
        String datastreamLink = null;
        if (uriInfo != null) {
            sensorLink = getLink(uriInfo, VERSION, "/Sensors({id})", sensorId);
            datastreamLink = getLink(uriInfo, sensorLink, "Datastreams");
        }
        return DtoMapperSimple.toSensor(provider, sensorLink, datastreamLink);

    }

    public static ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        String datastreamId = provider.getName();
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(provider);

        String observedPropertyId = getResourceField(service, "observedPropertyId", String.class);

        String id = String.format("%s~%s", datastreamId, observedPropertyId);

        String observedPropertyLink = null;
        String datastreamLink = null;
        if (uriInfo != null) {
            observedPropertyLink = getLink(uriInfo, VERSION, "/ObservedProperties({id})", id);
            datastreamLink = getLink(uriInfo, observedPropertyLink, "Datastreams");
        }
        ObservedProperty observedProperty = DtoMapperSimple.toObservedProperty(provider, observedPropertyLink,
                datastreamLink);

        return observedProperty;
    }

    public static Optional<Observation> toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource) {
        return toObservation(userSession, application, mapper, uriInfo, expansions, filter, resource,
                resource.getValue());
    }

    public static List<HistoricalLocation> toHistoricalLocationList(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, String locationId, List<TimedValue<?>> historicalLocations) {
        if (provider == null) {
            throw new NotFoundException();
        }

        List<HistoricalLocation> list = new ArrayList<>(historicalLocations.size());
        for (TimedValue<?> tv : historicalLocations) {
            toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, provider, locationId,
                    Optional.of(tv)).ifPresent(list::add);
        }

        return list;
    }

    public static Optional<Observation> toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource, TimedValue<?> t) {
        if (resource == null) {
            throw new NotFoundException();
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
                String id = String.format("%s~%s", obs.id(), Long.toString(timestamp.toEpochMilli(), 16));

                String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})")
                        .resolveTemplate("id", id).build().toString();
                String datastreamLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastream").build().toString();
                String featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("FeatureOfInterest")
                        .build().toString();
                Observation observation = DtoMapperSimple.toObservation(mapper, id, t, selfLink, datastreamLink,
                        featureOfInterestLink);

                if (expansions.shouldExpand("Datastream", observation)) {
                    expansions.addExpansion("Datastream", observation,
                            toDatastream(userSession, application, mapper, uriInfo,
                                    expansions.getExpansionSettings("Datastream"), filter,
                                    resource.getService().getProvider()));
                }

                if (expansions.shouldExpand("FeatureOfInterest", observation)) {
                    expansions.addExpansion("FeatureOfInterest", observation,
                            toFeatureOfInterest(userSession, application, mapper, uriInfo,
                                    expansions.getExpansionSettings("FeatureOfInterest"), filter,
                                    resource.getService().getProvider()));
                }
                return Optional.of(observation);
            }
        }
        return Optional.empty();
    }

    public static <T> T getResourceField(ServiceSnapshot service, String resourceName, Class<T> expectedType) {

        return DtoMapperSimple.getResourceField(service, resourceName, expectedType);
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        return toLocation(userSession, application, mapper, uriInfo, expansions, filter, provider, null);
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider,
            ICriterion filterThing) {
        // check service is container correct type

        String selfLink = getLink(uriInfo, VERSION, "Locations({id})", provider.getName());
        String thingsLink = getLink(uriInfo, selfLink, "Things");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");

        Location location = DtoMapperSimple.toLocation(mapper, provider, selfLink, thingsLink, historicalLocationsLink);
        if (expansions.shouldExpand("Things", location) && filterThing != null) {
            List<ProviderSnapshot> listProviderThing = userSession.filteredSnapshot(filterThing);

            ResultList<Thing> list = new ResultList<>(null, null,
                    listProviderThing.stream().map(p -> DtoMapper.toThing(userSession, application, mapper, uriInfo,
                            expansions.getExpansionSettings("Thing"), filter, p)).toList());
            expansions.addExpansion("Things", location, list);
        }
        if (expansions.shouldExpand("HistoricalLocations", location)) {

            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter, provider);
            if (!historicalLocation.isEmpty()) {
                ResultList<HistoricalLocation> list = new ResultList<>(null, null, List.of(historicalLocation.get()));
                expansions.addExpansion("HistoricalLocations", location, list);
            }
        }

        return location;
    }

    public static Instant getTimestampFromId(String id) {
        int idx = id.lastIndexOf('~');
        if (idx < 0 || idx == id.length() - 1) {
            throw new BadRequestException("Invalid id");
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(id.substring(idx + 1), 16));
        } catch (Exception e) {
            throw new BadRequestException("Invalid id");
        }
    }

    public static ResultList<HistoricalLocation> toHistoricalLocations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, List<ProviderSnapshot> providerThings, String locationId) {
        List<HistoricalLocation> listHl = providerThings.stream().map(
                p -> toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, p, locationId))
                .filter(Optional::isPresent).map(hl -> hl.get()).toList();
        return new ResultList<>(null, null, listHl);

    }

    public static ResultList<HistoricalLocation> toHistoricalLocations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot providerThing) {
        final TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(providerThing, mapper, true);

        Optional<HistoricalLocation> optHl = toHistoricalLocation(userSession, application, mapper, uriInfo, expansions,
                filter, providerThing, Optional.of(location));
        return new ResultList<>(null, null, optHl.isEmpty() ? List.of() : List.of(optHl.get()));

    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider) {
        final TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(provider, mapper, true);
        return toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, provider, null,
                Optional.of(location));
    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, Optional<TimedValue<?>> t) {
        return toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, provider, null, t);
    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, String locationId) {
        final TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(provider, mapper, true);

        return toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, provider, locationId,
                Optional.of(location));
    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, String locationId, Optional<TimedValue<?>> t) {
        if (locationId != null) {
            ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(provider);
            if (!DtoMapperSimple.getResourceField(serviceThing, "locationIds", List.class).contains(locationId)) {
                return Optional.empty();
            }
        }
        final Instant time = t.map(TimedValue::getTimestamp).orElse(Instant.EPOCH);
        String id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();

        HistoricalLocation historicalLocation = DtoMapperSimple.toHistoricalLocation(provider, t, selfLink,
                locationsLink, thingLink);
        if (expansions.shouldExpand("Thing", historicalLocation)) {
            expansions.addExpansion("Thing", historicalLocation, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, provider));
        }
        if (expansions.shouldExpand("Locations", historicalLocation)) {
            ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(userSession,
                    application, mapper, uriInfo, expansions.getExpansionSettings("Locations"), filter, provider)));
            expansions.addExpansion("Locations", historicalLocation, list);
        }
        return Optional.of(historicalLocation);
    }

    public static List<Datastream> toDatastreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            List<String> datastreamids) {
        if (datastreamids == null) {
            throw new NotFoundException();
        }

        return datastreamids.stream().map(datastreamId -> UtilDto.getProviderSnapshot(userSession, datastreamId))
                .flatMap(Optional::stream)
                .map(p -> toDatastream(userSession, application, mapper, uriInfo, expansions, filter, p)).toList();

    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        ServiceSnapshot serviceSnapshot = DtoMapperSimple.getDatastreamService(provider);

        ExpandedObservation lastObservation = DtoMapperSimple.getObservationFromService(mapper, serviceSnapshot);
        if (lastObservation != null && lastObservation.featureOfInterest() != null) {
            String foiId = String.format("%s~%s~%s", provider.getName(), lastObservation.id(),
                    lastObservation.featureOfInterest().id());

            String selfLink = getLink(uriInfo, VERSION, "FeaturesOfInterest({id})", foiId);
            String observationLink = getLink(uriInfo, selfLink, "Observations");
            return DtoMapperSimple.toFeatureOfInterest(provider, lastObservation, foiId, selfLink, observationLink);
        }
        return null;
    }

}
