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
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
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
import jakarta.ws.rs.core.UriInfo;

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

    /**
     * get id value from a record
     *
     * @param record
     * @return
     */
    public static String getIdFromRecord(Object record) {
        Object id = DtoToModelMapper.getRecordField(record, "id");
        if (id instanceof Map) {
            id = ((Map<?, ?>) id).values().stream().findFirst().get();
        }
        return id instanceof String ? (String) id : null;

    }

    /**
     * get new id for new sensorthing entities
     *
     * @param model
     * @return
     */
    public static String getNewId(Id model) {
        return model.id() != null ? (String) model.id() : getNewId();
    }

    public static String getNewId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * return resource snapshot identified name from service
     *
     * @param service
     * @param resource
     * @return
     */
    public static Optional<? extends ResourceSnapshot> getProviderField(ServiceSnapshot service, String resource) {

        return service.getResources().stream().filter(r -> resource.equals(r.getName())).findFirst();
    }

    /**
     * extract second (part) of id separate by ~
     *
     * @param id
     * @param part
     * @return
     */
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

    /**
     * get first segment of i separate by ~
     *
     * @param id
     * @return
     */
    public static String extractFirstIdSegment(String id) {
        return extractIdSegment(id, 0);
    }

    /**
     * get second part of id separate by ~
     *
     * @param id
     * @return
     */
    public static String extractSecondIdSegment(String id) {
        return extractIdSegment(id, 1);

    }

    /**
     * get third part of id separate by ~
     *
     * @param id
     * @return
     */
    public static String extractThirdIdSegment(String id) {
        return extractIdSegment(id, 2);

    }

    /**
     * get fourth element in the id (separate by ~)
     *
     * @param id
     * @return
     */
    public static String extractFouthIdSegment(String id) {
        return extractIdSegment(id, 3);

    }

    /**
     * get object from service identified by resource name
     *
     * @param service
     * @param resource
     * @return
     */
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

    /**
     * get location update instance for dataupdate
     *
     * @param providerId
     * @param location
     * @return
     */
    public static SensorThingsUpdate toLocationUpdate(String providerId, ExpandedLocation location) {
        return new LocationUpdate(providerId, providerId, location.name(), location.description(),
                location.encodingType(), location.properties(), location.location());
    }

    public static ExpandedDataStream toDatastream(ProviderSnapshot provider) {
        Datastream datastream = DtoMapperSimple.toDatastream(provider, null, null, null, null, null);
        UnitOfMeasurement uom = DtoMapperSimple.toUnitOfMeasure(provider);
        ObservedProperty observedProperty = DtoMapperSimple.toObservedProperty(provider, null, null);
        Sensor sensor = DtoMapperSimple.toSensor(provider, null, null);

        return new ExpandedDataStream(null, datastream.id(), datastream.name(), datastream.description(),
                datastream.observationType(), uom, datastream.observedArea(), datastream.phenomenonTime(),
                datastream.resultTime(), datastream.properties(), null, null, null, null, null, observedProperty,
                sensor, null, null);
    }

    public static ExpandedDataStream toDatastreamOnly(ProviderSnapshot provider) {
        Datastream datastream = DtoMapperSimple.toDatastream(provider, null, null, null, null, null);
        UnitOfMeasurement uom = DtoMapperSimple.toUnitOfMeasure(provider);

        return new ExpandedDataStream(null, datastream.id(), datastream.name(), datastream.description(),
                datastream.observationType(), uom, datastream.observedArea(), datastream.phenomenonTime(),
                datastream.resultTime(), datastream.properties(), null, null, null, null, null, null, null, null, null);
    }

    /**
     * get the datastream update instance for dataUpdater
     *
     * @param providerId
     * @param observedArea
     * @param thingId
     * @param ds
     * @param sensor
     * @param observedProperty
     * @param unit
     * @param lastObservation
     * @param featureOfInterest
     * @return
     * @throws JsonProcessingException
     */
    public static SensorThingsUpdate toDatastreamUpdate(ObjectMapper mapper, String providerId,
            GeoJsonObject observedArea, String thingId, ExpandedDataStream ds, Sensor sensor,
            ObservedProperty observedProperty, UnitOfMeasurement unit, ExpandedObservation lastObservation,
            FeatureOfInterest featureOfInterest) {
        return toDatastreamUpdate(mapper, providerId, observedArea, thingId, ds, sensor, observedProperty, unit,
                lastObservation, lastObservation, featureOfInterest);
    }

    /**
     * Merge a new FOI geometry into the current observedArea.
     *
     * @param currentArea the current observedArea (can be null)
     * @param newFeature  the new FOI feature geometry
     * @return the merged observedArea geometry
     */

    static GeoJsonObject mergeObservedArea(GeoJsonObject existing, GeoJsonObject newGeo) {
        if (existing == null) {
            return newGeo;
        }

        List<Feature> newFeatures = new ArrayList<>();
        if (newGeo instanceof Feature f) {
            newFeatures.add(f);
        } else if (newGeo instanceof FeatureCollection fc) {
            newFeatures.addAll(fc.features());
        } else if (newGeo instanceof Geometry g) {
            newFeatures.add(new Feature(null, g, Map.of(), null, null));
        } else {
            throw new IllegalArgumentException("Unsupported GeoJsonObject type: " + newGeo.getClass());
        }
        // Merge into existing observedArea
        if (existing instanceof FeatureCollection fc) {
            List<Feature> merged = new ArrayList<>(fc.features());
            merged.addAll(newFeatures);
            return new FeatureCollection(merged, fc.bbox(), fc.foreignMembers());
        } else if (existing instanceof Feature f) {
            List<Feature> merged = new ArrayList<>();
            merged.add(f);
            merged.addAll(newFeatures);
            return new FeatureCollection(merged, null, null);
        } else if (existing instanceof Geometry g) {
            // Wrap existing Geometry into a Feature
            List<Feature> merged = new ArrayList<>();
            merged.add(new Feature(null, g, Map.of(), null, null));
            merged.addAll(newFeatures);
            return new FeatureCollection(merged, null, null);
        } else {
            throw new IllegalArgumentException("Unsupported existing observedArea type: " + existing.getClass());
        }
    }

    /**
     * return datastream update object for dataUpdater to update EMF model
     *
     * @param providerId
     * @param thingId
     * @param observedArea
     * @param ds
     * @param sensor
     * @param observedProperty
     * @param unit
     * @param lastObservation
     * @param lastObservationReceived
     * @param featureOfInterest
     * @return
     * @throws JsonProcessingException
     */
    public static SensorThingsUpdate toDatastreamUpdate(ObjectMapper mapper, String providerId,
            GeoJsonObject observedArea, String thingId, ExpandedDataStream ds, Sensor sensor,
            ObservedProperty observedProperty, UnitOfMeasurement unit, ExpandedObservation lastObservation,
            ExpandedObservation lastObservationReceived, FeatureOfInterest featureOfInterest) {

        Instant timestamp = Instant.now();
        String name = ds != null ? ds.name() : null;
        String description = ds != null ? ds.description() : null;
        Map<String, Object> properties = ds != null ? ds.properties() : null;
        // --- Sensor ---
        String sensorId = sensor != null && !isRecordOnlyField(sensor, "id") ? DtoToModelMapper.getNewId(sensor) : null;
        String sensorName = sensor != null ? sensor.name() : null;
        String sensorDescription = sensor != null ? sensor.description() : null;
        String sensorEncodingType = sensor != null ? sensor.encodingType() : null;
        Object sensorMetadata = sensor != null ? sensor.metadata() : null;
        Map<String, Object> sensorProperties = sensor != null ? sensor.properties() : null;

        // --- ObservedProperty ---
        String observedPropertyId = observedProperty != null && !isRecordOnlyField(observedProperty, "id")
                ? DtoToModelMapper.getNewId(observedProperty)
                : null;
        String observedPropertyName = observedProperty != null ? observedProperty.name() : null;
        String observedPropertyDescription = observedProperty != null ? observedProperty.description() : null;
        String observedPropertyDefinition = observedProperty != null ? observedProperty.definition() : null;
        Map<String, Object> observedPropertyProperties = observedProperty != null ? observedProperty.properties()
                : null;

        String observationType = ds != null ? ds.observationType() : null;
        // --- Unit ---
        String unitName = unit != null ? unit.name() : null;
        String unitSymbol = unit != null ? unit.symbol() : null;
        String unitDefinition = unit != null ? unit.definition() : null;
        GeoJsonObject observedAreaToUpdate = observedArea;

        // last observation
        ExpandedObservation obs = lastObservationReceived;
        if (lastObservation != null) {

            String observationId = lastObservationReceived.id() != null ? (String) lastObservationReceived.id()
                    : String.format("%s~%s", providerId, getNewId(obs));
            Instant phenomenonTime = lastObservationReceived.phenomenonTime() != null
                    ? lastObservationReceived.phenomenonTime()
                    : lastObservation.phenomenonTime();
            Instant resultTime = lastObservationReceived.resultTime() != null ? lastObservationReceived.resultTime()
                    : lastObservation.resultTime();
//            if (resultTime == null) {
//                resultTime = Instant.now();
//            }
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
            Map<String, Object> obsProperties = lastObservationReceived.properties() != null
                    ? lastObservationReceived.properties()
                    : lastObservation.properties();

            obs = new ExpandedObservation(null, observationId, phenomenonTime, resultTime, result, resultQuality,
                    validTime, parameters, obsProperties, null, null, null, featureOfInterest, false);
            // DtoMapperSimple.checkRequireField(obs);
            if (featureOfInterest != null && featureOfInterest.feature() != null) {
                observedAreaToUpdate = mergeObservedArea(observedArea, featureOfInterest.feature());
            }

        }
        String obsStr = obs != null ? serializeObservation(mapper, obs) : null;
        // --- Build DatastreamUpdate ---
        DatastreamUpdate datastreamUpdate = new DatastreamUpdate(providerId, providerId, name, description,
                observationType, properties, timestamp, observedAreaToUpdate, thingId, sensorId, sensorName,
                sensorDescription, sensorEncodingType, sensorMetadata, sensorProperties, observedPropertyId,
                observedPropertyName, observedPropertyDescription, observedPropertyDefinition,
                observedPropertyProperties, unitName, unitSymbol, unitDefinition, obsStr);

        return datastreamUpdate;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path) {
        String sensorLink = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).build().toString();
        return sensorLink;
    }

    public static String getLink(UriInfo uriInfo, String baseUri, String path, String id) {
        if (id == null) {
            id = "null";
        }
        String link = uriInfo.getBaseUriBuilder().uri(baseUri).path(path).resolveTemplate("id", id).build().toString();
        return link;
    }

    private static String serializeObservation(ObjectMapper mapper, ExpandedObservation obs) {
        String obsStr;
        try {
            obsStr = mapper.writeValueAsString(obs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return obsStr;
    }

    public static String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static String toString(Optional<?> o) {
        return o == null || o.isEmpty() ? null : String.valueOf(o.get());
    }

    /**
     * return true if the record object is a record and has only the first
     * identified by idFieldName, else False
     *
     * @param record
     * @param idFieldName
     * @return
     */
    public static boolean isRecordOnlyField(Object record, String idFieldName) {
        return DtoMapperSimple.isRecordOnlyField(record, idFieldName);
    }

    /**
     * get record field identified by fieldName from a record object, else null
     *
     * @param record
     * @param fieldName
     * @return
     */
    public static Object getRecordField(Object record, String fieldName) {
        return DtoMapperSimple.getRecordField(record, fieldName);
    }

    /**
     * retrun Feature from FeatureCollection
     *
     * @param fc
     * @return
     */
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

    /**
     * return the Feature GeoJon from the location information in Location
     * sensorthing entity
     *
     * @param location
     * @return
     */
    private static Feature toFeature(Location location) {
        Feature f;

        if (location.location() != null) {
            String id = (String) location.id();
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
                yield new Feature(id, (Geometry) location.location(), null, null, null);
            default:
                throw new IllegalArgumentException("Unknown GeoJSON object " + location.location().type());
            };

        } else {
            f = null;
        }

        return f;
    }

    /**
     * aggregate all location geojson from collection of locations sensorthing
     * entity in a new Geojson
     *
     * @param locations
     * @return
     */
    private static GeoJsonObject aggregate(List<Location> locations) {
        return switch (locations.size()) {
        case 0:
            yield null;
        case 1:

            yield locations.get(0).location();
        default:
            yield new FeatureCollection(locations.stream().map(DtoToModelMapper::toFeature).toList(), null, null);
        };

    }

    /**
     * return the thing update object to be use in dataupdate to update Thing entity
     *
     * @param request
     * @param id
     * @param existingLocationIds
     * @param existingDatastreamIds
     * @return
     */
    public static List<SensorThingsUpdate> toThingUpdates(ExtraUseCaseRequest<ExpandedThing> request, String id,
            List<String> existingLocationIds, List<String> existingDatastreamIds, List<ExpandedDataStream> listDs,
            List<Location> listNewLocation) {
        String providerIdThing = id;
        ExpandedThing thing = request.model();
        Instant timestamp = Instant.now();
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

            geoLocationAggregate = getAggregateLocation(request, existingLocationIds, listNewLocation);
        }
        if (listDs != null) {
            for (ExpandedDataStream ds : listDs) {
                // check require field for datastream + require link

                String idDatastream = getDatastreamid(ds);
                existingDatastreamIds.add(idDatastream);
                if (ds.observations() != null && ds.observations().size() > 0) {
                    final GeoJsonObject feature = geoLocationAggregate != null ? geoLocationAggregate : new Point(0, 0);
                    listUpdate.addAll(ds.observations().stream().map(obs -> {
                        FeatureOfInterest foi = obs.featureOfInterest();
                        if (foi == null) {

                            foi = new FeatureOfInterest(null, DtoToModelMapper.getNewId(), "default",
                                    "default feature of interest", "application/vnd.geo+json", feature, Map.of(), null);
                        }
                        return toDatastreamUpdate(request.mapper(), idDatastream, null, providerIdThing, ds,
                                ds.sensor(), ds.observedProperty(), ds.unitOfMeasurement(), obs, foi);
                    }).toList());
                } else {
                    listUpdate.add(toDatastreamUpdate(request.mapper(), idDatastream, null, providerIdThing, ds,
                            ds.sensor(), ds.observedProperty(), ds.unitOfMeasurement(), null, null, null));
                }
            }

        }

        ThingUpdate provider = new ThingUpdate(providerIdThing, timestamp, geoLocationAggregate, thing.name(),
                thing.description(), providerIdThing, thingProperties, existingLocationIds, existingDatastreamIds);

        listUpdate.add(provider);

        return listUpdate;
    }

    /**
     * aggregate location geojson from list of location that is identified by a list
     * of id in existingLocationIds
     *
     * @param request
     * @param existingLocationIds
     * @return
     */
    public static GeoJsonObject getAggregateLocation(ExtraUseCaseRequest<?> request, List<String> existingLocationIds,
            List<Location> locationsInLine) {
        Stream<Location> existingLocationsStream = existingLocationIds.stream()
                .map(idLocation -> UtilDto.getProviderSnapshot(request.session(), idLocation))
                .filter(Optional::isPresent).map(Optional::get)
                .map(p -> DtoMapperSimple.toLocation(request.mapper(), p, null, null, null));
        List<Location> list = new ArrayList<Location>();
        List<Location> existingLocations = existingLocationsStream.toList();
        list.addAll(existingLocations);
        list.addAll(locationsInLine.stream().filter(l -> !existingLocations.stream()
                .map(el -> el.location().toJsonString()).toList().contains(l.location().toJsonString())).toList());
        return aggregate(list);
    }

    /**
     * get new id for location if not exists
     *
     * @param l
     * @return
     */
    private static String getLocationId(ExpandedLocation l) {
        return DtoToModelMapper.getNewId(l);
    }

    /**
     * get new id for datastream if not exists
     *
     * @param l
     * @return
     */
    private static String getDatastreamid(ExpandedDataStream l) {
        return DtoToModelMapper.getNewId(l);
    }

    /**
     * return location update object to update using data update the Location
     * sensorthing Entity
     *
     * @param location
     * @param id
     * @return
     */
    public static List<SensorThingsUpdate> toLocationUpdates(ExpandedLocation location, String id) {
        List<SensorThingsUpdate> listUpdate = new ArrayList<SensorThingsUpdate>();

        String locationId = id != null ? id
                : sanitizeId(location.id() != null ? location.id() : DtoToModelMapper.getNewId());

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
