package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream.DatastreamUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing.ThingUpdate;

import jakarta.ws.rs.BadRequestException;

public class Helpers {

    public static final String ADMIN = "admin";
    public static final String DESCRIPTION = "description";
    public static final String FRIENDLY_NAME = "friendlyName";
    public static final String LOCATION = "location";
    public static final String DEFAULT_ENCODING_TYPE = "text/plain";
    public static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    public static final String VERSION = "v1.1";

    public static final String NO_DESCRIPTION = "No description";
    public static final String NO_DEFINITION = "No definition";

    public static GeoJsonObject aggregate(List<Location> locations) {
        return switch (locations.size()) {
        case 0:
            yield null;
        case 1:
            yield toFeature(locations.get(0));
        default:
            yield new FeatureCollection(locations.stream().map(Helpers::toFeature).toList(), null, null);
        };
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

    public static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider,
            String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> ADMIN.equals(s.getName())).findFirst()
                .get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    public static Optional<Object> getProviderAdminFieldValue(ProviderSnapshot provider, String resource) {
        Optional<? extends ResourceSnapshot> rc = getProviderAdminField(provider, resource);
        if (rc.isPresent()) {
            TimedValue<?> value = rc.get().getValue();
            if (value != null) {
                return Optional.ofNullable(value.getValue());
            }
        }
        return Optional.empty();
    }

    public static String sanitizeId(Object object) {
        return String.valueOf(object).replaceAll("[^0-9a-zA-Z\\.\\-_]", "_");
    }

    public static DatastreamUpdate toDatastreamUpdate(String providerId, ExpandedDataStream ds) {
        String serviceName = Helpers.sanitizeId(ds.name == null ? ds.id : ds.name);
        Instant timestamp = ds.phenomenonTime == null ? null : ds.phenomenonTime.start;

        Object observation;
        Map<String, Object> observationParameters;
        if (ds.observations == null || ds.observations.isEmpty()) {
            observation = null;
            observationParameters = null;
        } else {
            Observation o = ds.observations.get(0);
            observation = o.result;
            timestamp = o.phenomenonTime;
            observationParameters = new HashMap<>();
            observationParameters.put("sensorthings.observation.id", String.valueOf(o.id));
            observationParameters.put("sensorthings.observation.resultQuality", o.resultQuality);
            if (o.parameters != null) {
                o.parameters
                        .forEach((k, v) -> observationParameters.put("sensorthings.observation.parameters." + k, v));
            }
        }

        String unit;
        Map<String, Object> unitMetadata;
        if (ds.unitOfMeasurement == null) {
            unit = null;
            unitMetadata = null;
        } else {
            unit = ds.unitOfMeasurement.symbol;
            unitMetadata = Map.of("sensorthings.unit.name", String.valueOf(ds.unitOfMeasurement.name),
                    "sensorthings.unit.definition", String.valueOf(ds.unitOfMeasurement.definition));
        }

        Object sensor;
        Map<String, Object> sensorMetadata;
        if (ds.sensor == null) {
            sensor = null;
            sensorMetadata = null;
        } else {
            sensor = Objects.toString(ds.sensor.id);
            sensorMetadata = new HashMap<>();
            sensorMetadata.put("sensorthings.sensor.name", ds.sensor.name);
            sensorMetadata.put("sensorthings.sensor.description", ds.sensor.description);
            sensorMetadata.put("sensorthings.sensor.metadata", ds.sensor.metadata);
            sensorMetadata.put("sensorthings.sensor.encodingType", ds.sensor.encodingType);
            if (ds.sensor.properties != null) {
                ds.sensor.properties.forEach((k, v) -> sensorMetadata.put("sensorthings.sensor.properties." + k, v));
            }
        }

        Object observedProperty;
        Map<String, Object> observedPropertyMetadata;
        if (ds.observedProperty == null) {
            observedProperty = null;
            observedPropertyMetadata = null;
        } else {
            observedProperty = ds.observedProperty.id;
            observedPropertyMetadata = new HashMap<>();
            observedPropertyMetadata.put("sensorthings.observedProperty.name", ds.observedProperty.name);
            observedPropertyMetadata.put("sensorthings.observedProperty.description", ds.observedProperty.description);
            observedPropertyMetadata.put("sensorthings.observedProperty.definition", ds.observedProperty.definition);
            if (ds.observedProperty.properties != null) {
                ds.observedProperty.properties.forEach(
                        (k, v) -> observedPropertyMetadata.put("sensorthings.observedProperty.properties." + k, v));
            }
        }

        return new DatastreamUpdate(providerId, serviceName, ds.id, ds.name, ds.description, observation, timestamp,
                observationParameters, unit, unitMetadata, sensor, sensorMetadata, observedProperty,
                observedPropertyMetadata);
    }

    private static Feature toFeature(FeatureCollection fc) {
        return switch (fc.features().size()) {
        case 0:
            yield null;
        case 1:
            yield fc.features().get(0);
        default:
            GeometryCollection gc = new GeometryCollection(
                    fc.features().stream().map(fe -> fe.geometry()).filter(Objects::nonNull).toList(), null, null);
            yield new Feature(fc.features().get(0).id() + ".combined", gc, null, null, null);
        };
    }

    public static Feature toFeature(Location location) {
        Feature f;

        if (location.location != null) {
            String id = sanitizeId(location.id);
            f = switch (location.location.type()) {
            case Feature:
                yield (Feature) location.location;
            case FeatureCollection:
                yield toFeature((FeatureCollection) location.location);
            case GeometryCollection:
            case LineString:
            case MultiLineString:
            case MultiPoint:
            case MultiPolygon:
            case Point:
            case Polygon:
                yield new Feature(id, (Geometry) location.location, Map.of("sensorthings.location.description",
                        location.description, "sensorthings.location.name", location.name), null, null);
            default:
                throw new IllegalArgumentException("Unknown GeoJSON object " + location.location.type());
            };
        } else {
            f = null;
        }

        return f;
    }

    public static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    public static Stream<SensorThingsUpdate> toUpdates(ExpandedThing thing) {
        String providerId = Helpers.sanitizeId(thing.name == null ? thing.id : thing.name);
        GeoJsonObject location = null;
        if (thing.locations != null) {
            location = Helpers.aggregate(thing.locations);
        }
        Map<String, Object> thingProperties = thing.properties.entrySet().stream()
                .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue));
        ThingUpdate provider = new ThingUpdate(providerId, thing.name, thing.description, location, thing.id,
                thingProperties);

        Stream<SensorThingsUpdate> updates = Stream.of(provider);
        if (thing.datastreams != null && thing.datastreams.size() > 0) {
            updates = Stream.concat(Stream.of(provider),
                    thing.datastreams.stream().map(d -> toDatastreamUpdate(providerId, d)));
        }
        return updates;
    }

    /**
     * Ensure the given ID contains a single segment
     */
    public static void validatedProviderId(String id) {
        if (id.contains("~")) {
            throw new BadRequestException("Multi-segments ID found");
        }
    }

}
