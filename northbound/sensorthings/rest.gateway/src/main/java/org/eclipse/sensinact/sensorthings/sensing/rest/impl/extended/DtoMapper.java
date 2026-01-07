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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended;

import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_OBSERVATION_QUALITY;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.RootResourceAccessImpl;
import org.eclipse.sensinact.sensorthings.sensing.rest.snapshot.GenericResourceSnapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

/**
 * new DtoMapper for use for Post
 */
public class DtoMapper {
    private static final String ADMIN = "admin";
    private static final String DESCRIPTION = "description";
    private static final String FRIENDLY_NAME = "name";
    private static final String LOCATION = "location";
    private static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    public static final String VERSION = "v1.1";
    private static final String NO_DESCRIPTION = "No description";

    public static ServiceSnapshot getServiceSnapshot(ProviderSnapshot provider, String name) {
        return provider.getServices().stream().filter(s -> name.equals(s.getName())).findFirst().get();
    }

    public static List<Observation> toObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resourceSnapshot, List<TimedValue<?>> observations) {
        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }

        List<Observation> list = new ArrayList<>(observations.size());
        for (TimedValue<?> tv : observations) {
            toObservation(userSession, application, mapper, uriInfo, expansions, filter, resourceSnapshot,
                    Optional.of(tv)).ifPresent(list::add);
        }

        return list;
    }

    public static ServiceSnapshot validateAndGeService(SensiNactSession session, String id, String serviceName) {
        String providerId = UtilDto.extractFirstIdSegment(id);

        Optional<ProviderSnapshot> provider = DtoMapper.getProviderSnapshot(session, providerId);

        if (provider != null && provider.isPresent() && serviceName != null) {
            return DtoMapper.getServiceSnapshot(provider.get(), serviceName);
        }
        throw new NotFoundException(String.format("can't find model identified by %s", providerId));
    }

    public static ResourceSnapshot validateAndGetResourceSnapshot(SensiNactSession session, String id) {
        String provider = extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = DtoMapper.validateAndGetProvider(session, provider);

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResourceSnapshot resourceSnapshot = providerSnapshot.getResource(service, resource);

        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }
        return resourceSnapshot;
    }

    public static ProviderSnapshot validateAndGetProvider(SensiNactSession session, String id) {
        DtoMapper.validatedProviderId(id);

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(session, id);

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

    public static Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        return Optional.ofNullable(session.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    public static Thing toThing(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        return toThing(userSession, application, mapper, uriInfo, expansions, filter,
                UtilDto.getThingService(provider));
    }

    @SuppressWarnings("unchecked")
    public static Thing toThing(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot serviceThing) {
        String id = serviceThing.getProvider().getName();

        String name = getResourceField(serviceThing, "name", String.class);

        String description = getResourceField(serviceThing, "description", String.class);

        String selfLink = getLink(uriInfo, VERSION, "Things({id})", id);
        String datastreamsLink = getLink(uriInfo, selfLink, "Datastreams");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
        String locationsLink = getLink(uriInfo, selfLink, "Locations");
        List<String> locationIds = getResourceField(serviceThing, "locationIds", List.class);
        Thing thing = new Thing(selfLink, id, name, description, null, datastreamsLink, historicalLocationsLink,
                locationsLink);

        if (expansions.shouldExpand("Datastreams", thing)) {
            List<String> listDatastreamId = getResourceField(serviceThing, "datastreamIds", List.class);
            expansions.addExpansion("Datastreams", thing, toDatastreams(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastreams"), filter, listDatastreamId));
        }

        if (expansions.shouldExpand("HistoricalLocations", thing)) {
            ResultList<HistoricalLocation> list = new ResultList<>(null, null,
                    locationIds.stream().map(idLoc -> validateAndGetProvider(userSession, idLoc))
                            .map(UtilDto::getLocationService).filter(Objects::nonNull)
                            .map(s -> DtoMapper.toHistoricalLocation(userSession, application, mapper, uriInfo,
                                    expansions.getExpansionSettings("HistoricalLocations"), filter, s))
                            .flatMap(Optional::stream).toList());
            expansions.addExpansion("HistoricalLocations", thing, list);

        }
        if (expansions.shouldExpand("Locations", thing)) {
            if (locationIds != null) {

                List<ServiceSnapshot> services = locationIds.stream()
                        .map(idLocation -> getProviderSnapshot(userSession, idLocation)).flatMap(Optional::stream)
                        .map(p -> p.getServices()).flatMap(List::stream).toList();
                List<Location> locations = services.stream().filter(service -> service.getName() != ADMIN)
                        .map(s -> toLocation(userSession, application, mapper, uriInfo, expansions, filter, s))
                        .toList();
                ResultList<Location> list = new ResultList<>(null, null, locations);
                expansions.addExpansion("Locations", thing, list);
            }
        }

        return thing;
    }

    public static Datastream toDatastream(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        ServiceSnapshot service = UtilDto.getDatastreamService(provider);
        return toDatastream(userSession, application, mapper, uriInfo, expansions, filter, service);
    }

    public static Datastream toDatastream(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service) {
        String providerName = service.getProvider().getName();
        String id = String.format("%s", providerName);

        String name = getResourceField(service, FRIENDLY_NAME, String.class);
        String description = getResourceField(service, DESCRIPTION, String.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = getResourceField(service, "properties", Map.class);
        UnitOfMeasurement unit = toUnitOfMeasure(userSession, application, mapper, uriInfo, expansions, filter,
                service);

        Polygon observedArea = null; // TODO

        String selfLink = getLink(uriInfo, VERSION, "Datastreams({id})", id);

        String observationsLink = getLink(uriInfo, selfLink, "Observations");
        String observedPropertyLink = getLink(uriInfo, selfLink, "ObservedProperty");
        String sensorLink = getLink(uriInfo, selfLink, "Sensor");

        String thingLink = getLink(uriInfo, selfLink, "Thing");
        String thingId = getResourceField(service, "thingId", String.class);

        Datastream datastream = new Datastream(selfLink, id, name, description,
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation", unit, observedArea, null, null,
                metadata, observationsLink, observedPropertyLink, sensorLink, thingLink);
        if (expansions.shouldExpand("Observations", datastream)) {
            expansions.addExpansion("Observations", datastream,
                    RootResourceAccessImpl.getObservationList(userSession, application, mapper, uriInfo,
                            expansions.getExpansionSettings("Observations"), service.getResource("lastObservation"),
                            filter, 25));
        }

        if (expansions.shouldExpand("ObservedProperty", datastream)) {
            expansions.addExpansion("ObservedProperty", datastream, toObservedProperty(userSession, application, mapper,
                    uriInfo, expansions.getExpansionSettings("ObservedProperty"), filter, service));
        }

        if (expansions.shouldExpand("Sensor", datastream)) {
            expansions.addExpansion("Sensor", datastream, toSensor(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Sensor"), filter, service));
        }

        if (expansions.shouldExpand("Thing", datastream)) {
            ProviderSnapshot providerThing = validateAndGetProvider(userSession, thingId);
            expansions.addExpansion("Thing", datastream, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, UtilDto.getThingService(providerThing)));
        }

        return datastream;
    }

    public static Sensor toSensor(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        return toSensor(userSession, application, mapper, uriInfo, expansions, filter,
                UtilDto.getDatastreamService(provider));
    }

    public static Sensor toSensor(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service) {
        String sensorId = String.format("%s~%s", service.getProvider().getName(),
                getResourceField(service, "sensorId", String.class));
        String sensorName = getResourceField(service, "sensorName", String.class);
        String sensorDescription = getResourceField(service, "sensorDescription", String.class);
        String sensorEncodingType = getResourceField(service, "sensorEncodingType", String.class);
        Object sensorMetadata = getResourceField(service, "sensorMetadata", Object.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> sensorProperty = getResourceField(service, "sensorProperty", Map.class);

        String sensorLink = getLink(uriInfo, VERSION, "/Sensors({id})", sensorId);
        String datastreamLink = getLink(uriInfo, sensorLink, "Datastreams");
        Sensor sensor = new Sensor(sensorLink, sensorId, sensorName, sensorDescription, sensorEncodingType,
                sensorMetadata, sensorProperty, datastreamLink);

        return sensor;
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

    public static ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        return toObservedProperty(userSession, application, mapper, uriInfo, expansions, filter,
                UtilDto.getDatastreamService(provider));
    }

    public static ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot service) {
        String datastreamId = getResourceField(service, "id", String.class);

        String observedPropertyId = getResourceField(service, "observedPropertyId", String.class);
        String id = String.format("%s~%s", datastreamId, observedPropertyId);
        String observedPropertyName = getResourceField(service, "observedPropertyName", String.class);
        String observedPropertyDescription = getResourceField(service, "observedPropertyDescription", String.class);
        String observedPropertyDefinition = getResourceField(service, "observedPropertyDefinition", String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> observedPropertyProperty = getResourceField(service, "observedPropertyProperties",
                Map.class);

        String observedPropertyLink = getLink(uriInfo, VERSION, "/ObservedProperties({id})", id);
        String datastreamLink = getLink(uriInfo, observedPropertyLink, "Datastreams");

        ObservedProperty observedProperty = new ObservedProperty(observedPropertyLink, id, observedPropertyName,
                observedPropertyDescription, observedPropertyDefinition, observedPropertyProperty, datastreamLink);

        return observedProperty;
    }

    public static Optional<Observation> toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource) {
        return toObservation(userSession, application, mapper, uriInfo, expansions, filter, resource,
                Optional.ofNullable(resource.getValue()));
    }

    public static Optional<Observation> toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource, Optional<TimedValue<?>> t) {
        if (resource == null) {
            throw new NotFoundException();
        }
        ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
        if (rvf != null) {
            ResourceSnapshot rs = new GenericResourceSnapshot(resource, t.orElse(new DefaultTimedValue<>()));
            if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                return Optional.empty();
            }
        }

        final Instant timestamp = t.map(TimedValue::getTimestamp).orElse(null);

        ProviderSnapshot providerSnapshot = resource.getService().getProvider();
        String id = String.format("%s~%s~%s~%s", providerSnapshot.getName(), resource.getService().getName(),
                resource.getName(), Long.toString(timestamp.toEpochMilli(), 16));

        Object result = t.map(TimedValue::getValue).orElse(null);
        Object resultQuality = resource.getMetadata().get(SENSORTHINGS_OBSERVATION_QUALITY);

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})").resolveTemplate("id", id)
                .build().toString();
        String datastreamLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastream").build().toString();
        String featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("FeatureOfInterest").build()
                .toString();

        Observation observation = new Observation(selfLink, id, timestamp, timestamp, result, resultQuality, null, null,
                datastreamLink, featureOfInterestLink);
        if (expansions.shouldExpand("Datastream", observation)) {
            expansions.addExpansion("Datastream", observation, toDatastream(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastream"), filter, resource.getService()));
        }

        if (expansions.shouldExpand("FeatureOfInterest", observation)) {
            expansions.addExpansion("FeatureOfInterest", observation, toFeatureOfInterest(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("FeatureOfInterest"), filter, providerSnapshot));
        }

        return Optional.of(observation);
    }

    private static TimedValue<GeoJsonObject> getLocation(ServiceSnapshot service, ObjectMapper mapper,
            boolean allowNull) {
        final ResourceSnapshot locationResource = service.getResource(LOCATION);

        final Instant time;
        final Object rawValue;
        if (locationResource == null) {
            time = Instant.EPOCH;
            rawValue = null;
        } else {
            final TimedValue<?> timedValue = locationResource.getValue();
            if (timedValue == null) {
                time = Instant.EPOCH;
                rawValue = null;
            } else {
                time = timedValue.getTimestamp() != null ? timedValue.getTimestamp() : Instant.EPOCH;
                rawValue = timedValue.getValue();
            }
        }
        return getLocation(mapper, rawValue, time, allowNull);
    }

    private static TimedValue<GeoJsonObject> getLocation(ObjectMapper mapper, Object rawValue, Instant time,
            boolean allowNull) {

        final GeoJsonObject parsedLocation;
        if (rawValue == null) {
            if (allowNull) {
                parsedLocation = null;
            } else {
                parsedLocation = new Point(Coordinates.EMPTY, null, null);
            }
        } else {
            if (rawValue instanceof GeoJsonObject) {
                parsedLocation = (GeoJsonObject) rawValue;
            } else if (rawValue instanceof String) {
                try {
                    parsedLocation = mapper.readValue((String) rawValue, GeoJsonObject.class);
                } catch (JsonProcessingException ex) {
                    if (allowNull) {
                        return null;
                    }
                    throw new RuntimeException("Invalid resource location content", ex);
                }
            } else {
                parsedLocation = mapper.convertValue(rawValue, GeoJsonObject.class);
            }
        }

        return new DefaultTimedValue<>(parsedLocation, time);
    }

    public static <T> T getResourceField(ServiceSnapshot service, String resourceName, Class<T> expectedType) {

        return UtilDto.getResourceField(service, resourceName, expectedType);
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        return toLocation(userSession, application, mapper, uriInfo, expansions, filter,
                UtilDto.getLocationService(provider), null);
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service) {
        return toLocation(userSession, application, mapper, uriInfo, expansions, filter, service, null);
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ServiceSnapshot service,
            ICriterion filterThing) {
        // check service is container correct type

        final TimedValue<GeoJsonObject> rcLocation = getLocation(service, mapper, false);
        final GeoJsonObject object = rcLocation.getValue();

        String id = getResourceField(service, "id", String.class);

        String name = Objects.requireNonNullElse(getResourceField(service, FRIENDLY_NAME, String.class), "");

        String description = Objects.requireNonNullElse(getResourceField(service, DESCRIPTION, String.class),
                NO_DESCRIPTION);

        String selfLink = getLink(uriInfo, VERSION, "Locations({id})", id);
        String thingsLink = getLink(uriInfo, selfLink, "Things");
        String historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
        Location location = new Location(selfLink, id, name, description, ENCODING_TYPE_VND_GEO_JSON, object,
                thingsLink, historicalLocationsLink);
        if (expansions.shouldExpand("Things", location) && filterThing != null) {
            List<ProviderSnapshot> listProviderThing = userSession.filteredSnapshot(filterThing);

            ResultList<Thing> list = new ResultList<>(null, null,
                    listProviderThing.stream()
                            .map(p -> DtoMapper.toThing(userSession, application, mapper, uriInfo,
                                    expansions.getExpansionSettings("Thing"), filter, UtilDto.getThingService(p)))
                            .toList());
            expansions.addExpansion("Things", location, list);
        }
        if (expansions.shouldExpand("HistoricalLocations", location)) {

            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter, service);
            if (!historicalLocation.isEmpty()) {
                ResultList<HistoricalLocation> list = new ResultList<>(null, null, List.of(historicalLocation.get()));
                expansions.addExpansion("HistoricalLocations", location, list);
            }
        }

        return location;
    }

    public static ResultList<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, List<ProviderSnapshot> providerLocations) {
        return new ResultList<>(null, null,
                providerLocations.stream().map(p -> UtilDto.getLocationService(p)).map(
                        s -> toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, s))
 // TODO                       map(List::add).or.toList());
    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ServiceSnapshot serviceLocation) {
        final TimedValue<GeoJsonObject> location = getLocation(serviceLocation, mapper, true);
        return toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, serviceLocation,
                Optional.of(location));
    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ServiceSnapshot service, Optional<TimedValue<?>> t) {

        ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
        if (rvf != null) {
            ResourceSnapshot locationResource = service.getResource(LOCATION);

            ResourceSnapshot rs = new GenericResourceSnapshot(locationResource, t.get());
            if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                return Optional.empty();
            }
        }

        final Instant time = t.map(TimedValue::getTimestamp).orElse(Instant.EPOCH);

        String id = String.format("%s~%s", service.getProvider().getName(), Long.toString(time.toEpochMilli(), 16));

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();

        HistoricalLocation historicalLocation = new HistoricalLocation(selfLink, id, time, locationsLink, thingLink);
        if (expansions.shouldExpand("Thing", historicalLocation)) {
            expansions.addExpansion("Thing", historicalLocation, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, service));
        }
        if (expansions.shouldExpand("Locations", historicalLocation)) {
            ResultList<Location> list = new ResultList<>(null, null, List.of(DtoMapper.toLocation(userSession,
                    application, mapper, uriInfo, expansions.getExpansionSettings("Locations"), filter, service)));
            expansions.addExpansion("Locations", historicalLocation, list);
        }
        return Optional.of(historicalLocation);
    }

    public static UnitOfMeasurement toUnitOfMeasure(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot service) {
        if (service == null) {
            throw new NotFoundException();
        }

        String unitName = getResourceField(service, "unitName", String.class);
        String unitSymbol = getResourceField(service, "unitSymbol", String.class);
        String unitDefinition = getResourceField(service, "unitDefinition", String.class);

        return new UnitOfMeasurement(unitName, unitSymbol, unitDefinition);
    }

    public static List<Datastream> toDatastreams(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            List<String> datastreamids) {
        if (datastreamids == null) {
            throw new NotFoundException();
        }

        return datastreamids.stream().map(datastreamId -> DtoMapper.getProviderSnapshot(userSession, datastreamId))
                .flatMap(Optional::stream)
                .map(p -> toDatastream(userSession, application, mapper, uriInfo, expansions, filter, p)).toList();

    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        return toFeatureOfInterest(userSession, application, mapper, uriInfo, expansions, filter,
                UtilDto.getDatastreamService(provider));
    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ServiceSnapshot serviceSnapshot) {
        String idDatastream = UtilDto.getResourceField(serviceSnapshot, "id", String.class);

        ExpandedObservation lastObservation = UtilDto.getResourceField(serviceSnapshot, "lastObservation",
                ExpandedObservation.class);
        if (lastObservation != null && lastObservation.featureOfInterest() != null) {
            FeatureOfInterest foiReaded = lastObservation.featureOfInterest();
            String foiId = String.format("%s~%s~%s", idDatastream, lastObservation.id(), foiReaded.id());
            String selfLink = getLink(uriInfo, VERSION, "FeaturesOfInterest({id})", foiId);
            String observationLink = getLink(uriInfo, selfLink, "Observations");
            FeatureOfInterest foi = new FeatureOfInterest(selfLink, foiId, foiReaded.name(), foiReaded.description(),
                    foiReaded.encodingType(), foiReaded.feature(), observationLink);
            return foi;
        }
        return null;
    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            FeatureOfInterest foiReaded, String idObservation) {
        String idFoi = String.format("%s~%s", idObservation, foiReaded.id());
        String selfLink = getLink(uriInfo, VERSION, "FeaturesOfInterest(%s)", idFoi);
        String observationLink = getLink(uriInfo, selfLink, "Observations");

        FeatureOfInterest foi = new FeatureOfInterest(selfLink, idFoi, foiReaded.name(), foiReaded.description(),
                foiReaded.encodingType(), foiReaded.feature(), observationLink);

        return foi;
    }
}
