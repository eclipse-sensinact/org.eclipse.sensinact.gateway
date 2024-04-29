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

import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.SensorthingsAnnotations.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.ServiceDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;

public class DtoMapper {

    private static final String DESCRIPTION = "description";
    private static final String FRIENDLY_NAME = "friendlyName";
    private static final String LOCATION = "location";
    private static final String DEFAULT_ENCODING_TYPE = "text/plain";
    private static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    private static final String VERSION = "v1.1";

    private static final String NO_DESCRIPTION = "No description";
    private static final String NO_DEFINITION = "No definition";

    private static ResourceDescription getProviderAdminField(SensiNactSession userSession, String provider,
            String resource) {
        ResourceDescription description = userSession.describeResource(provider, "admin", resource);
        // Use an empty description to avoid NPE elsewhere
        return description == null ? new ResourceDescription() : description;
    }

    private static Optional<ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider, String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> "admin".equals(s.getName())).findFirst()
                .get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    private static Optional<Object> getProviderAdminFieldValue(ProviderSnapshot provider, String resource) {
        Optional<ResourceSnapshot> rc = getProviderAdminField(provider, resource);
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

    public static Thing toThing(SensiNactSession userSession, UriInfo uriInfo, String providerName) {
        Thing thing = new Thing();
        thing.id = providerName;

        String friendlyName = toString(getProviderAdminField(userSession, providerName, FRIENDLY_NAME).value);
        thing.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = toString(getProviderAdminField(userSession, providerName, DESCRIPTION).value);
        thing.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        thing.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Things({id})")
                .resolveTemplate("id", providerName).build().toString();
        thing.datastreamsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Datastreams").build().toString();
        thing.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("HistoricalLocations")
                .build().toString();
        thing.locationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Locations").build().toString();

        return thing;
    }

    public static Thing toThing(UriInfo uriInfo, ProviderSnapshot provider) {
        final String providerName = provider.getName();
        Thing thing = new Thing();
        thing.id = providerName;

        thing.name = toString(getProviderAdminFieldValue(provider, FRIENDLY_NAME).orElse(providerName));

        thing.description = toString(getProviderAdminFieldValue(provider, DESCRIPTION).orElse(NO_DESCRIPTION));

        thing.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Things({id})")
                .resolveTemplate("id", providerName).build().toString();
        thing.datastreamsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Datastreams").build().toString();
        thing.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("HistoricalLocations")
                .build().toString();
        thing.locationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Locations").build().toString();

        return thing;
    }


    public static Location toLocation(SensiNactSession userSession, UriInfo uriInfo, ObjectMapper mapper,
            String providerName) {
        Location location = new Location();

        final TimedValue<GeoJsonObject> rcLocation = getLocation(userSession, mapper, providerName, false);
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

        return location;
    }

    public static Location toLocation(UriInfo uriInfo, ObjectMapper mapper, ProviderSnapshot provider) {
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

        return location;
    }

    public static HistoricalLocation toHistoricalLocation(SensiNactSession userSession, ObjectMapper mapper,
            UriInfo uriInfo, String providerName) {
        HistoricalLocation historicalLocation = new HistoricalLocation();

        final TimedValue<GeoJsonObject> location = getLocation(userSession, mapper, providerName, true);
        final Instant time;
        if (location.getTimestamp() == null) {
            time = Instant.EPOCH;
        } else {
            time = location.getTimestamp();
        }

        historicalLocation.id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));
        historicalLocation.time = time;

        historicalLocation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", historicalLocation.id).build().toString();
        historicalLocation.thingLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink).path("Thing")
                .build().toString();
        historicalLocation.locationsLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink)
                .path("Locations").build().toString();

        return historicalLocation;
    }

    public static HistoricalLocation toHistoricalLocation(ObjectMapper mapper, UriInfo uriInfo,
            ProviderSnapshot provider) {
        HistoricalLocation historicalLocation = new HistoricalLocation();

        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, true);
        final Instant time;
        if (location.getTimestamp() == null) {
            time = Instant.EPOCH;
        } else {
            time = location.getTimestamp();
        }

        historicalLocation.id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));
        historicalLocation.time = time;

        historicalLocation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                .resolveTemplate("id", historicalLocation.id).build().toString();
        historicalLocation.thingLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink).path("Thing")
                .build().toString();
        historicalLocation.locationsLink = uriInfo.getBaseUriBuilder().uri(historicalLocation.selfLink)
                .path("Locations").build().toString();

        return historicalLocation;
    }

    public static Datastream toDatastream(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ResourceDescription resource) {
        if (resource == null) {
            throw new NotFoundException();
        }
         
        Datastream datastream = new Datastream();

        datastream.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);

        datastream.name = toString(resource.metadata.getOrDefault(FRIENDLY_NAME, resource.resource));
        datastream.description = toString(resource.metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));

        // TODO can we make this more fine-grained
        datastream.observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

        UnitOfMeasurement unit = new UnitOfMeasurement();
        unit.symbol = Objects.toString(resource.metadata.get("unit"), null);
        unit.name = Objects.toString(resource.metadata.get(SENSORTHINGS_UNIT_NAME), null);
        unit.definition = Objects.toString(resource.metadata.get(SENSORTHINGS_UNIT_DEFINITION), null);
        datastream.unitOfMeasurement = unit;

        datastream.observedArea = getObservedArea(getLocation(userSession, mapper, resource, false).getValue());
        datastream.properties = resource.metadata;

        datastream.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Datastreams({id})")
                .resolveTemplate("id", datastream.id).build().toString();
        datastream.observationsLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Observations").build()
                .toString();
        datastream.observedPropertyLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("ObservedProperty")
                .build().toString();
        datastream.sensorLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Sensor").build().toString();
        datastream.thingLink = uriInfo.getBaseUriBuilder().uri(datastream.selfLink).path("Thing").build().toString();

        return datastream;
    }

    public static Datastream toDatastream(ObjectMapper mapper, UriInfo uriInfo, ResourceSnapshot resource) {
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

        return datastream;
    }


    public static Sensor toSensor(UriInfo uriInfo, ResourceDescription resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Sensor sensor = new Sensor();

        sensor.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);

        sensor.name = toString(resource.metadata.getOrDefault(FRIENDLY_NAME, resource.resource));
        sensor.description = toString(resource.metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));
        sensor.properties = resource.metadata;

        sensor.metadata = resource.metadata.getOrDefault(SENSORTHINGS_SENSOR_METADATA, "No metadata");
        sensor.encodingType = toString(
                resource.metadata.getOrDefault(SENSORTHINGS_SENSOR_ENCODING_TYPE, DEFAULT_ENCODING_TYPE));

        sensor.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Sensors({id})")
                .resolveTemplate("id", sensor.id).build().toString();
        sensor.datastreamsLink = uriInfo.getBaseUriBuilder().uri(sensor.selfLink).path("Datastreams").build()
                .toString();

        return sensor;
    }

    public static Sensor toSensor(UriInfo uriInfo, ResourceSnapshot resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Sensor sensor = new Sensor();

        final String provider = resource.getService().getProvider().getName();
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

        return sensor;
    }

    public static Observation toObservation(UriInfo uriInfo, ResourceDescription resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Observation observation = new Observation();

        if (resource.timestamp != null) {
            observation.id = String.format("%s~%s~%s~%s", resource.provider, resource.service, resource.resource,
                    Long.toString(resource.timestamp.toEpochMilli(), 16));
        } else {
            observation.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);
        }

        observation.resultTime = resource.timestamp;
        observation.result = resource.value;
        observation.phenomenonTime = resource.timestamp;
        observation.resultQuality = resource.metadata.get(SENSORTHINGS_OBSERVATION_QUALITY);

        observation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})")
                .resolveTemplate("id", observation.id).build().toString();
        observation.datastreamLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink).path("Datastream").build()
                .toString();
        observation.featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink)
                .path("FeatureOfInterest").build().toString();

        return observation;
    }

    public static List<Observation> toObservationList(UriInfo uriInfo, String provider, String service, String resource,
            List<TimedValue<?>> observations) {
        if (resource == null) {
            throw new NotFoundException();
        }

        List<Observation> list = new ArrayList<>(observations.size());
        for (TimedValue<?> tv : observations) {
            list.add(toObservation(uriInfo, provider, service, resource, tv));
        }

        return list;
    }

    public static Observation toObservation(UriInfo uriInfo, String provider, String service, String resource,
            TimedValue<?> tv) {
        Observation observation = new Observation();

        observation.id = String.format("%s~%s~%s~%s", provider, service, resource,
                Long.toString(tv.getTimestamp().toEpochMilli(), 16));

        observation.resultTime = tv.getTimestamp();
        observation.result = tv.getValue();
        observation.phenomenonTime = tv.getTimestamp();

        observation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})")
                .resolveTemplate("id", observation.id).build().toString();
        observation.datastreamLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink).path("Datastream").build()
                .toString();
        observation.featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink)
                .path("FeatureOfInterest").build().toString();
        return observation;
    }

    public static Observation toObservation(UriInfo uriInfo, ResourceSnapshot resource) {
        if (resource == null) {
            throw new NotFoundException();
        }

        Observation observation = new Observation();
        final Instant timestamp = resource.getValue().getTimestamp();

        observation.id = String.format("%s~%s~%s~%s", resource.getService().getProvider().getName(),
                resource.getService().getName(), resource.getName(), Long.toString(timestamp.toEpochMilli(), 16));

        observation.resultTime = timestamp;
        observation.result = resource.getValue().getValue();
        observation.phenomenonTime = timestamp;
        observation.resultQuality = resource.getMetadata().get(SENSORTHINGS_OBSERVATION_QUALITY);

        observation.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})")
                .resolveTemplate("id", observation.id).build().toString();
        observation.datastreamLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink).path("Datastream").build()
                .toString();
        observation.featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(observation.selfLink)
                .path("FeatureOfInterest").build().toString();

        return observation;
    }

    public static ObservedProperty toObservedProperty(UriInfo uriInfo, ResourceDescription resource) {
        ObservedProperty observedProperty = new ObservedProperty();

        observedProperty.id = String.format("%s~%s~%s", resource.provider, resource.service, resource.resource);

        observedProperty.name = toString(resource.metadata.getOrDefault(FRIENDLY_NAME, resource.resource));
        observedProperty.description = toString(resource.metadata.getOrDefault(DESCRIPTION, NO_DESCRIPTION));
        observedProperty.properties = resource.metadata;

        observedProperty.definition = toString(
                resource.metadata.getOrDefault(SENSORTHINGS_OBSERVEDPROPERTY_DEFINITION, NO_DEFINITION));

        observedProperty.selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("ObservedProperties({id})")
                .resolveTemplate("id", observedProperty.id).build().toString();
        observedProperty.datastreamsLink = uriInfo.getBaseUriBuilder().uri(observedProperty.selfLink)
                .path("Datastreams").build().toString();

        return observedProperty;
    }

    public static ObservedProperty toObservedProperty(UriInfo uriInfo, ResourceSnapshot resource) {
        ObservedProperty observedProperty = new ObservedProperty();

        final Map<String, Object> metadata = resource.getMetadata();

        observedProperty.id = String.format("%s~%s~%s", resource.getService().getProvider().getName(),
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

        return observedProperty;
    }

    public static FeatureOfInterest toFeatureOfInterest(SensiNactSession userSession, UriInfo uriInfo,
            ObjectMapper mapper, String providerName) {
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();
    
        final TimedValue<GeoJsonObject> location = getLocation(userSession, mapper, providerName, false);
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
    
        return featureOfInterest;
    }

    public static FeatureOfInterest toFeatureOfInterest(UriInfo uriInfo, ObjectMapper mapper,
            ProviderSnapshot provider) {
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
            geometry = ((Feature) location).geometry;
        } else if (location instanceof FeatureCollection) {
            // TODO is there a better mapping?
            geometry = ((FeatureCollection) location).features.stream().map((f) -> f.geometry)
                    .filter(Polygon.class::isInstance).map(Polygon.class::cast).findFirst().orElse(null);
        }
        return geometry instanceof Polygon ? (Polygon) geometry : null;
    }

    private static String getProperty(GeoJsonObject location, String propName) {
        if (location instanceof Feature) {
            Feature f = (Feature) location;
            return toString(f.properties.get(propName));
        } else if (location instanceof FeatureCollection) {
            FeatureCollection fc = (FeatureCollection) location;
            return fc.features.stream().map(f -> toString(f.properties.get(propName))).filter(p -> p != null)
                    .findFirst().orElse(null);
        }
        return null;
    }

    private static TimedValue<GeoJsonObject> getLocation(SensiNactSession userSession, ObjectMapper mapper,
            ResourceDescription resource, boolean allowNull) {
        ServiceDescription describeService = userSession.describeService(resource.provider, resource.service);
        Optional<ResourceDescription> optRD = describeService.resources.stream()
                .map(r -> userSession.describeResource(resource.provider, resource.service, r))
                .filter(describeResource -> describeResource.metadata.keySet().contains(SENSORTHINGS_OBSERVEDAREA))
                .findFirst();
        TimedValue<GeoJsonObject> location = null;
        if (optRD.isPresent()) {
            ResourceDescription describeResource = optRD.get();
            location = getLocation(mapper, describeResource.value, describeResource.timestamp, allowNull);
        }
        if (location == null) {
            ResourceDescription locationResource = getProviderAdminField(userSession, resource.provider, LOCATION);
            Instant time = locationResource.timestamp != null ? locationResource.timestamp : Instant.EPOCH;
            location = getLocation(mapper, locationResource.value, time, allowNull);
        }
        return location;
    }

    private static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            ResourceSnapshot resource, boolean allowNull) {
        Optional<ResourceSnapshot> optRS = resource.getService().getResources().stream()
                .filter(r -> r.getMetadata().keySet().contains(SENSORTHINGS_OBSERVEDAREA)).findFirst();
        TimedValue<GeoJsonObject> location = null;
        if (optRS.isPresent()) {
            ResourceSnapshot rs = optRS.get();
            location = getLocation(mapper, rs.getValue().getValue(), rs.getValue().getTimestamp(), allowNull);
        }
        if (location == null) {
            location = getLocation(provider, mapper, allowNull);
        }
        return location;
    }    
    
    private static TimedValue<GeoJsonObject> getLocation(SensiNactSession userSession, ObjectMapper mapper,
            String providerName, boolean allowNull) {
        ResourceDescription locationResource = getProviderAdminField(userSession, providerName, LOCATION);
        final Instant time = locationResource.timestamp != null ? locationResource.timestamp : Instant.EPOCH;
        final Object rawValue = locationResource.value;
        return getLocation(mapper, rawValue, time, allowNull);
    }
    
    private static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            boolean allowNull) {
        final Optional<ResourceSnapshot> locationResource = getProviderAdminField(provider, LOCATION);

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
                Point point = new Point();
                point.coordinates = new Coordinates();
                parsedLocation = point;
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

        return new TimedValue<GeoJsonObject>() {
            @Override
            public Instant getTimestamp() {
                return time;
            }

            @Override
            public GeoJsonObject getValue() {
                return parsedLocation;
            }
        };
    }

}
