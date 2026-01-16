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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper;

import static java.util.stream.Collectors.toMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.DatastreamUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.LocationUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;

/**
 * Dto to Model record mapper for updating EMF
 */
public class DtoToModelMapper {

    public static final String ADMIN = "admin";
    public static final String DESCRIPTION = "description";
    public static final String FRIENDLY_NAME = "friendlyName";
    public static final String LOCATION = "location";
    public static final String DEFAULT_ENCODING_TYPE = "text/plain";
    public static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    public static final String VERSION = "v1.1";

    public static final String NO_DESCRIPTION = "No description";
    public static final String NO_DEFINITION = "No definition";

    public static String getIdFromRecord(Object record) {
        Object field = DtoToModelMapper.getRecordField(record, "id");
        Object id = null;
        if (field instanceof Map) {
            id = ((Map<?, ?>) field).values().stream().findFirst().get();
        }
        return id instanceof String ? (String) id : null;

    }

    public static String getNewId(Id model) {
        return sanitizeId(model.id() != null ? model.id() : UUID.randomUUID().toString().substring(0, 8));
    }

    public static Optional<? extends ResourceSnapshot> getProviderField(ServiceSnapshot service, String resource) {

        return service.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    public static String extractIdSegment(String id, int part) {
        if (id.isEmpty())
            return null;
        String[] parts = id.split("~");
        if (parts == null || parts.length == 0) {
            return id;
        }
        if (parts == null || parts.length == 0)
            return id;
        if (part < parts.length) {
            return parts[part];
        }
        return null;
    }

    public static String extractFirstIdSegment(String id) {
        return extractIdSegment(id, 0);
    }

    public static String extractSecondIdSegment(String id) {
        return extractIdSegment(id, 1);

    }

    public static String extractThirdIdSegment(String id) {
        return extractIdSegment(id, 2);

    }

    public static String extractFouthIdSegment(String id) {
        return extractIdSegment(id, 3);

    }

    public static Optional<Object> getProviderFieldValue(ServiceSnapshot service, String resource) {
        Optional<? extends ResourceSnapshot> rc = getProviderField(service, resource);
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

    public static SensorThingsUpdate toLocationUpdate(String providerId, ExpandedLocation location) {
        return new LocationUpdate(providerId, providerId, location.name(), location.description(),
                location.encodingType(), location.location());
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, ExpandedDataStream ds, Sensor sensor,
            ObservedProperty observedProperty, UnitOfMeasurement unit, ExpandedObservation lastObservation,
            ExpandedObservation lastObservationReceived, FeatureOfInterest featureOfInterest) {
        return toDatastreamUpdate(providerId, null, ds, sensor, observedProperty, unit, lastObservation,
                lastObservationReceived, featureOfInterest);
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, String thingId, ExpandedDataStream ds,
            Sensor sensor, ObservedProperty observedProperty, UnitOfMeasurement unit,
            ExpandedObservation lastObservation, FeatureOfInterest featureOfInterest) {
        return toDatastreamUpdate(providerId, thingId, ds, sensor, observedProperty, unit, lastObservation,
                lastObservation, featureOfInterest);
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, String thingId, ExpandedDataStream ds,
            Sensor sensor, ObservedProperty observedProperty, UnitOfMeasurement unit,
            ExpandedObservation lastObservation, ExpandedObservation lastObservationReceived,
            FeatureOfInterest featureOfInterest) {

        Instant timestamp = lastObservationReceived != null && lastObservationReceived.phenomenonTime() != null
                ? lastObservationReceived.phenomenonTime()
                : Instant.now();

        String name = ds != null ? ds.name() : null;
        String description = ds != null ? ds.description() : null;

        // --- Sensor ---
        String sensorId = sensor != null && !isRecordOnlyField(sensor, "id")
                ? sanitizeId(sensor.id() != null ? sensor.id() : sensor.name())
                : null;
        String sensorName = sensor != null ? sensor.name() : null;
        String sensorDescription = sensor != null ? sensor.description() : null;
        String sensorEncodingType = sensor != null ? sensor.encodingType() : null;
        Object sensorMetadata = sensor != null ? sensor.metadata() : null;
        Map<String, Object> sensorProperties = sensor != null ? sensor.properties() : null;

        // --- ObservedProperty ---
        String observedPropertyId = observedProperty != null && !isRecordOnlyField(observedProperty, "id")
                ? sanitizeId(observedProperty.id() != null ? observedProperty.id() : observedProperty.name())
                : null;
        String observedPropertyName = observedProperty != null ? observedProperty.name() : null;
        String observedPropertyDescription = observedProperty != null ? observedProperty.description() : null;
        String observedPropertyDefinition = observedProperty != null ? observedProperty.definition() : null;
        Map<String, Object> observedPropertyProperties = observedProperty != null ? observedProperty.properties()
                : null;

        // --- Unit ---
        String unitName = unit != null ? unit.name() : null;
        String unitSymbol = unit != null ? unit.symbol() : null;
        String unitDefinition = unit != null ? unit.definition() : null;
        // last observation
        ExpandedObservation obs = lastObservationReceived;
        if (lastObservationReceived != null && lastObservation != null) {

            String observationId = lastObservationReceived.id() != null ? (String) lastObservationReceived.id()
                    : String.format("%s-%d", lastObservationReceived.result().toString(),
                            String.valueOf(Instant.now().toEpochMilli()));
            Instant phenomenonTime = lastObservationReceived.phenomenonTime() != null
                    ? lastObservationReceived.phenomenonTime()
                    : lastObservation.phenomenonTime();
            Instant resultTime = lastObservationReceived.resultTime() != null ? lastObservationReceived.resultTime()
                    : lastObservation.resultTime();
            Object result = lastObservationReceived.result() != null ? lastObservationReceived.result()
                    : lastObservation.result();
            Object resultQuality = lastObservationReceived.resultQuality() != null
                    ? lastObservationReceived.resultQuality()
                    : lastObservation.resultQuality();
            TimeInterval validTime = lastObservationReceived.validTime() != null ? lastObservationReceived.validTime()
                    : lastObservation.validTime();
            Map<String, Object> parameters = lastObservationReceived.parameters() != null
                    ? lastObservationReceived.parameters()
                    : lastObservation.parameters();
            Map<String, Object> properties = lastObservationReceived.properties() != null
                    ? lastObservationReceived.properties()
                    : lastObservation.properties();

            obs = new ExpandedObservation(null, observationId, phenomenonTime, resultTime, result, resultQuality,
                    validTime, parameters, properties, null, null, null, featureOfInterest);
            DtoMapperSimple.checkRequireField(obs);
        }
        // --- Build DatastreamUpdate ---
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, providerId, name, description, timestamp,
                thingId, sensorId, sensorName, sensorDescription, sensorEncodingType, sensorMetadata, sensorProperties,
                observedPropertyId, observedPropertyName, observedPropertyDescription, observedPropertyDefinition,
                observedPropertyProperties, unitName, unitSymbol, unitDefinition, obs);

        return datastreamUpdate;
    }

    public static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    public static boolean isRecordOnlyField(Object record, String idFieldName) {
        return DtoMapperSimple.isRecordOnlyField(record, idFieldName);
    }

    public static Object getRecordField(Object record, String fieldName) {
        return DtoMapperSimple.getRecordField(record, fieldName);
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

    private static Feature toFeature(Location location) {
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

    private static GeoJsonObject aggregate(List<Location> locations) {
        return switch (locations.size()) {
        case 0:
            yield null;
        case 1:
            yield toFeature(locations.get(0));
        default:
            yield new FeatureCollection(locations.stream().map(DtoToModelMapper::toFeature).toList(), null, null);
        };
    }

    public static List<SensorThingsUpdate> toThingUpdates(ExtraUseCaseRequest<ExpandedThing> request, String id,
            List<String> existingLocationIds, List<String> existingDatastreamIds) {
        String providerIdThing = id;
        ExpandedThing thing = request.model();
        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();
        Map<String, Object> thingProperties = null;

        if (thing.properties() != null) {
            thingProperties = thing.properties().entrySet().stream()
                    .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue));
        }
        GeoJsonObject geoLocationAggregate = null;

        if (thing.locations() != null) {
            for (ExpandedLocation l : thing.locations()) {
                String locationId = getLocationId(l);
                if (!isRecordOnlyField(l, "id")) {
                    listUpdate.add(toLocationUpdate(locationId, l));
                }
                existingLocationIds.add(locationId);
            }
            thing.locations().stream().filter(l -> !isRecordOnlyField(l, "id"))
                    .map(l -> toLocationUpdate(getLocationId(l), l)).forEach(listUpdate::add);
            existingLocationIds.addAll(thing.locations().stream().map(l -> getLocationId(l)).toList());
            geoLocationAggregate = getAggregateLocation(request, existingLocationIds);
        }
        if (thing.datastreams() != null) {
            for (ExpandedDataStream ds : thing.datastreams()) {
                String idDatastream = getDatastreamid(ds);
                existingDatastreamIds.add(idDatastream);
                if (ds.observations() != null && ds.observations().size() > 0) {
                    listUpdate.addAll(ds.observations().stream().map(obs -> {
                        return toDatastreamUpdate(idDatastream, providerIdThing, ds, ds.sensor(), ds.observedProperty(),
                                ds.unitOfMeasurement(), obs, obs.featureOfInterest());
                    }).toList());
                } else {
                    listUpdate.add(toDatastreamUpdate(idDatastream, providerIdThing, ds, ds.sensor(),
                            ds.observedProperty(), ds.unitOfMeasurement(), null, null));
                }
            }

        }
        ThingUpdate provider = new ThingUpdate(providerIdThing, geoLocationAggregate, thing.name(), thing.description(),
                providerIdThing, thingProperties, existingLocationIds, existingDatastreamIds);

        listUpdate.add(provider);

        return listUpdate;
    }

    public static GeoJsonObject getAggregateLocation(ExtraUseCaseRequest<?> request, List<String> existingLocationIds) {
        return aggregate(existingLocationIds.stream()
                .map(idLocation -> UtilDto.getProviderSnapshot(request.session(), idLocation))
                .filter(Optional::isPresent).map(Optional::get)
                .map(p -> DtoMapperSimple.toLocation(request.mapper(), p, null)).toList());
    }

    private static String getLocationId(ExpandedLocation l) {
        return DtoToModelMapper.getNewId(l);
    }

    private static String getDatastreamid(ExpandedDataStream l) {
        return DtoToModelMapper.getNewId(l);
    }

    public static List<SensorThingsUpdate> toLocationUpdates(ExpandedLocation location, String id) {
        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();

        String locationId = id != null ? id : sanitizeId(location.id() != null ? location.id() : location.name());

        listUpdate.add(toLocationUpdate(locationId, location));

        return listUpdate;
    }

    public static List<SensorThingsUpdate> toLocationUpdates(ExpandedLocation location) {
        return toLocationUpdates(location, null);
    }

    public static String getProperty(GeoJsonObject location, String propName) {
        if (location instanceof Feature) {
            Feature f = (Feature) location;
            return DtoToModelMapper.toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName)));
        } else if (location instanceof FeatureCollection) {
            FeatureCollection fc = (FeatureCollection) location;
            return fc.features().stream()
                    .map(f -> DtoToModelMapper.toString(Optional.ofNullable(f.properties()).map(m -> m.get(propName))))
                    .filter(p -> p != null).findFirst().orElse(null);
        }
        return null;
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

    /**
     * Ensure the given ID contains a single segment
     */
    public static void validatedProviderId(String id) {
        if (id.contains("~")) {
            throw new BadRequestException("Multi-segments ID found");
        }
    }
}
