/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.dto.util;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
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
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;

public class DtoMapperSimple {

    private static final String DESCRIPTION = "description";
    private static final String FRIENDLY_NAME = "friendlyName";
    public static final String LOCATION = "location";
    private static final String ENCODING_TYPE_VND_GEO_JSON = "application/vnd.geo+json";
    public static final String VERSION = "v1.1";
    private static final String NO_DESCRIPTION = "No description";

    public static String SERVICE_DATASTREAM = "datastream";
    public static String SERVICE_THING = "thing";
    public static String SERVICE_ADMIN = "admin";

    public static String SERVICE_LOCATON = "location";

    private static String getLink(UriInfo uriInfo, String baseUri, String path) {
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

    public static void checkRequireField(Sensor dto) {
        if (dto == null) {
            throw new BadRequestException("sensor not found in  Sensor");
        }
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Sensor");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  Sensor");
        }

    }

    public static void checkRequireField(ObservedProperty dto) {
        if (dto == null) {
            throw new BadRequestException("ObservedProperty not found in  ObservedProperty");
        }
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  ObservedProperty");
        }
        if (dto.definition() == null) {
            throw new BadRequestException("definition not found in  ObservedProperty");
        }

    }

    public static void checkRequireField(ExpandedObservation dto) {
        if (dto.result() == null) {
            throw new BadRequestException("result not found in  ExpandedObservation");
        }
        if (dto.phenomenonTime() == null) {
            throw new BadRequestException("phenomenonTime not found in  ExpandedObservation");
        }

    }

    public static void checkRequireField(Observation dto) {
        if (dto.result() == null) {
            throw new BadRequestException("result not found in  Observation");
        }
        if (dto.phenomenonTime() == null) {
            throw new BadRequestException("phenomenonTime not found in  Observation");
        }

    }

    public static void checkRequireField(ExpandedThing dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Thing");
        }

    }

    public static void checkRequireField(Thing dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Thing");
        }

    }

    public static void checkRequireField(Location dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Location");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  Location");
        }
        if (dto.location() == null) {
            throw new BadRequestException("location not found in  Location");
        }

    }

    public static void checkRequireField(ExpandedLocation dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  Location");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  Location");
        }
        if (dto.location() == null) {
            throw new BadRequestException("location not found in  Location");
        }

    }

    public static void checkRequireField(FeatureOfInterest dto) {
        if (dto.name() == null) {
            throw new BadRequestException("name not found in  FeatureOfInterest");
        }
        if (dto.encodingType() == null) {
            throw new BadRequestException("encodingType not found in  FeatureOfInterest");
        }
        if (dto.feature() == null) {
            throw new BadRequestException("feature not found in  FeatureOfInterest");
        }

    }

    public static void checkRequireField(ExpandedDataStream datastream) {
        if (datastream.name() == null) {
            throw new BadRequestException("name not found in  Datastream");
        }

        if (datastream.unitOfMeasurement() == null) {
            throw new BadRequestException("unit Of Measure not found in  Datastream");
        }
        if (datastream.observationType() == null) {
            throw new BadRequestException("observationType not found in  Datastream");
        }

    }

    public static void checkRequireField(Datastream datastream) {
        if (datastream.name() == null) {
            throw new BadRequestException("name not found in  Datastream");
        }

        if (datastream.unitOfMeasurement() == null) {
            throw new BadRequestException("unit Of Measure not found in  Datastream");
        }
        if (datastream.observationType() == null) {
            throw new BadRequestException("observationType not found in  Datastream");
        }

    }

    public static <T> void checkRequireField(T dto) {
        if (dto == null) {
            throw new BadRequestException("dto null");
        } else if (dto instanceof Sensor dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof ExpandedDataStream dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof ExpandedLocation dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof ExpandedThing dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof ExpandedObservation dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof ObservedProperty dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof UnitOfMeasurement dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof Observation dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof Thing dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof Location dtoCasted) {
            checkRequireField(dtoCasted);
        } else if (dto instanceof FeatureOfInterest dtoCasted) {
            checkRequireField(dtoCasted);
        } else {
            throw new BadRequestException("no dto managed");

        }
    }

    public static void checkRequireField(UnitOfMeasurement unit) {
        if (unit.name() == null) {
            throw new BadRequestException("name not found in  UnitOfMeasurement");
        }
        if (unit.definition() == null) {
            throw new BadRequestException("definition not found in  UnitOfMeasurement");
        }
        if (unit.symbol() == null) {
            throw new BadRequestException("symbol not found in  UnitOfMeasurement");
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

    /**
     * get datastream service
     */
    public static ServiceSnapshot getDatastreamService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(SERVICE_DATASTREAM);
    }

    /**
     * get location service
     */
    public static ServiceSnapshot getLocationService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(SERVICE_LOCATON);
    }

    /**
     * get thing device service
     */
    public static ServiceSnapshot getThingService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(SERVICE_THING);
    }

    public static ServiceSnapshot getAdminService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(SERVICE_ADMIN);
    }

    /**
     * return false if the class is not a record or the field doesn't exists else
     * true
     *
     * @param record
     * @param idFieldName
     * @return
     */
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

    /**
     * return exception if record is not a record class else return the value of
     * field if it exists else null
     *
     * @param record
     * @param fieldName
     * @return
     *
     */
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

    public static UnitOfMeasurement toUnitOfMeasure(ProviderSnapshot provider) {
        ServiceSnapshot service = getDatastreamService(provider);
        if (service == null) {
            throw new NotFoundException();
        }

        String unitName = getResourceField(service, "unitName", String.class);
        String unitSymbol = getResourceField(service, "unitSymbol", String.class);
        String unitDefinition = getResourceField(service, "unitDefinition", String.class);

        UnitOfMeasurement unit = new UnitOfMeasurement(unitName, unitSymbol, unitDefinition);
//        DtoMapperSimple.checkRequireField(unit);
        return unit;
    }

    public static HistoricalLocation toHistoricalLocation(ProviderSnapshot provider, Optional<TimedValue<?>> t,
            UriInfo uriInfo) {
        final Instant time = t.map(TimedValue::getTimestamp).orElse(Instant.EPOCH);

        String id = String.format("%s~%s", provider.getName(), Long.toString(time.toEpochMilli(), 16));
        String selfLink = null;
        String thingLink = null;
        String locationsLink = null;
        if (uriInfo != null) {
            selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("HistoricalLocations({id})")
                    .resolveTemplate("id", id).build().toString();
            thingLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Thing").build().toString();
            locationsLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Locations").build().toString();
        }
        return new HistoricalLocation(selfLink, id, time, locationsLink, thingLink);

    }

    public static FeatureOfInterest toFeatureOfInterest(ProviderSnapshot provider, ExpandedObservation lastObservation,
            UriInfo uriInfo) {
        ServiceSnapshot serviceSnapshot = getDatastreamService(provider);
        String idDatastream = getResourceField(serviceSnapshot, "id", String.class);

        FeatureOfInterest foiReaded = lastObservation.featureOfInterest();
        String foiId = String.format("%s~%s~%s", idDatastream, lastObservation.id(), foiReaded.id());
        String selfLink = null;
        String observationLink = null;
        if (uriInfo != null) {

            selfLink = getLink(uriInfo, VERSION, "FeaturesOfInterest({id})", foiId);
            observationLink = getLink(uriInfo, selfLink, "Observations");
        }
        FeatureOfInterest foi = new FeatureOfInterest(selfLink, foiId, foiReaded.name(), foiReaded.description(),
                foiReaded.encodingType(), foiReaded.feature(), observationLink);

        DtoMapperSimple.checkRequireField(foi);

        return foi;
    }

    public static ObservedProperty toObservedProperty(ProviderSnapshot provider, UriInfo uriInfo) {
        String datastreamId = provider.getName();
        ServiceSnapshot service = getDatastreamService(provider);

        String observedPropertyId = getResourceField(service, "observedPropertyId", String.class);
        String id = String.format("%s~%s", datastreamId, observedPropertyId);
        String observedPropertyName = getResourceField(service, "observedPropertyName", String.class);
        String observedPropertyDescription = getResourceField(service, "observedPropertyDescription", String.class);
        String observedPropertyDefinition = getResourceField(service, "observedPropertyDefinition", String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> observedPropertyProperty = getResourceField(service, "observedPropertyProperties",
                Map.class);
        String observedPropertyLink = null;
        String datastreamLink = null;
        if (uriInfo != null) {
            observedPropertyLink = getLink(uriInfo, VERSION, "/ObservedProperties({id})", id);
            datastreamLink = getLink(uriInfo, observedPropertyLink, "Datastreams");
        }
        ObservedProperty observedProperty = new ObservedProperty(observedPropertyLink, id, observedPropertyName,
                observedPropertyDescription, observedPropertyDefinition, observedPropertyProperty, datastreamLink);

        DtoMapperSimple.checkRequireField(observedProperty);
        return observedProperty;
    }

    public static Sensor toSensor(ProviderSnapshot provider, UriInfo uriInfo) {
        ServiceSnapshot service = getDatastreamService(provider);
        String sensorId = String.format("%s~%s", provider.getName(),
                getResourceField(service, "sensorId", String.class));
        String sensorName = getResourceField(service, "sensorName", String.class);
        String sensorDescription = getResourceField(service, "sensorDescription", String.class);
        String sensorEncodingType = getResourceField(service, "sensorEncodingType", String.class);
        Object sensorMetadata = getResourceField(service, "sensorMetadata", Object.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> sensorProperty = getResourceField(service, "sensorProperty", Map.class);
        String sensorLink = null;
        String datastreamLink = null;
        if (uriInfo != null) {
            sensorLink = getLink(uriInfo, VERSION, "/Sensors({id})", sensorId);
            datastreamLink = getLink(uriInfo, sensorLink, "Datastreams");
        }
        Sensor sensor = new Sensor(sensorLink, sensorId, sensorName, sensorDescription, sensorEncodingType,
                sensorMetadata, sensorProperty, datastreamLink);

        DtoMapperSimple.checkRequireField(sensor);

        return sensor;
    }

    public static Thing toThing(ProviderSnapshot provider, UriInfo uriInfo) {
        String id = provider.getName();
        String name = getResourceField(getAdminService(provider), FRIENDLY_NAME, String.class);
        String description = getResourceField(getAdminService(provider), DESCRIPTION, String.class);
        String selfLink = null;
        String datastreamsLink = null;
        String historicalLocationsLink = null;
        String locationsLink = null;
        if (uriInfo != null) {
            selfLink = getLink(uriInfo, VERSION, "Things({id})", provider.getName());
            datastreamsLink = getLink(uriInfo, selfLink, "Datastreams");
            historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
            locationsLink = getLink(uriInfo, selfLink, "Locations");
        }
        Thing thing = new Thing(selfLink, id, name, description, null, datastreamsLink, historicalLocationsLink,
                locationsLink);

        DtoMapperSimple.checkRequireField(thing);

        return thing;
    }

    public static Observation toObservation(ResourceSnapshot resource, UriInfo uriInfo) {
        return toObservation(resource.getService().getProvider().getName(), null, uriInfo);
    }

    public static TimedValue<GeoJsonObject> getLocation(ProviderSnapshot provider, ObjectMapper mapper,
            boolean allowNull) {
        final ResourceSnapshot locationResource = getAdminService(provider).getResource(LOCATION);

        final Instant time;
        final Object rawValue;
        if (locationResource == null) {
            time = Instant.EPOCH;
            rawValue = null;
        } else {
            final TimedValue<?> timedValue = locationResource.getValue();
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

    public static Location toLocation(ObjectMapper mapper, ProviderSnapshot provider, UriInfo uriInfo) {
        final TimedValue<GeoJsonObject> rcLocation = getLocation(provider, mapper, false);
        final GeoJsonObject object = rcLocation.getValue();

        String id = getResourceField(getLocationService(provider), "id", String.class);

        String name = Objects
                .requireNonNullElse(getResourceField(getAdminService(provider), FRIENDLY_NAME, String.class), "");

        String description = Objects.requireNonNullElse(
                getResourceField(getAdminService(provider), DESCRIPTION, String.class), NO_DESCRIPTION);
        String selfLink = null;
        String thingsLink = null;
        String historicalLocationsLink = null;
        if (uriInfo != null) {
            selfLink = getLink(uriInfo, VERSION, "Locations({id})", provider.getName());
            thingsLink = getLink(uriInfo, selfLink, "Things");
            historicalLocationsLink = getLink(uriInfo, selfLink, "HistoricalLocations");
        }
        Location location = new Location(selfLink, id, name, description, ENCODING_TYPE_VND_GEO_JSON, object,
                thingsLink, historicalLocationsLink);
        if (uriInfo != null) {
            DtoMapperSimple.checkRequireField(location);
        }
        return location;
    }

    public static Observation toObservation(String providerId, TimedValue<?> timeValue, UriInfo uriInfo) {

        TimedValue<?> t = timeValue;

        final Instant timestamp = t.getTimestamp();
        Object obs = t.getValue();
        if (obs != null && obs instanceof ExpandedObservation) {
            ExpandedObservation readObs = (ExpandedObservation) obs;
            String id = String.format("%s~%s~%s", providerId, readObs.id(),
                    Long.toString(timestamp.toEpochMilli(), 16));
            String selfLink = null;
            String datastreamLink = null;
            String featureOfInterestLink = null;

            if (uriInfo != null) {
                selfLink = uriInfo.getBaseUriBuilder().path(VERSION).path("Observations({id})")
                        .resolveTemplate("id", id).build().toString();
                datastreamLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("Datastream").build().toString();
                featureOfInterestLink = uriInfo.getBaseUriBuilder().uri(selfLink).path("FeatureOfInterest").build()
                        .toString();
            }

            Observation observation = new Observation(selfLink, id, readObs.phenomenonTime(), readObs.resultTime(),
                    readObs.result(), readObs.resultQuality(), readObs.validTime(), readObs.parameters(),
                    datastreamLink, featureOfInterestLink);
            if (uriInfo != null) {
                DtoMapperSimple.checkRequireField(observation);
                DtoMapperSimple.checkRequireLink(readObs.featureOfInterest());
            }
            return observation;

        }
        return null;
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

    public static Datastream toDatastream(ProviderSnapshot provider, UriInfo uriInfo) {
        String id = provider.getName();

        String name = getResourceField(getAdminService(provider), FRIENDLY_NAME, String.class);
        String description = getResourceField(getAdminService(provider), DESCRIPTION, String.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = getResourceField(getDatastreamService(provider), "properties", Map.class);
        UnitOfMeasurement unit = toUnitOfMeasure(provider);

        GeoJsonObject observedAreaRead = getResourceField(getAdminService(provider), LOCATION, GeoJsonObject.class);
        Polygon observedArea = getObservedArea(observedAreaRead);
        String thingId = getResourceField(getDatastreamService(provider), "thingId", String.class);
        Sensor sensor = toSensor(provider, uriInfo);
        ObservedProperty observedProperty = toObservedProperty(provider, uriInfo);
        UnitOfMeasurement unitOfMeasurement = toUnitOfMeasure(provider);

        String selfLink = null;
        String observationsLink = null;
        String observedPropertyLink = null;
        String sensorLink = null;
        String thingLink = null;
        if (uriInfo != null) {
            selfLink = getLink(uriInfo, VERSION, "Datastreams({id})", id);
            observationsLink = getLink(uriInfo, selfLink, "Observations");
            observedPropertyLink = getLink(uriInfo, selfLink, "ObservedProperty");
            sensorLink = getLink(uriInfo, selfLink, "Sensor");

            thingLink = getLink(uriInfo, selfLink, "Thing");
        }
        Datastream datastream = new Datastream(selfLink, id, name, description,
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation", unit, observedArea, null, null,
                metadata, observationsLink, observedPropertyLink, sensorLink, thingLink);

        DtoMapperSimple.checkRequireField(datastream);
        DtoMapperSimple.checkRequireLink(unit, sensor, observedProperty, unitOfMeasurement, thingId);

        return datastream;
    }

    public static String extractFirstIdSegment(String id) {
        return extractIdSegment(id, 0);
    }

    public static boolean isDatastream(ProviderSnapshot provider) {
        return getDatastreamService(provider) != null;
    }

    public static boolean isThing(ProviderSnapshot provider) {
        return getThingService(provider) != null;
    }

    public static boolean isLocation(ProviderSnapshot provider) {
        return getLocationService(provider) != null;
    }

    public static <T> T getResourceField(ServiceSnapshot service, String resourceName, Class<T> expectedType) {

        var resource = service.getResource(resourceName);

        if (resource != null && resource.getValue() != null) {
            return expectedType.cast(resource.getValue().getValue());
        }
        if (List.class.isAssignableFrom(expectedType)) {
            return expectedType.cast(List.of());
        }

        if (Map.class.isAssignableFrom(expectedType)) {
            return expectedType.cast(Map.of());
        }
        return null;
    }

    public static String extractSecondIdSegment(String id) {
        return extractIdSegment(id, 1);

    }

    public static String extractThirdIdSegment(String id) {
        return extractIdSegment(id, 2);

    }
}
