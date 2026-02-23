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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_OBSERVATION_QUALITY;
import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_OBSERVEDAREA;
import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_OBSERVEDPROPERTY_DEFINITION;
import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_SENSOR_ENCODING_TYPE;
import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_SENSOR_METADATA;
import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_UNIT_DEFINITION;
import static org.eclipse.sensinact.sensorthings.sensing.dto.SensorthingsAnnotations.SENSORTHINGS_UNIT_NAME;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.FeaturesOfInterestDeletegateSensinact.getLiveObservations;

import java.time.Instant;
import java.util.ArrayList;
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
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
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
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.snapshot.GenericResourceSnapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

public class DtoMapper {

    private static final String ADMIN = "admin";
    private static final String DESCRIPTION = "description";
    private static final String FRIENDLY_NAME = "friendlyName";
    private static final String LOCATION = "location";
    private static final String DEFAULT_ENCODING_TYPE = "text/plain";
    private static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    private static final String VERSION = "v1.1";

    private static final String NO_DESCRIPTION = "No description";
    private static final String NO_DEFINITION = "No definition";

    private static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider,
            String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> ADMIN.equals(s.getName())).findFirst()
                .get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    private static Optional<Object> getProviderAdminFieldValue(ProviderSnapshot provider, String resource) {
        Optional<? extends ResourceSnapshot> rc = getProviderAdminField(provider, resource);
        if (rc.isPresent()) {
            TimedValue<?> value = rc.get().getValue();
            if (value != null) {
                return Optional.ofNullable(value.getValue());
            }
        }
        return Optional.empty();
    }

    private static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    public static Thing toThing(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        String id = provider.getName();

        String name = Objects.requireNonNullElse(toString(getProviderAdminFieldValue(provider, FRIENDLY_NAME)),
                provider.getName());

        String description = Objects.requireNonNullElse(toString(getProviderAdminFieldValue(provider, DESCRIPTION)),
                NO_DESCRIPTION);

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Things({id})").resolveTemplate("id", id)
                .build().toString();
        String datastreamsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastreams").build().toString();
        String historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("HistoricalLocations").build()
                .toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();

        Thing thing = new Thing(selfLink, id, name, description, null, datastreamsLink, historicalLocationsLink,
                locationsLink);
        if (expansions.shouldExpand("Datastreams", thing)) {
            expansions.addExpansion("Datastreams", thing, DatastreamsDelegateSensinact.getDataStreams(userSession,
                    application, mapper, uriInfo, expansions.getExpansionSettings("Datastreams"), filter, provider));
        }

        if (expansions.shouldExpand("HistoricalLocations", thing)) {
            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter, provider);
            if (historicalLocation.isPresent()) {
                ResultList<HistoricalLocation> list = new ResultList<>(null, null, List.of(historicalLocation.get()));
                expansions.addExpansion("HistoricalLocations", thing, list);
            }
        }
        if (expansions.shouldExpand("Locations", thing)) {
            ResultList<Location> list = new ResultList<>(null, null, List.of(toLocation(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Locations"), filter, provider)));
            expansions.addExpansion("Locations", thing, list);
        }

        return thing;
    }

    public static Location toLocation(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider) {
        final String providerName = provider.getName();
        final TimedValue<GeoJsonObject> rcLocation = getLocation(provider, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        String id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String name = Objects.requireNonNullElse(getProperty(object, "name"), providerName);

        String description = Objects.requireNonNullElse(getProperty(object, DESCRIPTION), NO_DESCRIPTION);

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Locations({id})").resolveTemplate("id", id)
                .build().toString();
        String thingsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Things").build().toString();
        String historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("HistoricalLocations").build()
                .toString();

        Location location = new Location(selfLink, id, name, description, ENCODING_TYPE_VND_GEO_JSON, object, null,
                thingsLink, historicalLocationsLink);
        if (expansions.shouldExpand("Things", location)) {
            ResultList<Thing> list = new ResultList<>(null, null, List.of(DtoMapper.toThing(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Thing"), filter, provider)));
            expansions.addExpansion("Things", location, list);
        }
        if (expansions.shouldExpand("HistoricalLocations", location)) {
            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("HistoricalLocations"), filter, provider);
            if (historicalLocation.isPresent()) {
                ResultList<HistoricalLocation> list = new ResultList<>(null, null, List.of(historicalLocation.get()));
                expansions.addExpansion("HistoricalLocations", location, list);
            }
        }

        return location;
    }

    public static List<HistoricalLocation> toHistoricalLocationList(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, List<TimedValue<?>> historicalLocations) {
        if (provider == null) {
            throw new NotFoundException();
        }

        List<HistoricalLocation> list = new ArrayList<>(historicalLocations.size());
        for (TimedValue<?> tv : historicalLocations) {
            toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, provider,
                    Optional.of(tv)).ifPresent(list::add);
        }

        return list;
    }

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, Optional<TimedValue<?>> t) {
        if (provider == null) {
            throw new NotFoundException();
        }

        ResourceValueFilter rvf = filter == null ? null : filter.getResourceValueFilter();
        if (rvf != null) {
            Optional<? extends ResourceSnapshot> locationResource = getProviderAdminField(provider, LOCATION);
            if (locationResource.isEmpty() || t.isEmpty()) {
                return Optional.empty();
            }
            ResourceSnapshot rs = new GenericResourceSnapshot(locationResource.get(), t.get());
            if (!rvf.test(rs.getService().getProvider(), List.of(rs))) {
                return Optional.empty();
            }
        }

        final Instant time = t.map(TimedValue::getTimestamp).orElse(Instant.EPOCH);

        String id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
        String locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();

        HistoricalLocation historicalLocation = new HistoricalLocation(selfLink, id, time, locationsLink, thingLink);
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

    public static Optional<HistoricalLocation> toHistoricalLocation(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider) {
        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, true);
        return toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, filter, provider,
                Optional.of(location));
    }

    public static Sensor toSensor(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter, ResourceSnapshot resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        ProviderSnapshot providerSnapshot = resource.getService().getProvider();
        final String provider = providerSnapshot.getName();
        final Map<String, Object> metadata = resource.getMetadata();

        String id = String.format("%s~%s~%s", provider, resource.getService().getName(), resource.getName());

        String name = toString(metadata.getOrDefault(FRIENDLY_NAME, resource.getName()));
        String description = toString(metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));

        Object sensorMetadata = metadata.getOrDefault(SENSORTHINGS_SENSOR_METADATA, "No metadata");
        String encodingType = toString(metadata.getOrDefault(SENSORTHINGS_SENSOR_ENCODING_TYPE, DEFAULT_ENCODING_TYPE));

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Sensors({id})").resolveTemplate("id", id)
                .build().toString();
        String datastreamsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastreams").build().toString();

        Sensor sensor = new Sensor(selfLink, id, name, description, encodingType, sensorMetadata, metadata,
                datastreamsLink);
        if (expansions.shouldExpand("Datastreams", sensor)) {
            expansions.addExpansion("Datastreams", sensor,
                    DatastreamsDelegateSensinact.getDataStreams(userSession, application, mapper, uriInfo,
                            expansions.getExpansionSettings("Datastreams"), filter, providerSnapshot));
        }

        return sensor;
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
                    expansions.getExpansionSettings("Datastream"), resource, filter));
        }

        if (expansions.shouldExpand("FeatureOfInterest", observation)) {
            expansions.addExpansion("FeatureOfInterest", observation, toFeatureOfInterest(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("FeatureOfInterest"), filter, providerSnapshot));
        }

        return Optional.of(observation);
    }

    public static ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ResourceSnapshot resource) {
        final Map<String, Object> metadata = resource.getMetadata();

        ProviderSnapshot providerSnapshot = resource.getService().getProvider();
        String id = String.format("%s~%s~%s", providerSnapshot.getName(), resource.getService().getName(),
                resource.getName());

        String name = toString(metadata.getOrDefault(FRIENDLY_NAME, resource.getName()));
        String description = toString(metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));

        String definition = toString(metadata.getOrDefault(SENSORTHINGS_OBSERVEDPROPERTY_DEFINITION, NO_DEFINITION));

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("ObservedProperties({id})")
                .resolveTemplate("id", id).build().toString();
        String datastreamsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastreams").build().toString();

        ObservedProperty observedProperty = new ObservedProperty(selfLink, id, name, description, definition, metadata,
                datastreamsLink);
        if (expansions.shouldExpand("Datastreams", observedProperty)) {
            expansions.addExpansion("Datastreams", observedProperty,
                    DatastreamsDelegateSensinact.getDataStreams(userSession, application, mapper, uriInfo,
                            expansions.getExpansionSettings("Datastreams"), filter, providerSnapshot));
        }

        return observedProperty;
    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider) {
        final String providerName = provider.getName();

        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, false);
        final GeoJsonObject object = location.getValue();

        String id = providerName;

        String name = Objects.requireNonNullElse(getProperty(object, "name"), providerName);

        String description = Objects.requireNonNullElse(getProperty(object, DESCRIPTION), NO_DESCRIPTION);

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("FeaturesOfInterest({id})")
                .resolveTemplate("id", id).build().toString();
        String observationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Observations").build().toString();

        FeatureOfInterest featureOfInterest = new FeatureOfInterest(selfLink, id, name, description,
                ENCODING_TYPE_VND_GEO_JSON, object, null, observationsLink);
        if (expansions.shouldExpand("Observations", featureOfInterest)) {
            expansions.addExpansion("Observations", featureOfInterest, getLiveObservations(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Observations"), filter, provider));
        }

        return featureOfInterest;
    }

    public static String extractFirstIdSegment(String id) {
        if (id.isEmpty()) {
            throw new BadRequestException("Invalid id");
        }

        int idx = id.indexOf('~');
        if (idx == -1) {
            // No segment found, return the whole ID
            return id;
        } else if (idx == 0 || idx == id.length() - 1) {
            throw new BadRequestException("Invalid id");
        }
        return id.substring(0, idx);
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

    /**
     * Ensure the given ID contains a single segment
     */
    public static void validatedProviderId(String id) {
        if (id.contains("~")) {
            throw new BadRequestException("Multi-segments ID found");
        }
    }

    private static Polygon getObservedArea(GeoJsonObject location) {
        Geometry geometry = null;
        if (location instanceof Feature) {
            geometry = ((Feature) location).geometry();
        } else if (location instanceof FeatureCollection) {
            // TODO is there a better mapping?
            geometry = ((FeatureCollection) location).features().stream().map((f) -> f.geometry())
                    .filter(Polygon.class::isInstance).map(Polygon.class::cast).findFirst().orElse(null);
        }
        return geometry instanceof Polygon ? (Polygon) geometry : null;
    }

    private static String getProperty(GeoJsonObject location, String propName) {
        if (location instanceof Feature) {
            Feature f = (Feature) location;
            return toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName)));
        } else if (location instanceof FeatureCollection) {
            FeatureCollection fc = (FeatureCollection) location;
            return fc.features().stream()
                    .map(f -> toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName))))
                    .filter(p -> p != null).findFirst().orElse(null);
        }
        return null;
    }

    private static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            ResourceSnapshot resource, boolean allowNull) {
        Optional<? extends ResourceSnapshot> optRS = resource.getService().getResources().stream()
                .filter(r -> r.getMetadata().keySet().contains(SENSORTHINGS_OBSERVEDAREA)).findFirst();
        TimedValue<GeoJsonObject> location = null;
        if (optRS.isPresent()) {
            ResourceSnapshot rs = optRS.get();
            TimedValue<?> value = rs.getValue();
            if (value != null) {
                location = getLocation(mapper, value.getValue(), rs.getValue().getTimestamp(), allowNull);
            }
        }
        if (location == null) {
            location = getLocation(provider, mapper, allowNull);
        }
        return location;
    }

    private static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            boolean allowNull) {
        final Optional<? extends ResourceSnapshot> locationResource = getProviderAdminField(provider, LOCATION);

        final Instant time;
        final Object rawValue;
        if (locationResource.isEmpty()) {
            time = Instant.EPOCH;
            rawValue = null;
        } else {
            final TimedValue<?> timedValue = locationResource.get().getValue();
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

    public static Datastream toDatastream(SensiNactSession userSession, Application application, ObjectMapper mapper,
            UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resource, ICriterion filter) {
        if (resource == null) {
            throw new NotFoundException();
        }

        final ProviderSnapshot provider = resource.getService().getProvider();
        final Map<String, Object> metadata = resource.getMetadata();

        String id = String.format("%s~%s~%s", provider.getName(), resource.getService().getName(), resource.getName());

        String name = toString(metadata.getOrDefault(FRIENDLY_NAME, resource.getName()));
        String description = toString(metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));

        UnitOfMeasurement unit = new UnitOfMeasurement(Objects.toString(metadata.get(SENSORTHINGS_UNIT_NAME), null),
                Objects.toString(metadata.get("unit"), null),
                Objects.toString(metadata.get(SENSORTHINGS_UNIT_DEFINITION), null));

        Polygon observedArea = getObservedArea(getLocation(provider, mapper, resource, false).getValue());

        String selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Datastreams({id})").resolveTemplate("id", id)
                .build().toString();
        String observationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Observations").build().toString();
        String observedPropertyLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("ObservedProperty").build()
                .toString();
        String sensorLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Sensor").build().toString();
        String thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();

        Datastream datastream = new Datastream(selfLink, id, name, description,
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation", unit, observedArea, null, null,
                metadata, observationsLink, observedPropertyLink, sensorLink, thingLink);
        if (expansions.shouldExpand("Observations", datastream)) {
            expansions.addExpansion("Observations", datastream,
                    RootResourceDelegateSensinact.getObservationList(userSession, application, mapper, uriInfo,
                            expansions.getExpansionSettings("Observations"), resource, filter, 25));
        }

        if (expansions.shouldExpand("ObservedProperty", datastream)) {
            expansions.addExpansion("ObservedProperty", datastream, toObservedProperty(userSession, application, mapper,
                    uriInfo, expansions.getExpansionSettings("ObservedProperty"), filter, resource));
        }

        if (expansions.shouldExpand("Sensor", datastream)) {
            expansions.addExpansion("Sensor", datastream, toSensor(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Sensor"), filter, resource));
        }

        if (expansions.shouldExpand("Thing", datastream)) {
            expansions.addExpansion("Thing", datastream, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), filter, provider));
        }

        return datastream;
    }

}
