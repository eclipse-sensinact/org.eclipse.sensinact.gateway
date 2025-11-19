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
package org.eclipse.sensinact.gateway.southbound.sensorthings.sensing.rest;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.DATA_STREAM_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.gateway.southbound.sensorthings.sensing.rest.dto.ExpandedDataStream;
import org.eclipse.sensinact.gateway.southbound.sensorthings.sensing.rest.dto.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component(configurationPid = "sensinact.sensorthings.southbound.rest", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PollingRest {

    private static final String QUERY = """
            $expand=\
            Locations,\
            Datastreams(\
            $expand=Sensor,\
            ObservedProperty,\
            Observations($orderby=phenomenonTime desc;$top=1)\
            )""";

    public @interface Config {
        int threads() default 2;

        int interval() default 30;

        String uri();

        String provider_prefix() default "sensorthings_";
    }

    @Reference
    DataUpdate dataUpdate;

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    ScheduledExecutorService worker;

    HttpClient client;

    Duration interval;

    URI baseURI;

    @Activate
    void start(Config config) {
        interval = Duration.ofSeconds(config.interval());
        worker = Executors.newScheduledThreadPool(config.threads());
        baseURI = URI.create(config.uri());

        client = HttpClient.newBuilder().executor(worker).connectTimeout(interval.dividedBy(4)).build();

        worker.execute(this::doPoll);
    }

    void doPoll() {
        long start = System.nanoTime();
        try {
            URI uri = baseURI.resolve("Things");
            uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), QUERY, null);

            do {
                HttpRequest req = HttpRequest.newBuilder(uri).timeout(interval.dividedBy(2))
                        .header("Accept", "application/json").GET().build();

                HttpResponse<InputStream> response = client.send(req, BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Wrong status code: " + response.statusCode());
                }

                ResultList<ExpandedThing> list = mapper.readValue(response.body(),
                        new TypeReference<ResultList<ExpandedThing>>() {
                        });

                List<SensorThingsUpdate> updates = list.value().stream().flatMap(this::toUpdates).toList();

                dataUpdate.pushUpdate(updates);

                uri = list.nextLink() != null && !list.value().isEmpty() ? URI.create(list.nextLink()) : null;

            } while (uri != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Duration toWait = interval.minusNanos(System.nanoTime() - start);
        if (toWait.compareTo(interval.dividedBy(4)) <= 0) {
            // Querying took a long time, wait a full interval
            toWait = interval;
        }
        worker.schedule(this::doPoll, toWait.toNanos(), TimeUnit.NANOSECONDS);
    }

    Stream<SensorThingsUpdate> toUpdates(ExpandedThing thing) {
        String providerId = sanitizeId(thing.name() == null ? thing.id() : thing.name());
        GeoJsonObject location = aggregate(thing.locations());
        Map<String, Object> thingProperties = thing.properties().entrySet().stream()
                .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue));
        ThingUpdate provider = new ThingUpdate(providerId, thing.name(), thing.description(), location,
                thing.id(), thingProperties);

        return Stream.concat(Stream.of(provider),
                thing.datastreams().stream().map(d -> toDatastreamUpdate(providerId, d)));
    }

    private String sanitizeId(Object object) {
        return String.valueOf(object).replaceAll("[^0-9a-zA-Z\\.\\-_]", "_");
    }

    private GeoJsonObject aggregate(List<Location> locations) {
        return switch (locations.size()) {
        case 0:
            yield null;
        case 1:
            yield toFeature(locations.get(0));
        default:
            yield new FeatureCollection(locations.stream().map(this::toFeature).toList(), null, null);
        };
    }

    private Feature toFeature(Location location) {
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

    private Feature toFeature(FeatureCollection fc) {
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

    private DatastreamUpdate toDatastreamUpdate(String providerId, ExpandedDataStream ds) {
        String serviceName = sanitizeId(ds.name() == null ? ds.id() : ds.name());
        Instant timestamp = ds.phenomenonTime() == null ? null : ds.phenomenonTime().start();

        Object observation;
        Map<String, Object> observationParameters;
        if (ds.observations() == null || ds.observations().isEmpty()) {
            observation = null;
            observationParameters = null;
        } else {
            Observation o = ds.observations().get(0);
            observation = o.result();
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

        Object sensor;
        Map<String, Object> sensorMetadata;
        if (ds.sensor() == null) {
            sensor = null;
            sensorMetadata = null;
        } else {
            Sensor dsSensor = ds.sensor();
            sensor = Objects.toString(dsSensor.id());
            sensorMetadata = new HashMap<>();
            sensorMetadata.put("sensorthings.sensor.name", ds.sensor().name());
            sensorMetadata.put("sensorthings.sensor.description", ds.sensor().description());
            sensorMetadata.put("sensorthings.sensor.metadata", ds.sensor().metadata());
            sensorMetadata.put("sensorthings.sensor.encodingType", ds.sensor().encodingType());
            if (ds.sensor().properties() != null) {
                ds.sensor().properties().forEach((k, v) -> sensorMetadata.put("sensorthings.sensor.properties." + k, v));
            }
        }

        Object observedProperty;
        Map<String, Object> observedPropertyMetadata;
        if (ds.observedProperty() == null) {
            observedProperty = null;
            observedPropertyMetadata = null;
        } else {
            ObservedProperty dsOp = ds.observedProperty();
            observedProperty = dsOp.id();
            observedPropertyMetadata = new HashMap<>();
            observedPropertyMetadata.put("sensorthings.observedProperty.name", ds.observedProperty().name());
            observedPropertyMetadata.put("sensorthings.observedProperty.description", ds.observedProperty().description());
            observedPropertyMetadata.put("sensorthings.observedProperty.definition", ds.observedProperty().definition());
            if (ds.observedProperty().properties() != null) {
                ds.observedProperty().properties().forEach(
                        (k, v) -> observedPropertyMetadata.put("sensorthings.observedProperty.properties." + k, v));
            }
        }

        return new DatastreamUpdate(providerId, serviceName, ds.id(), ds.name(), ds.description(), observation, timestamp,
                observationParameters, unit, unitMetadata, sensor, sensorMetadata, observedProperty,
                observedPropertyMetadata);
    }

    @Service("admin")
    public record ThingUpdate(@Model EClass model, @Provider String providerId,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
            @Service("thing") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT) GeoJsonObject location,
            @Service("thing") @Resource("id") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object thingId,
            @Service("thing") @Resource("id") @Metadata(onMap = {
                    USE_KEYS_AS_FIELDS }) Map<String, Object> properties)
            implements SensorThingsUpdate{
        public ThingUpdate {
            if (model == null) {
                model = SENSOR_THINGS_DEVICE;
            }
            if (model != SENSOR_THINGS_DEVICE) {
                throw new IllegalArgumentException(
                        "The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
            }
        }

        ThingUpdate(String providerId, String friendlyName, String description, GeoJsonObject location, Object thingId,
                Map<String, Object> properties) {
            this(SENSOR_THINGS_DEVICE, providerId, friendlyName, description, location, thingId, properties);
        }
    }

    public record DatastreamUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
            @Service String serviceName, @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingsId,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String name,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object latestObservation,
            @Timestamp Instant timestamp, @Resource("latestObservation") @Metadata(onMap = {
                    USE_KEYS_AS_FIELDS }) Map<String, Object> observationParameters,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String unit,
            @Resource("unit") @Metadata(onMap = { USE_KEYS_AS_FIELDS }) Map<String, Object> unitMetadata,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object sensor,
            @Resource("sensor") @Metadata(onMap = { USE_KEYS_AS_FIELDS }) Map<String, Object> sensorMetadata,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) Object observedProperty,
            @Resource("observedProperty") @Metadata(onMap = {
                    USE_KEYS_AS_FIELDS }) Map<String, Object> observedPropertyMetadata)
            implements SensorThingsUpdate{
        public DatastreamUpdate {
            if (model == null) {
                model = SENSOR_THINGS_DEVICE;
            }
            if (model != SENSOR_THINGS_DEVICE) {
                throw new IllegalArgumentException(
                        "The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
            }
            if (service == null) {
                service = DATA_STREAM_SERVICE;
            }
            if (service != DATA_STREAM_SERVICE) {
                throw new IllegalArgumentException(
                        "The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
            }
        }

        DatastreamUpdate(String providerId, String serviceName, Object sensorThingsId, String name, String description,
                Object latestObservation, Instant timestamp, Map<String, Object> observationParameters, String unit,
                Map<String, Object> unitMetadata, Object sensor, Map<String, Object> sensorMetadata,
                Object observedProperty, Map<String, Object> observedPropertyMetadata) {
            this(SENSOR_THINGS_DEVICE, DATA_STREAM_SERVICE, providerId, serviceName, sensorThingsId, name, description,
                    latestObservation, timestamp, observationParameters, unit, unitMetadata, sensor, sensorMetadata,
                    observedProperty, observedPropertyMetadata);
        }
    }
}
