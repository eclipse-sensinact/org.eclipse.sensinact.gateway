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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.FeaturesOfInterestAccessImpl.getLiveObservations;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_OBSERVATION_QUALITY;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_OBSERVEDAREA;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_OBSERVEDPROPERTY_DEFINITION;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_SENSOR_ENCODING_TYPE;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_SENSOR_METADATA;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_UNIT_DEFINITION;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.SENSORTHINGS_UNIT_NAME;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
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

    private static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider, String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> ADMIN.equals(s.getName())).findFirst()
                .get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    private static Optional<Object> getProviderAdminFieldValue(ProviderSnapshot provider,
            String resource) {
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

    public static Thing toThing(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ProviderSnapshot provider) {
        Thing thing = new Thing();
        thing.id = provider.getName();

        String friendlyName = toString(getProviderAdminFieldValue(provider, FRIENDLY_NAME));
        thing.name = Objects.requireNonNullElse(friendlyName, provider.getName());

        String description = toString(getProviderAdminFieldValue(provider, DESCRIPTION));
        thing.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        thing.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Things({id})")
                .resolveTemplate("id", thing.id).build().toString();
        thing.datastreamsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Datastreams").build().toString();
        thing.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("HistoricalLocations")
                .build().toString();
        thing.locationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Locations").build().toString();


        if(expansions.shouldExpand("Datastreams", thing)) {
            expansions.addExpansion("Datastreams", thing, DatastreamsAccessImpl.getDataStreams(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Datastreams"), provider));
        }

        if(expansions.shouldExpand("HistoricalLocations", thing)) {
            ResultList<HistoricalLocation> list = new ResultList<>();
            list.value = List.of(DtoMapper.toHistoricalLocation(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("HistoricalLocations"), provider));
            expansions.addExpansion("HistoricalLocations", thing, list);
        }
        if(expansions.shouldExpand("Locations", thing)) {
            ResultList<Location> list = new ResultList<>();
            list.value = List.of(DtoMapper.toLocation(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Locations"), provider));
            expansions.addExpansion("Locations", thing, list);
        }

        return thing;
    }

    public static Location toLocation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ProviderSnapshot provider) {
        Location location = new Location();

        final String providerName = provider.getName();
        final TimedValue<GeoJsonObject> rcLocation = getLocation(provider, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        location.id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String friendlyName = getProperty(object, "name");
        location.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = getProperty(object, DESCRIPTION);
        location.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        location.encodingType = ENCODING_TYPE_VND_GEO_JSON;
        location.location = object;

        location.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Locations({id})")
                .resolveTemplate("id", location.id).build().toString();
        location.thingsLink = uriInfo.getBaseUriBuilder().uri(location.selfLink).path("Things").build().toString();
        location.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(location.selfLink)
                .path("HistoricalLocations").build().toString();

        if(expansions.shouldExpand("Things", location)) {
            ResultList<Thing> list = new ResultList<>();
            list.value = List.of(DtoMapper.toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), provider));
            expansions.addExpansion("Things", location, list);
        }
        if(expansions.shouldExpand("HistoricalLocations", location)) {
            ResultList<HistoricalLocation> list = new ResultList<>();
            list.value = List.of(DtoMapper.toHistoricalLocation(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("HistoricalLocations"), provider));
            expansions.addExpansion("HistoricalLocations", location, list);
        }

        return location;
    }

    public static List<HistoricalLocation> toHistoricalLocationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ProviderSnapshot provider, List<TimedValue<?>> historicalLocations) {
        if (provider == null) {
            throw new NotFoundException();
        }

        List<HistoricalLocation> list = new ArrayList<>(historicalLocations.size());
        for (TimedValue<?> tv : historicalLocations) {
            list.add(toHistoricalLocation(userSession, application, mapper, uriInfo, expansions,
                    provider, Optional.of(tv)));
        }

        return list;
    }

    public static HistoricalLocation toHistoricalLocation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ProviderSnapshot provider, Optional<TimedValue<?>> t) {
        if (provider == null) {
            throw new NotFoundException();
        }
        HistoricalLocation historicalLocation = new HistoricalLocation();
        final Instant time = t.map(TimedValue::getTimestamp).orElse( Instant.EPOCH);

        historicalLocation.id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));
        historicalLocation.time = time;

        historicalLocation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", historicalLocation.id).build().toString();
        historicalLocation.thingLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink).path("Thing")
                .build().toString();
        historicalLocation.locationsLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink)
                .path("Locations").build().toString();

        if(expansions.shouldExpand("Thing", historicalLocation)) {
            expansions.addExpansion("Thing", historicalLocation, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), provider));
        }
        if(expansions.shouldExpand("Locations", historicalLocation)) {
            ResultList<Location> list = new ResultList<>();
            list.value = List.of(DtoMapper.toLocation(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Locations"), provider));
            expansions.addExpansion("Locations", historicalLocation, list);
        }
        return historicalLocation;
    }

    public static HistoricalLocation toHistoricalLocation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ProviderSnapshot provider) {
        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, true);
        return toHistoricalLocation(userSession, application, mapper, uriInfo, expansions, provider, Optional.of(location));
    }

    public static Datastream toDatastream(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Datastream datastream = new Datastream();

        final ProviderSnapshot provider = resource.getService().getProvider();
        final Map<String, Object> metadata = resource.getMetadata();

        datastream.id = String.format("%s~%s~%s", provider.getName(), resource.getService().getName(),
                resource.getName());

        datastream.name = toString(metadata.getOrDefault(FRIENDLY_NAME, resource.getName()));
        datastream.description = toString(metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));

        // TODO can we make this more fine-grained
        datastream.observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

        UnitOfMeasurement unit = new UnitOfMeasurement();
        unit.symbol = Objects.toString(metadata.get("unit"), null);
        unit.name = Objects.toString(metadata.get(SENSORTHINGS_UNIT_NAME), null);
        unit.definition = Objects.toString(metadata.get(SENSORTHINGS_UNIT_DEFINITION), null);
        datastream.unitOfMeasurement = unit;

        datastream.observedArea = getObservedArea(getLocation(provider, mapper, resource, false).getValue());
        datastream.properties = metadata;

        datastream.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Datastreams({id})")
                .resolveTemplate("id", datastream.id).build().toString();
        datastream.observationsLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Observations").build()
                .toString();
        datastream.observedPropertyLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("ObservedProperty")
                .build().toString();
        datastream.sensorLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Sensor").build().toString();
        datastream.thingLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Thing").build().toString();

        if(expansions.shouldExpand("Observations", datastream)) {
            expansions.addExpansion("Observations", datastream, RootResourceAccessImpl.getObservationList(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Observations"), resource, 25));
        }

        if(expansions.shouldExpand("ObservedProperty", datastream)) {
            expansions.addExpansion("ObservedProperty", datastream, toObservedProperty(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("ObservedProperty"), resource));
        }

        if(expansions.shouldExpand("Sensor", datastream)) {
            expansions.addExpansion("Sensor", datastream, toSensor(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Sensor"), resource));
        }

        if(expansions.shouldExpand("Thing", datastream)) {
            expansions.addExpansion("Thing", datastream, toThing(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Thing"), provider));
        }

        return datastream;
    }

    public static Sensor toSensor(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Sensor sensor = new Sensor();

        ProviderSnapshot providerSnapshot = resource.getService().getProvider();
        final String provider = providerSnapshot.getName();
        final Map<String, Object> metadata = resource.getMetadata();

        sensor.id = String.format("%s~%s~%s", provider, resource.getService().getName(), resource.getName());

        sensor.name = toString(metadata.getOrDefault(FRIENDLY_NAME, resource.getName()));
        sensor.description = toString(metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));
        sensor.properties = metadata;

        sensor.metadata = metadata.getOrDefault(SENSORTHINGS_SENSOR_METADATA, "No metadata");
        sensor.encodingType = toString(metadata.getOrDefault(SENSORTHINGS_SENSOR_ENCODING_TYPE, DEFAULT_ENCODING_TYPE));

        sensor.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Sensors({id})")
                .resolveTemplate("id", sensor.id).build().toString();
        sensor.datastreamsLink = uriInfo.getBaseUriBuilder().uri(sensor.selfLink).path("Datastreams").build()
                .toString();

        if(expansions.shouldExpand("Datastreams", sensor)) {
            expansions.addExpansion("Datastreams", sensor, DatastreamsAccessImpl.getDataStreams(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Datastreams"), providerSnapshot));
        }

        return sensor;
    }

    public static List<Observation> toObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ResourceSnapshot resourceSnapshot, List<TimedValue<?>> observations) {
        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }

        List<Observation> list = new ArrayList<>(observations.size());
        for (TimedValue<?> tv : observations) {
            list.add(toObservation(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, Optional.of(tv)));
        }

        return list;
    }

    public static Observation toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ResourceSnapshot resource) {
        return toObservation(userSession, application, mapper, uriInfo, expansions, resource, Optional.ofNullable(resource.getValue()));
    }

    public static Observation toObservation(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ResourceSnapshot resource, Optional<TimedValue<?>> t) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Observation observation = new Observation();
        final Instant timestamp = t.map(TimedValue::getTimestamp).orElse(null);

        ProviderSnapshot providerSnapshot = resource.getService().getProvider();
        observation.id = String.format("%s~%s~%s~%s", providerSnapshot.getName(),
                resource.getService().getName(), resource.getName(), Long.toString(timestamp.toEpochMilli(), 16));

        observation.resultTime = timestamp;
        observation.result = t.map(TimedValue::getValue).orElse(null);
        observation.phenomenonTime = timestamp;
        observation.resultQuality = resource.getMetadata().get(SENSORTHINGS_OBSERVATION_QUALITY);

        observation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})")
                .resolveTemplate("id", observation.id).build().toString();
        observation.datastreamLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink).path("Datastream").build()
                .toString();
        observation.featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink)
                .path("FeatureOfInterest").build().toString();

        if(expansions.shouldExpand("Datastream", observation)) {
            expansions.addExpansion("Datastream", observation, toDatastream(userSession, application, mapper, uriInfo,
                    expansions.getExpansionSettings("Datastream"), resource));
        }

        if(expansions.shouldExpand("FeatureOfInterest", observation)) {
            expansions.addExpansion("FeatureOfInterest", observation, toFeatureOfInterest(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("FeatureOfInterest"), providerSnapshot));
        }

        return observation;
    }

    public static ObservedProperty toObservedProperty(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resource) {
        ObservedProperty observedProperty = new ObservedProperty();

        final Map<String, Object> metadata = resource.getMetadata();

        ProviderSnapshot providerSnapshot = resource.getService().getProvider();
        observedProperty.id = String.format("%s~%s~%s", providerSnapshot.getName(),
                resource.getService().getName(), resource.getName());

        observedProperty.name = toString(metadata.getOrDefault(FRIENDLY_NAME, resource.getName()));
        observedProperty.description = toString(metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));
        observedProperty.properties = metadata;

        observedProperty.definition = toString(
                metadata.getOrDefault(SENSORTHINGS_OBSERVEDPROPERTY_DEFINITION, NO_DEFINITION));

        observedProperty.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("ObservedProperties({id})")
                .resolveTemplate("id", observedProperty.id).build().toString();
        observedProperty.datastreamsLink = uriInfo.getBaseUriBuilder().uri(observedProperty.selfLink)
                .path("Datastreams").build().toString();

        if(expansions.shouldExpand("Datastreams", observedProperty)) {
            expansions.addExpansion("Datastreams", observedProperty, DatastreamsAccessImpl.getDataStreams(userSession,
                    application, mapper, uriInfo, expansions.getExpansionSettings("Datastreams"), providerSnapshot));
        }

        return observedProperty;
    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ProviderSnapshot provider) {
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();

        final String providerName = provider.getName();

        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, false);
        final GeoJsonObject object = location.getValue();

        featureOfInterest.id = providerName;

        String friendlyName = getProperty(object, "name");
        featureOfInterest.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = getProperty(object, DESCRIPTION);
        featureOfInterest.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        featureOfInterest.encodingType = ENCODING_TYPE_VND_GEO_JSON;
        featureOfInterest.feature = object;

        featureOfInterest.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("FeaturesOfInterest({id})")
                .resolveTemplate("id", featureOfInterest.id).build().toString();
        featureOfInterest.observationsLink = uriInfo.getBaseUriBuilder().uri(featureOfInterest.selfLink)
                .path("Observations").build().toString();

        if(expansions.shouldExpand("Observations", featureOfInterest)) {
            expansions.addExpansion("Observations", featureOfInterest, getLiveObservations(userSession, application,
                    mapper, uriInfo, expansions.getExpansionSettings("Observations"), provider));
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
            return fc.features().stream().map(f -> toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName)))).filter(p -> p != null)
                    .findFirst().orElse(null);
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
            if(value != null) {
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

}
