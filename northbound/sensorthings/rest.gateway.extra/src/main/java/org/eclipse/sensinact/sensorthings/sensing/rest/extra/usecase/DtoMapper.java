package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

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
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.DatastreamUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

public class DtoMapper {

    public static final String ADMIN = "admin";
    public static final String DESCRIPTION = "description";
    public static final String FRIENDLY_NAME = "friendlyName";
    public static final String LOCATION = "location";
    public static final String DEFAULT_ENCODING_TYPE = "text/plain";
    public static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    public static final String VERSION = "v1.1";

    public static final String NO_DESCRIPTION = "No description";
    public static final String NO_DEFINITION = "No definition";

    private static GeoJsonObject aggregate(List<ExpandedLocation> locations) {
        return switch (locations.size()) {
        case 0:
            yield null;
        case 1:

            yield toFeature(locations.get(0));
        default:
            yield new FeatureCollection(locations.stream().map(DtoMapper::toFeature).toList(), null, null);
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

    public static DatastreamUpdate toObservationUpdate(String providerId, String idDatastream,
            ExpandedObservation obs) {
        String serviceName = idDatastream;

        Object observation;
        Map<String, Object> observationParameters;

        observation = sanitizeId(obs.result());
        Instant timestamp = obs.phenomenonTime();
        observationParameters = new HashMap<>();
        observationParameters.put("sensorthings.observation.id", String.valueOf(obs.id()));
        observationParameters.put("sensorthings.observation.resultQuality", obs.resultQuality());
        if (obs.parameters() != null) {
            obs.parameters()
                    .forEach((k, v) -> observationParameters.put("sensorthings.observation.parameters." + k, v));
        }

        return new DatastreamUpdate(providerId, serviceName, idDatastream, null, null, observation, timestamp,
                observationParameters, null, null, null, null, null, null);
    }

    public static DatastreamUpdate toObservedPropertyUpdate(String providerId, String idDatastream,
            ExpandedObservedProperty obs) {
        String serviceName = idDatastream;

        Map<String, Object> observedPropertyMetadata;
        String observedProperty = sanitizeId(obs.id() == null ? obs.name() : obs.id());

        observedPropertyMetadata = new HashMap<>();
        observedPropertyMetadata.put("sensorthings.observedProperty.name", obs.name());
        observedPropertyMetadata.put("sensorthings.observedProperty.description", obs.description());
        observedPropertyMetadata.put("sensorthings.observedProperty.definition", obs.definition());
        if (obs.properties() != null) {
            obs.properties().forEach(
                    (k, v) -> observedPropertyMetadata.put("sensorthings.observedProperty.properties." + k, v));
        }
        return new DatastreamUpdate(providerId, serviceName, idDatastream, null, null, null, null, null, null, null,
                null, null, observedProperty, observedPropertyMetadata);
    }

    public static DatastreamUpdate toDatastreamUpdate(String providerId, ExpandedDataStream ds) {
        String serviceName = sanitizeId(ds.name() == null ? ds.id() : ds.name());
        Instant timestamp = ds.phenomenonTime() == null ? null : ds.phenomenonTime().start();

        Object observation;
        Map<String, Object> observationParameters;
        if (ds.observations() == null || ds.observations().isEmpty()) {
            observation = Map.of();
            observationParameters = Map.of();
        } else {
            Observation o = ds.observations().get(0);
            observation = sanitizeId(o.result());
            timestamp = o.phenomenonTime();
            observationParameters = new HashMap<>();
            observationParameters.put("sensorthings.observation.id", String.valueOf(o.id()));
            observationParameters.put("sensorthings.observation.resultQuality", o.resultQuality());
            if (o.parameters() != null) {
                o.parameters()
                        .forEach((k, v) -> observationParameters.put("sensorthings.observation.parameters." + k, v));
            }
        }

        String unit;
        Map<String, Object> unitMetadata;
        if (ds.unitOfMeasurement() == null) {
            unit = null;
            unitMetadata = null;
        } else {
            unit = ds.unitOfMeasurement().symbol();
            unitMetadata = Map.of("sensorthings.unit.name", String.valueOf(ds.unitOfMeasurement().name()),
                    "sensorthings.unit.definition", String.valueOf(ds.unitOfMeasurement().definition()));
        }

        String sensor;
        Map<String, Object> sensorMetadata;
        if (ds.sensor() == null) {
            sensor = null;
            sensorMetadata = Map.of();
        } else {
            Sensor dsSensor = ds.sensor();
            sensor = sanitizeId(toString(dsSensor.id() == null ? dsSensor.name() : dsSensor.id()));
            sensorMetadata = new HashMap<>();
            sensorMetadata.put("sensorthings.sensor.name", ds.sensor().name());
            sensorMetadata.put("sensorthings.sensor.description", ds.sensor().description());
            sensorMetadata.put("sensorthings.sensor.metadata", ds.sensor().metadata());
            sensorMetadata.put("sensorthings.sensor.encodingType", ds.sensor().encodingType());
            if (ds.sensor().properties() != null) {
                ds.sensor().properties()
                        .forEach((k, v) -> sensorMetadata.put("sensorthings.sensor.properties." + k, v));
            }
        }

        String observedProperty;
        Map<String, Object> observedPropertyMetadata;
        if (ds.observedProperty() == null) {
            observedProperty = null;
            observedPropertyMetadata = Map.of();
        } else {
            ObservedProperty dsOp = ds.observedProperty();
            observedProperty = sanitizeId(dsOp.id() == null ? dsOp.name() : dsOp.id());

            observedPropertyMetadata = new HashMap<>();
            observedPropertyMetadata.put("sensorthings.observedProperty.name", ds.observedProperty().name());
            observedPropertyMetadata.put("sensorthings.observedProperty.description",
                    ds.observedProperty().description());
            observedPropertyMetadata.put("sensorthings.observedProperty.definition",
                    ds.observedProperty().definition());
            if (ds.observedProperty().properties() != null) {
                ds.observedProperty().properties().forEach(
                        (k, v) -> observedPropertyMetadata.put("sensorthings.observedProperty.properties." + k, v));
            }
        }
        Object dataStreamId = sanitizeId(ds.id() == null ? ds.name() : ds.id());
        return new DatastreamUpdate(providerId, serviceName, dataStreamId, ds.name(), ds.description(), observation,
                timestamp, observationParameters, unit, unitMetadata, sensor, sensorMetadata, observedProperty,
                observedPropertyMetadata);
    }

    private static Feature toFeature(ExpandedLocation location) {
        Feature f;

        if (location.location() != null) {
            String id = sanitizeId(location.id());
            f = switch (location.location().type()) {
            case Feature:
                yield (Feature) location.location();
            case FeatureCollection:

                yield toFeature((FeatureCollection) location.location());
            case GeometryCollection:
            case LineString:
            case MultiLineString:
            case MultiPoint:
            case MultiPolygon:
            case Point:
            case Polygon:
                yield new Feature(id, (Geometry) location.location(), Map.of("sensorthings.location.description",
                        location.description(), "sensorthings.location.name", location.name()), null, null);
            default:
                throw new IllegalArgumentException("Unknown GeoJSON object " + location.location().type());
            };

        } else {
            f = null;
        }

        return f;
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

    public static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    public static Stream<SensorThingsUpdate> toUpdates(ExpandedThing thing) {
        String providerId = sanitizeId(thing.name() == null ? thing.id() : thing.name());
        Object id = thing.id() == null ? providerId : thing.id();
        GeoJsonObject location = null;
        if (thing.locations() != null) {
            location = aggregate(thing.locations());
        }
        Map<String, Object> thingProperties = null;
        if (thing.properties() != null) {
            thingProperties = thing.properties().entrySet().stream()
                    .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue));
        }
        ThingUpdate provider = new ThingUpdate(providerId, thing.name(), thing.description(), location, id,
                thingProperties);

        if (thing.datastreams() != null) {
            return Stream.concat(Stream.of(provider),
                    thing.datastreams().stream().map(d -> toDatastreamUpdate(providerId, d)));
        }
        return Stream.of(provider);

    }

    public static Stream<SensorThingsUpdate> toUpdates(String providerId, String thingId,
            List<ExpandedLocation> locations, List<ExpandedDataStream> datastreams) {
        GeoJsonObject location = aggregate(locations);

        ThingUpdate provider = new ThingUpdate(providerId, null, null, location, thingId, null);

        return Stream.concat(Stream.of(provider), datastreams.stream().map(d -> toDatastreamUpdate(providerId, d)));
    }

    public static String getProperty(GeoJsonObject location, String propName) {
        if (location instanceof Feature) {
            Feature f = (Feature) location;
            return DtoMapper.toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName)));
        } else if (location instanceof FeatureCollection) {
            FeatureCollection fc = (FeatureCollection) location;
            return fc.features().stream()
                    .map(f -> DtoMapper.toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName))))
                    .filter(p -> p != null).findFirst().orElse(null);
        }
        return null;
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

    public static ExpandedThing toExpandedThing(ExtraUseCaseRequest<ExpandedLocation> request,
            ExpandedLocation location, ProviderSnapshot provider) {

        String id = provider.getName();
        // get current location
        ExpandedLocation readLocation = toLocation(request.session(), request.mapper(), request.uriInfo(), provider);
        List<ExpandedLocation> locations = readLocation.id().equals(location.id()) ? List.of(location)
                : List.of(readLocation, location);
        ExpandedThing thing = new ExpandedThing(null, id, null, null, null, null, null, null, null, locations, null);

        return thing;
    }

    public static TimedValue<GeoJsonObject> getLocation(ObjectMapper mapper, Object rawValue, Instant time,
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

    public static ExpandedLocation toLocation(SensiNactSession userSession, ObjectMapper mapper, UriInfo uriInfo,
            ProviderSnapshot provider) {
        final String providerName = provider.getName();
        final TimedValue<GeoJsonObject> rcLocation = DtoMapper.getLocation(provider, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        String id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String name = Objects.requireNonNullElse(getProperty(object, "name"), providerName);

        String description = Objects.requireNonNullElse(getProperty(object, DtoMapper.DESCRIPTION),
                DtoMapper.NO_DESCRIPTION);

        String selfLink = uriInfo.getBaseUriBuilder().path(DtoMapper.VERSION).path("Locations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Things").build().toString();
        String historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("HistoricalLocations").build()
                .toString();

        return new ExpandedLocation(selfLink, id, name, description, DtoMapper.ENCODING_TYPE_VND_GEO_JSON, object,
                thingsLink, historicalLocationsLink, null);
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
