package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;

public class DtoFactory {

    public static ExpandedDataStream getDatastreamMinimal(String name) {

        // Required
        return getDatastreamMinimalLinkThing(name, null);
    }

    public static ExpandedDataStream getDatastreamMinimalLinkThing(String name, Thing thingId) {
        Sensor sensor = getSensor("sensor1");
        // Required
        ObservedProperty op = getObservedProperty("Temperature");
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");

        return new ExpandedDataStream(null, null, name, "Measures temperature", null, uom, null, null, null, null, null,
                null, null, null, null, op, sensor, null, thingId);
    }

    private static UnitOfMeasurement getUnitOfMeasure(String name) {
        return new UnitOfMeasurement(name, "°C", "http://unitsofmeasure.org");
    }

    public static Sensor getSensor(String name) {
        return new Sensor(null, null, "Humidity Sensor", "Measures ambient humidity", "application/pdf",
                "http://example.com/humidity-sensor.pdf", null, null);

    }

    public static ObservedProperty getObservedProperty(String name) {
        return new ObservedProperty(null, null, "Temperature", "Air temperature", "http://example.com/op/temperature",
                null, null);

    }

    public static Observation getObservation(String name) {
        Instant now = Instant.now();
        Instant after = Instant.now();
        return new Observation(null, "obs2", now, after, null, null, new TimeInterval(now, after), null, null, null);

    }

    public static ExpandedLocation getIdLocation(Object id) {

        return new ExpandedLocation(null, id, null, null, null, null, null, null, null);
    }

    public static ExpandedDataStream getIdDatastream(Object id) {

        return new ExpandedDataStream(null, id, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
    }

    public static Thing getIdThing(Object id) {

        return new Thing(null, id, null, null, null, null, null, null);
    }

    public static ExpandedDataStream getDatastreamLinkThingWithSensorObservedPropertyObservation(String name,
            Thing thing) {

        // Required unit
        UnitOfMeasurement uom = getUnitOfMeasure("Percent");

        // Inline sensor
        Observation observation1 = getObservation("osb1");
        Observation observation2 = getObservation("obs2");

        Sensor sensor = getSensor("sensor");
        ObservedProperty op = getObservedProperty("op1");
        return new ExpandedDataStream(null, null, name, "Measures temperature", null, uom, null, null, null, null, null,
                null, null, null, List.of(observation1, observation2), op, sensor, null, thing);
    }

    public static ExpandedLocation getLocation(String name) {

        return getLocationLinkThing(name, null);
    }

    public static ExpandedLocation getLocationLinkThing(String name, List<Thing> things) {
        return new ExpandedLocation(null, null, name, "location1 test", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), null, null, things);
    }

    public static ExpandedThing getExpandedThing(String name, String description, Map<String, Object> properties) {
        return new ExpandedThing(null, null, name, description, properties, null, null, null, null, null, null);
    }

    public static ExpandedThing getExpandedThingWithLocationsHistoricalLocation(String name, String description,
            Map<String, Object> properties, List<ExpandedLocation> locations,
            List<HistoricalLocation> historicalLocations) {
        return new ExpandedThing(null, null, name, description, properties, null, null, null, null, locations,
                historicalLocations);
    }

    public static ExpandedThing getExpandedThingWithLocations(String name, String description,
            Map<String, Object> properties, List<ExpandedLocation> locations) {
        return new ExpandedThing(null, null, name, description, properties, null, null, null, null, locations, null);
    }

    public static ExpandedThing getExpandedThingWithDatastreamsLocationsHistoricalLocations(String name,
            String description, Map<String, Object> properties, List<ExpandedDataStream> datastreams,
            List<ExpandedLocation> locations, List<HistoricalLocation> historicalLocations) {
        return new ExpandedThing(null, null, name, description, properties, null, null, null, datastreams, locations,
                historicalLocations);
    }

    public static ExpandedThing getExpandedThingWithDatastreamsLocations(String name, String description,
            Map<String, Object> properties, List<ExpandedDataStream> datastreams, List<ExpandedLocation> locations) {
        return new ExpandedThing(null, null, name, description, properties, null, null, null, datastreams, locations,
                null);
    }

    public static ExpandedThing getExpandedThingWithDatastreams(String name, String description,
            Map<String, Object> properties, List<ExpandedDataStream> datastreams) {
        return new ExpandedThing(null, null, name, description, properties, null, null, null, datastreams, null, null);
    }
}
