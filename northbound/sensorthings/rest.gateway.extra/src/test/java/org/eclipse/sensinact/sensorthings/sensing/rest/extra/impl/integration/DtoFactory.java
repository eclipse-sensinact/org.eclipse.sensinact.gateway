package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;

public class DtoFactory {

    public static Datastream getDatastreamMinimal() {
        Datastream ds = new Datastream();
        ds.name = "Temperature Datastream";
        ds.description = "Measures temperature";

        // Required
        UnitOfMeasurement uom = new UnitOfMeasurement();
        uom.name = "Celsius";
        uom.symbol = "°C";
        uom.definition = "http://unitsofmeasure.org";
        ds.unitOfMeasurement = uom;
        return ds;
    }

    public static ExpandedDataStream getDatastreamWithSensor() {
        ExpandedDataStream ds = new ExpandedDataStream();
        ds.name = "Humidity Datastream";
        ds.description = "Measures humidity";

        // Required unit
        UnitOfMeasurement uom = new UnitOfMeasurement();
        uom.name = "Percent";
        uom.symbol = "%";
        uom.definition = "http://unitsofmeasure.org";
        ds.unitOfMeasurement = uom;

        // Inline sensor
        Sensor sensor = new Sensor();
        sensor.name = "Humidity Sensor";
        sensor.description = "Measures ambient humidity";
        sensor.encodingType = "application/pdf";
        sensor.metadata = "http://example.com/humidity-sensor.pdf";
        ds.sensor = sensor;
        return ds;
    }

    public static ExpandedDataStream getDatastreamWithSensorObservedProperty() {
        ExpandedDataStream ds = new ExpandedDataStream();
        ds.name = "Humidity Datastream";
        ds.description = "Measures humidity";

        // Required unit
        UnitOfMeasurement uom = new UnitOfMeasurement();
        uom.name = "Percent";
        uom.symbol = "%";
        uom.definition = "http://unitsofmeasure.org";
        ds.unitOfMeasurement = uom;

        // Inline sensor
        Sensor sensor = new Sensor();
        sensor.name = "Humidity Sensor";
        sensor.description = "Measures ambient humidity";
        sensor.encodingType = "application/pdf";
        sensor.metadata = "http://example.com/humidity-sensor.pdf";
        ds.sensor = sensor;
        ObservedProperty op = new ObservedProperty();
        op.name = "Temperature";
        op.description = "Air temperature";
        op.definition = "http://example.com/op/temperature";
        ds.observedProperty = op;
        return ds;
    }

    public static Location getLocation1() {
        Location location1 = new Location();
        location1.name = "location1";
        location1.description = "location1 test";
        location1.location = new Point(-122.4194, 37.7749); // longitude, latitude
        location1.encodingType = "application/vnd.geo+json";
        return location1;
    }

    public static Location getLocation2() {
        Location location2 = new Location();
        location2.name = "location2";
        location2.description = "location2 test";
        location2.location = new Point(-121.4194, 38.7749); // longitude, latitude
        location2.encodingType = "application/vnd.geo+json";
        return location2;
    }

}
