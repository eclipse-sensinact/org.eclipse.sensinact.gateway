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
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
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

    private static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider,
            String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> "admin".equals(s.getName())).findFirst()
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

    private static String toString(Feature f, String propName) {
        Map<String, Object> properties = f.properties();
        return properties == null ? null : toString(properties.get(propName));
    }

    public static Thing toThing(ProviderSnapshot provider) {
        final String providerName = provider.getName();
        Thing thing = new Thing(null, providerName,
                toString(getProviderAdminFieldValue(provider, "friendlyName").orElse(providerName)),
                toString(getProviderAdminFieldValue(provider, "description").orElse(NO_DESCRIPTION)), null, null, null,
                null);
        return thing;
    }

    private static String getProperty(GeoJsonObject location, String propName) {
        if (location instanceof Feature) {
            Feature f = (Feature) location;
            return toString(f, propName);
        } else if (location instanceof FeatureCollection) {
            FeatureCollection fc = (FeatureCollection) location;
            return fc.features().stream().map(f -> toString(f, propName)).filter(p -> p != null).findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static Location toLocation(ObjectMapper mapper, ProviderSnapshot provider) {
        final String providerName = provider.getName();
        final TimedValue<GeoJsonObject> rcLocation = getLocation(provider, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        String id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String name = Objects.requireNonNullElse(getProperty(object, "name"), providerName);

        String description = Objects.requireNonNullElse(getProperty(object, "description"), NO_DESCRIPTION);

        return new Location(null, id, name, description, "application/vnd.geo+json", object, null, null, null);
    }

    public static HistoricalLocation toHistoricalLocation(ObjectMapper mapper, ProviderSnapshot provider) {
        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, true);
        final Instant time;
        if (location.getTimestamp() == null) {
            time = Instant.EPOCH;
        } else {
            time = location.getTimestamp();
        }
        String id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));
        return new HistoricalLocation(null, id, time, null, null);
    }

    private static Polygon getObservedArea(GeoJsonObject object) {

        if (object instanceof Feature) {
            object = ((Feature) object).geometry();
        } else if (object instanceof FeatureCollection) {
            // TODO is there a better mapping?
            object = ((FeatureCollection) object).features().stream().map((f) -> f.geometry())
                    .filter(Polygon.class::isInstance).map(Polygon.class::cast).findFirst().orElse(null);
        }
        return object instanceof Polygon ? (Polygon) object : null;
    }

    public static Datastream toDatastream(ObjectMapper mapper, ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }

        final ProviderSnapshot provider = resource.getService().getProvider();
        final Map<String, Object> metadata = resource.getMetadata();

        String id = String.format("%s~%s~%s", provider.getName(), resource.getService().getName(), resource.getName());

        String name = toString(metadata.getOrDefault("friendlyName", resource.getName()));
        String description = toString(metadata.getOrDefault("description", NO_DESCRIPTION));

        UnitOfMeasurement unit = new UnitOfMeasurement(Objects.toString(metadata.get("sensorthings.unit.name"), null),
                Objects.toString(metadata.get("unit"), null),
                Objects.toString(metadata.get("sensorthings.unit.definition"), null));

        Geometry observedArea = getObservedArea(getLocation(provider, mapper, false).getValue());
        return new Datastream(null, id, name, description,
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation", unit, observedArea, null, null,
                metadata, null, null, null, null);
    }

    public static Sensor toSensor(ResourceSnapshot resource) throws NotFoundException {
        if (resource == null) {
            throw new NotFoundException();
        }

        final String provider = resource.getService().getProvider().getName();
        final Map<String, Object> metadata = resource.getMetadata();

        String id = String.format("%s~%s~%s", provider, resource.getService().getName(), resource.getName());

        String name = toString(metadata.getOrDefault("friendlyName", resource.getName()));
        String description = toString(metadata.getOrDefault("description", NO_DESCRIPTION));

        String encodingType = toString(metadata.getOrDefault("sensorthings.sensor.encodingType", "text/plain"));
        return new Sensor(null, id, name, description, encodingType,
                metadata.getOrDefault("sensorthings.sensor.metadata", "No metadata"), metadata, null);
    }

    public static Observation toObservation(String provider, String service, String resource, TimedValue<?> tv) {
        String id = String.format("%s~%s~%s~%s", provider, service, resource,
                Long.toString(tv.getTimestamp().toEpochMilli(), 16));

        Instant timestamp = tv.getTimestamp();
        return new Observation(null, id, timestamp, timestamp, tv.getValue(), null, null, null, null, null);
    }

    public static ObservedProperty toObservedProperty(ResourceSnapshot resource) {
        final Map<String, Object> metadata = resource.getMetadata();

        String id = String.format("%s~%s~%s", resource.getService().getProvider().getName(),
                resource.getService().getName(), resource.getName());

        String name = toString(metadata.getOrDefault("friendlyName", resource.getName()));
        String description = toString(metadata.getOrDefault("description", NO_DESCRIPTION));
        String definition = toString(
                metadata.getOrDefault("sensorthings.observedproperty.definition", "No definition"));

        return new ObservedProperty(null, id, name, description, definition, metadata, null);
    }

    private static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            boolean allowNull) {

        final Optional<? extends ResourceSnapshot> locationResource = getProviderAdminField(provider, "location");

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

    public static FeatureOfInterest toFeatureOfInterest(ObjectMapper mapper, ProviderSnapshot provider) {
        final String providerName = provider.getName();

        final TimedValue<GeoJsonObject> location = getLocation(provider, mapper, false);
        final GeoJsonObject object = location.getValue();

        String name = Objects.requireNonNullElse(getProperty(object, "name"), providerName);

        String description = Objects.requireNonNullElse(getProperty(object, "description"), NO_DESCRIPTION);

        return new FeatureOfInterest(null, providerName, name, description, "application/vnd.geo+json", object, null,
                null);
    }
}
