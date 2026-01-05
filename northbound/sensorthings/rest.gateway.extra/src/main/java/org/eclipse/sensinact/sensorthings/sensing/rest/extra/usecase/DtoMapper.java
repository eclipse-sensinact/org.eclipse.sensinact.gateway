package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.ArrayList;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
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

    public static Optional<? extends ResourceSnapshot> getProviderAdminField(ProviderSnapshot provider,
            String resource) {
        ServiceSnapshot adminSvc = provider.getServices().stream().filter(s -> ADMIN.equals(s.getName())).findFirst()
                .get();
        return adminSvc.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
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

    public static SensorThingsUpdate toObservedPropertyUpdate(String providerId, String datastreamId, String idSensor,
            ExpandedObservedProperty observedProperty) {
        String serviceName = datastreamId;

        String sensorName = null;
        String sensorDescription = null;
        String sensorEncodingType = null;

        String observedPropertyName = null;
        String observedPropertyDescription = null;
        String observedPropertyDefinition = null;
        String observedPropertyId = null;

        String UnitName = null;
        String UnitDefinition = null;
        String UnitSymbol = null;

        if (observedProperty != null) {
            observedPropertyName = observedProperty.name();
            observedPropertyDescription = observedProperty.description();
            observedPropertyDefinition = observedProperty.definition();
            observedPropertyId = (String) observedProperty.id();
        }
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, serviceName, datastreamId, serviceName,
                observedPropertyDescription, null, sensorName, sensorDescription, sensorEncodingType,
                observedPropertyId, observedPropertyName, observedPropertyDescription, observedPropertyDefinition,
                UnitName, UnitSymbol, UnitDefinition);
        return datastreamUpdate;
    }

    public static SensorThingsUpdate toSensorUpdate(String providerId, String datastreamId, ExpandedSensor sensor) {
        String serviceName = datastreamId;

        String sensorName = null;
        String sensorDescription = null;
        String sensorEncodingType = null;

        String observedPropertyName = null;
        String observedPropertyDescription = null;
        String observedPropertyDefinition = null;
        String observedPropertyId = null;

        String UnitName = null;
        String UnitDefinition = null;
        String UnitSymbol = null;

        if (sensor != null) {
            sensorName = sensor.name();
            sensorDescription = sensor.description();
            sensorEncodingType = sensor.encodingType();
        }
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, serviceName, datastreamId, serviceName,
                observedPropertyDescription, null, sensorName, sensorDescription, sensorEncodingType,
                observedPropertyId, observedPropertyName, observedPropertyDescription, observedPropertyDefinition,
                UnitName, UnitSymbol, UnitDefinition);
        return datastreamUpdate;
    }

    public static SensorThingsUpdate toFeatureOfInterestUpdate(String providerId, String datastreamId,
            FeatureOfInterest featureOfInterest) {
        return null;
    }

    public static SensorThingsUpdate toUnitOfMeasureUpdate(String providerId, String datastreamId,
            UnitOfMeasurement unitOfMeasure) {
        String serviceName = datastreamId;

        String sensorName = null;
        String sensorDescription = null;
        String sensorEncodingType = null;

        String observedPropertyName = null;
        String observedPropertyDescription = null;
        String observedPropertyDefinition = null;
        String observedPropertyId = null;

        String UnitName = null;
        String UnitDefinition = null;
        String UnitSymbol = null;

        if (unitOfMeasure != null) {
            UnitName = unitOfMeasure.name();
            UnitDefinition = unitOfMeasure.definition();
            UnitSymbol = unitOfMeasure.symbol();
        }
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, serviceName, datastreamId, serviceName,
                observedPropertyDescription, null, sensorName, sensorDescription, sensorEncodingType,
                observedPropertyId, observedPropertyName, observedPropertyDescription, observedPropertyDefinition,
                UnitName, UnitSymbol, UnitDefinition);
        return datastreamUpdate;
    }

    public static SensorThingsUpdate toLocationUpdate(String providerId, ExpandedLocation location) {
        String idLocation = sanitizeId(location.id() == null ? location.name() : location.id());
        return new LocationUpdate(providerId, idLocation, location.name(), location.description(),
                location.encodingType(), location.location());
    }

    public static List<SensorThingsUpdate> toDatastreamUpdate(String providerId, ExpandedDataStream ds) {
        String serviceName = sanitizeId(ds.id() != null ? ds.id() : ds.name());
        String datastreamId = sanitizeId(ds.id() != null ? ds.id() : ds.name());
        new ArrayList<SensorThingsUpdate>();
        Instant timestamp = ds.observations() != null && ds.observations().size() > 0
                ? ds.observations().get(0).phenomenonTime()
                : Instant.now();
        String sensorName = null;
        String sensorDescription = null;
        String sensorEncodingType = null;
        if (ds.sensor() != null) {
            sensorName = ds.sensor().name();
            sensorDescription = ds.sensor().description();
            sensorEncodingType = ds.sensor().encodingType();
            ds.sensor().properties();
        }
        String observedPropertyName = null;
        String observedPropertyDescription = null;
        String observedPropertyDefinition = null;
        String observedPropertyId = null;
        if (ds.observedProperty() != null) {
            observedPropertyName = ds.observedProperty().name();
            observedPropertyDescription = ds.observedProperty().description();
            observedPropertyDefinition = ds.observedProperty().definition();
            observedPropertyId = (String) ds.observedProperty().id();
        }
        String UnitName = null;
        String UnitDefinition = null;
        String UnitSymbol = null;

        if (ds.unitOfMeasurement() != null) {
            UnitName = ds.unitOfMeasurement().name();
            UnitDefinition = ds.unitOfMeasurement().definition();
            UnitSymbol = ds.unitOfMeasurement().symbol();
        }
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, serviceName, datastreamId, ds.name(),
                ds.description(), timestamp, sensorName, sensorDescription, sensorEncodingType, observedPropertyId,
                observedPropertyName, observedPropertyDescription, observedPropertyDefinition, UnitName, UnitSymbol,
                UnitDefinition);
        return List.of(datastreamUpdate);
    }

    public static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    public static List<SensorThingsUpdate> toUpdates(ExpandedThing thing) {
        String providerIdThing = sanitizeId(thing.name() == null ? thing.id() : thing.name());

        Object id = thing.id() == null ? providerIdThing : thing.id();
        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();
        Map<String, Object> thingProperties = null;

        List<String> locationIds = new ArrayList<String>();
        if (thing.properties() != null) {
            thingProperties = thing.properties().entrySet().stream()
                    .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue));
        }
        if (thing.locations() != null) {
            listUpdate.addAll(thing.locations().stream().map(l -> toLocationUpdate(getLocationId(l), l)).toList());
            locationIds = thing.locations().stream().map(l -> getLocationId(l)).toList();
        }
        if (thing.datastreams() != null) {
            listUpdate.addAll(thing.datastreams().stream().map(d -> toDatastreamUpdate(providerIdThing, d))
                    .flatMap(List::stream).toList());
        }
        ThingUpdate provider = new ThingUpdate(providerIdThing, thing.name(), thing.description(), id, thingProperties,
                locationIds);

        listUpdate.add(provider);

        return listUpdate;
    }

    private static String getLocationId(ExpandedLocation l) {
        return sanitizeId(l.id() != null ? (String) l.id() : l.name());
    }

    public static List<SensorThingsUpdate> toThingUpdates(ExpandedLocation location, Object thingId) {
        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();
        List<String> locationIds = List.of(location.id() != null ? (String) location.id() : location.name());

        if (thingId != null) {
            listUpdate.add(toLocationUpdate((String) thingId, location));
        }
        if (location.things() != null) {
            listUpdate.addAll(location.things().stream().map(thing -> new ThingUpdate((String) thing.id(), // providerId
                    null, null, thing.id(), null, locationIds)).toList());
            listUpdate.addAll(
                    location.things().stream().map(thing -> toLocationUpdate((String) thing.id(), location)).toList());
        }
        return listUpdate;
    }

    public static List<SensorThingsUpdate> toThingUpdates(ExpandedLocation location) {
        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();
        listUpdate.addAll(location.things().stream().map(thing -> toThingUpdates(location, thing.id()))
                .flatMap(List::stream).toList());
        return listUpdate;
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
