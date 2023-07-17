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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers;

import java.time.Instant;
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
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.NotFoundException;
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

public class DtoMapper {

    private static final String NO_DESCRIPTION = "No description";

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

    public static Thing toThing(ProviderSnapshot provider) {
        final String providerName = provider.getName();
        Thing thing = new Thing();
        thing.id = providerName;
        thing.name = toString(getProviderAdminFieldValue(provider, "friendlyName").orElse(providerName));
        thing.description = toString(getProviderAdminFieldValue(provider, "description").orElse(NO_DESCRIPTION));
        return thing;
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

    public static Location toLocation(ObjectMapper mapper, ProviderSnapshot provider) {
        Location location = new Location();

        final String providerName = provider.getName();
        final TimedValue<GeoJsonObject> rcLocation = getLocation(provider, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        location.id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String friendlyName = getProperty(object, "name");
        location.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = getProperty(object, "description");
        location.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        location.encodingType = "application/vnd.geo+json";
        location.location = object;
        return location;
    }

    public static HistoricalLocation toHistoricalLocation(ObjectMapper mapper, ProviderSnapshot provider) {
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
        return historicalLocation;
    }

    private static Polygon getObservedArea(GeoJsonObject object) {

        if (object instanceof Feature) {
            object = ((Feature) object).geometry;
        } else if (object instanceof FeatureCollection) {
            // TODO is there a better mapping?
            object = ((FeatureCollection) object).features.stream().map((f) -> f.geometry)
                    .filter(Polygon.class::isInstance).map(Polygon.class::cast).findFirst().orElse(null);
        }
        return object instanceof Polygon ? (Polygon) object : null;
    }

    public static Datastream toDatastream(ObjectMapper mapper, ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }

        Datastream datastream = new Datastream();

        final ProviderSnapshot provider = resource.getService().getProvider();
        final Map<String, Object> metadata = resource.getMetadata();

        datastream.id = String.format("%s~%s~%s", provider.getName(), resource.getService().getName(),
                resource.getName());

        datastream.name = toString(metadata.getOrDefault("friendlyName", resource.getName()));
        datastream.description = toString(metadata.getOrDefault("description", NO_DESCRIPTION));

        // TODO can we make this more fine-grained
        datastream.observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";

        UnitOfMeasurement unit = new UnitOfMeasurement();
        unit.symbol = Objects.toString(metadata.get("unit"), null);
        unit.name = Objects.toString(metadata.get("sensorthings.unit.name"), null);
        unit.definition = Objects.toString(metadata.get("sensorthings.unit.definition"), null);
        datastream.unitOfMeasurement = unit;

        datastream.observedArea = getObservedArea(getLocation(provider, mapper, false).getValue());
        datastream.properties = metadata;
        return datastream;
    }

    public static Sensor toSensor(ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }

        Sensor sensor = new Sensor();

        final String provider = resource.getService().getProvider().getName();
        final Map<String, Object> metadata = resource.getMetadata();

        sensor.id = String.format("%s~%s~%s", provider, resource.getService().getName(), resource.getName());

        sensor.name = toString(metadata.getOrDefault("friendlyName", resource.getName()));
        sensor.description = toString(metadata.getOrDefault("description", NO_DESCRIPTION));
        sensor.properties = metadata;

        sensor.metadata = metadata.getOrDefault("sensorthings.sensor.metadata", "No metadata");
        sensor.encodingType = toString(metadata.getOrDefault("sensorthings.sensor.encodingType", "text/plain"));
        return sensor;
    }

    public static Observation toObservation(String provider, String service, String resource, TimedValue<?> tv) {
        Observation observation = new Observation();

        observation.id = String.format("%s~%s~%s~%s", provider, service, resource,
                Long.toString(tv.getTimestamp().toEpochMilli(), 16));

        observation.resultTime = tv.getTimestamp();
        observation.result = tv.getValue();
        observation.phenomenonTime = tv.getTimestamp();
        return observation;
    }

    public static ObservedProperty toObservedProperty(ResourceSnapshot resource) {
        ObservedProperty observedProperty = new ObservedProperty();

        final Map<String, Object> metadata = resource.getMetadata();

        observedProperty.id = String.format("%s~%s~%s", resource.getService().getProvider().getName(),
                resource.getService().getName(), resource.getName());

        observedProperty.name = toString(metadata.getOrDefault("friendlyName", resource.getName()));
        observedProperty.description = toString(metadata.getOrDefault("description", NO_DESCRIPTION));
        observedProperty.properties = metadata;

        observedProperty.definition = toString(
                metadata.getOrDefault("sensorthings.observedproperty.definition", "No definition"));
        return observedProperty;
    }

    private static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            boolean allowNull) {

        final Optional<ResourceSnapshot> locationResource = getProviderAdminField(provider, "location");

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

    public static FeatureOfInterest toFeatureOfInterest(ObjectMapper mapper, ProviderSnapshot provider) {
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();

        final String providerName = provider.getName();

        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, false);
        final GeoJsonObject object = location.getValue();

        featureOfInterest.id = providerName;

        String friendlyName = getProperty(object, "name");
        featureOfInterest.name = Objects.requireNonNullElse(friendlyName, providerName);

        String description = getProperty(object, "description");
        featureOfInterest.description = Objects.requireNonNullElse(description, NO_DESCRIPTION);

        featureOfInterest.encodingType = "application/vnd.geo+json";
        featureOfInterest.feature = object;
        return featureOfInterest;
    }
}
