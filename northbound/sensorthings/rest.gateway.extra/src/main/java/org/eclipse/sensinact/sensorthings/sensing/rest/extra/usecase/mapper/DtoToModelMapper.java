package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import static java.util.stream.Collectors.toMap;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.DatastreamUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.LocationUpdate;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.update.ThingUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;

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

    public static void checkRequireField(Id dto) {
        if (dto instanceof ExpandedThing) {
            checkRequireField((ExpandedThing) dto);
        } else if (dto instanceof ExpandedObservation) {
            checkRequireField((ExpandedObservation) dto);
        } else if (dto instanceof ExpandedLocation) {
            checkRequireField((ExpandedLocation) dto);
        } else if (dto instanceof ExpandedObservedProperty) {
            checkRequireField((ExpandedObservedProperty) dto);
        } else if (dto instanceof ExpandedSensor) {
            checkRequireField((ExpandedSensor) dto);
        } else if (dto instanceof ExpandedDataStream) {
            checkRequireField((ExpandedDataStream) dto);
        }
    }

    public static void checkRequireField(ExpandedSensor dto) {
        if (dto == null) {
            throw new BadRequestException("sensor not found in  Payload");
        }
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Payload");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  Payload");
        }
        if (dto.metadata() == null) {
            throw new BadRequestException("metadata not found in  Payload");
        }

    }

    public static void checkRequireField(ExpandedObservedProperty dto) {
        if (dto == null) {
            throw new BadRequestException("ObservedProperty not found in  Payload");
        }
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Payload");
        }
        if (dto.definition() == null) {
            throw new BadRequestException("definition not found in  Payload");
        }

    }

    public static void checkRequireField(ExpandedObservation dto) {
        if (dto.result() == null) {
            throw new BadRequestException("result not found in  Payload");
        }
        if (dto.phenomenonTime() == null) {
            throw new BadRequestException("phenomenonTime not found in  Payload");
        }

    }

    public static void checkRequireField(ExpandedThing dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Payload");
        }

    }

    public static void checkRequireField(ExpandedLocation dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Payload");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  Payload");
        }
        if (dto.location() == null) {
            throw new BadRequestException("location not found in  Payload");
        }

    }

    public static void checkRequireField(FeatureOfInterest dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Payload");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  Payload");
        }
        if (dto.feature() == null) {
            throw new BadRequestException("feature ot found in  Payload");
        }

    }

    public static void checkRequireField(ExpandedDataStream datastream) {
        if (datastream.name() == null) {
            throw new BadRequestException("name not found in  Payload");
        }
        if (datastream.description() == null) {
            throw new BadRequestException("description not found in  Payload");
        }
        if (datastream.unitOfMeasurement() == null) {
            throw new BadRequestException("unit Of Measure not found in  Payload");
        }
        if (datastream.observationType() == null) {
            throw new BadRequestException("observationType not found in  Payload");
        }

    }

    public static void checkRequireLink(Object... links) {
        for (int i = 0; i < links.length; i++) {
            Object link = links[i];
            if (link == null) {
                throw new BadRequestException("linked entity is required");
            }
        }

    }

    public static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider,
            String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> ADMIN.equals(s.getName())).findFirst()
                .get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
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
        return extractIdSegment(id, 1);

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

    public static SensorThingsUpdate toObservedPropertyUpdate(String providerId, ExpandedDataStream ds,
            ExpandedObservedProperty observedProperty) {
        return toDatastreamUpdate(providerId, ds, null, observedProperty, null, null, null);
    }

    public static SensorThingsUpdate toSensorUpdate(String providerId, ExpandedDataStream ds, ExpandedSensor sensor) {
        return toDatastreamUpdate(providerId, ds, sensor, null, null, null, null);

    }

    public static SensorThingsUpdate toFeatureOfInterestUpdate(String providerId, String datastreamId,
            FeatureOfInterest featureOfInterest) {
        return null;
    }

    public static SensorThingsUpdate toUnitOfMeasureUpdate(String providerId, ExpandedDataStream ds,
            UnitOfMeasurement unitOfMeasure) {
        return toDatastreamUpdate(providerId, ds, null, null, unitOfMeasure, null, null);
    }

    public static SensorThingsUpdate toLocationUpdate(String providerId, ExpandedLocation location) {
        return new LocationUpdate(providerId, providerId, location.name(), location.description(),
                location.encodingType(), location.location());
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, String datastreamId, ExpandedSensor sensor) {
        return toDatastreamUpdate(providerId, datastreamId, null, sensor, null, null, null, null);
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, String datastreamId, ExpandedSensor sensor,
            ExpandedObservedProperty observedProperty, UnitOfMeasurement unit, ExpandedObservation lastObservation,
            FeatureOfInterest featureOfInterest) {
        return toDatastreamUpdate(providerId, datastreamId, null, sensor, observedProperty, unit, lastObservation,
                featureOfInterest);
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, ExpandedDataStream ds, ExpandedSensor sensor,
            ExpandedObservedProperty observedProperty, UnitOfMeasurement unit, ExpandedObservation lastObservation,
            FeatureOfInterest featureOfInterest) {
        String datastreamId = sanitizeId(ds.id() != null ? ds.id() : ds.name());
        return toDatastreamUpdate(providerId, datastreamId, ds, sensor, observedProperty, unit, lastObservation,
                featureOfInterest);
    }

    public static SensorThingsUpdate toDatastreamUpdate(String providerId, String datastreamId, ExpandedDataStream ds,
            ExpandedSensor sensor, ExpandedObservedProperty observedProperty, UnitOfMeasurement unit,
            ExpandedObservation lastObservation, FeatureOfInterest featureOfInterest) {

        Instant timestamp = lastObservation != null ? lastObservation.phenomenonTime() : Instant.now();

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

        // --- Last Observation ---
        String observationId = lastObservation != null ? sanitizeId(lastObservation.result()) : null;
        Object observationResult = lastObservation != null ? lastObservation.result() : null;
        Instant observationPhenomenonTime = lastObservation != null ? lastObservation.phenomenonTime() : null;
        Instant observationResultTime = lastObservation != null ? lastObservation.resultTime() : null;
        TimeInterval observationValidTime = lastObservation != null ? lastObservation.validTime() : null;
        Object observationResultQuality = lastObservation != null ? lastObservation.resultQuality() : null;
        Map<String, Object> observationParameters = lastObservation != null ? lastObservation.parameters() : null;
        Map<String, Object> observationProperties = lastObservation != null ? lastObservation.properties() : null;

        // --- FeatureOfInterest ---
        String foiId = featureOfInterest != null
                ? sanitizeId(
                        featureOfInterest.id() != null ? (String) featureOfInterest.id() : featureOfInterest.name())
                : null;
        String foiName = featureOfInterest != null ? featureOfInterest.name() : null;
        String foiEncodingType = featureOfInterest != null ? featureOfInterest.encodingType() : null;
        String foiDescription = featureOfInterest != null ? featureOfInterest.description() : null;

        GeoJsonObject foiFeature = featureOfInterest != null ? featureOfInterest.feature() : null;

        // --- Build DatastreamUpdate ---
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, datastreamId, // serviceName
                datastreamId, name, description, timestamp, sensorId, sensorName, sensorDescription, sensorEncodingType,
                sensorMetadata, sensorProperties, observedPropertyId, observedPropertyName, observedPropertyDescription,
                observedPropertyDefinition, observedPropertyProperties, unitName, unitSymbol, unitDefinition,
                observationId, observationResult, observationPhenomenonTime, observationResultTime,
                observationValidTime, observationResultQuality, observationParameters, observationProperties, foiId,
                foiName, foiDescription, foiEncodingType, foiFeature);

        return datastreamUpdate;
    }

    public static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    public static boolean isRecordOnlyField(Object record, String idFieldName) {
        if (record == null || !record.getClass().isRecord()) {
            return false;
        }

        RecordComponent[] components = record.getClass().getRecordComponents();

        return Arrays.stream(components).allMatch(rc -> {
            try {
                Object value = rc.getAccessor().invoke(record);
                if (rc.getName().equals(idFieldName)) {
                    return value != null;
                } else {
                    return value == null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Object getRecordField(Object record, String fieldName) {
        if (!record.getClass().isRecord()) {
            throw new IllegalArgumentException("Ce n'est pas un record !");
        }

        RecordComponent[] components = record.getClass().getRecordComponents();

        for (RecordComponent rc : components) {
            if (rc.getName().equals(fieldName)) {
                try {
                    Object value = rc.getAccessor().invoke(record);

                    return value;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static List<SensorThingsUpdate> toThingUpdates(ExpandedThing thing, List<String> existingLocationIds) {
        return toThingUpdates(thing, null, existingLocationIds);
    }

    public static List<SensorThingsUpdate> toThingUpdates(ExpandedThing thing, String id,
            List<String> existingLocationIds) {
        String providerIdThing = id != null ? id : sanitizeId(thing.name() == null ? thing.id() : thing.name());

        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();
        Map<String, Object> thingProperties = null;

        if (thing.properties() != null) {
            thingProperties = thing.properties().entrySet().stream()
                    .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue));
        }
        if (thing.locations() != null) {
            thing.locations().stream().filter(l -> !isRecordOnlyField(l, "id"))
                    .map(l -> toLocationUpdate(getLocationId(l), l)).forEach(listUpdate::add);
            existingLocationIds.addAll(thing.locations().stream().map(l -> getLocationId(l)).toList());
        }
        if (thing.datastreams() != null) {
            for (ExpandedDataStream ds : thing.datastreams()) {
                if (ds.observations() != null && ds.observations().size() > 0) {
                    listUpdate.addAll(ds
                            .observations().stream().map(obs -> toDatastreamUpdate(providerIdThing, ds, ds.sensor(),
                                    ds.observedProperty(), ds.unitOfMeasurement(), obs, obs.featureOfInterest()))
                            .toList());
                } else {
                    listUpdate.add(toDatastreamUpdate(providerIdThing, ds, ds.sensor(), ds.observedProperty(),
                            ds.unitOfMeasurement(), null, null));
                }
            }

        }
        ThingUpdate provider = new ThingUpdate(providerIdThing, thing.name(), thing.description(), providerIdThing,
                thingProperties, existingLocationIds);

        listUpdate.add(provider);

        return listUpdate;
    }

    private static String getLocationId(ExpandedLocation l) {
        return sanitizeId(l.id() != null ? (String) l.id() : l.name());
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
        final TimedValue<GeoJsonObject> rcLocation = DtoToModelMapper.getLocation(provider, mapper, false);
        final Instant time = rcLocation.getTimestamp();
        final GeoJsonObject object = rcLocation.getValue();

        String id = String.format("%s~%s", providerName, Long.toString(time.toEpochMilli(), 16));

        String name = Objects.requireNonNullElse(getProperty(object, "name"), providerName);

        String description = Objects.requireNonNullElse(getProperty(object, DtoToModelMapper.DESCRIPTION),
                DtoToModelMapper.NO_DESCRIPTION);

        String selfLink = uriInfo.getBaseUriBuilder().path(DtoToModelMapper.VERSION).path("Locations({id})")
                .resolveTemplate("id", id).build().toString();
        String thingsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Things").build().toString();
        String historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("HistoricalLocations").build()
                .toString();

        return new ExpandedLocation(selfLink, id, name, description, DtoToModelMapper.ENCODING_TYPE_VND_GEO_JSON,
                object, thingsLink, historicalLocationsLink, null);
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
