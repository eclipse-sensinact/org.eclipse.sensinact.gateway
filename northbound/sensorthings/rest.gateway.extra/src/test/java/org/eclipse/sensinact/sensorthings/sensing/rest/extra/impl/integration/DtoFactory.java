/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-RefIdentifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;

public class DtoFactory {

    public static ExpandedDataStream getDatastreamMinimal(String name) {

        // Required
        return getDatastreamMinimalLinkThing(name, null);
    }

    /**
     * { "name": "Weather station location", "description": "Geographic location of
     * the weather station", "encodingType": "application/vnd.geo+json", "feature":
     * { "type": "Point", "coordinates": [2.3522, 48.8566] } }
     *
     * @param name
     * @return
     */
    public static FeatureOfInterest getFeatureOfInterest(String name, String encodingType, GeoJsonObject feature) {

        return new FeatureOfInterest(null, null, name, "Geographic location of the weather station", encodingType,
                feature, null);
    }

    public static ExpandedDataStream getDatastream(String name, String description, UnitOfMeasurement unit,
            String observationType, RefId thingRefId, ExpandedSensor sensor, ExpandedObservedProperty op,
            List<ExpandedObservation> listObs) {

        Instant start = Instant.now();
        Instant end = Instant.now();
        TimeInterval interval = new TimeInterval(end, start);
        return new ExpandedDataStream(null, null, name, description, observationType, unit, null, interval, interval,
                null, null, null, null, null, listObs, op, sensor, null, thingRefId);
    }

    public static ExpandedDataStream getDatastreamMinimalLinkThingWithObservations(String name, RefId thingRefId,
            List<ExpandedObservation> listObs) {
        ExpandedSensor sensor = getSensor("sensor1");
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");
        ExpandedObservedProperty op = getObservedProperty("obProp");

        return getDatastream(name, "Measures temperature", uom, "obsType", thingRefId, sensor, op, listObs);

    }

    public static ExpandedDataStream getDatastreamMinimalWithThingObervedPropertySensor(String name, RefId thingRefId,
            ExpandedSensor sensor, ExpandedObservedProperty op) {
        // Required
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");

        return getDatastream(name, "Measures temperature", uom, "obsType", thingRefId, sensor, op, null);

    }

    public static ExpandedDataStream getDatastreamMinimalLinkThingLinkObservedProperty(String name, RefId thingRefId,
            RefId obRefId) {
        // Required
        ExpandedObservedProperty op = new ExpandedObservedProperty(null, obRefId, null, null, null, null, null);
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");
        ExpandedSensor sensor = getSensor("sensor1");

        return getDatastream(name, "Measures temperature", uom, "obsType", thingRefId, sensor, op, null);

    }

    public static ExpandedDataStream getDatastreamMinimalLinkThing(String name, RefId thingRefId) {
        // Required
        ExpandedSensor sensor = getSensor("test");
        ExpandedObservedProperty op = getObservedProperty("Temperature");
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");

        return getDatastream(name, "Measures temperature", uom, "obsType", thingRefId, sensor, op, null);

    }

    public static ExpandedDataStream getDatastreamMinimal(String name, String descriptikon, String obsType) {
        // Required
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");
        ExpandedSensor sensor = getSensor("test");
        ExpandedObservedProperty op = getObservedProperty("Temperature");
        return getDatastream(name, descriptikon, uom, "obsType", null, sensor, op, null);

    }

    public static ExpandedDataStream getDatastreamMinimalLinkThingLinkSensor(String name, RefId thingRefId,
            RefId sensorRefId) {
        // Required
        ExpandedSensor sensor = new ExpandedSensor(null, sensorRefId, null, null, null, null, null, null);
        ExpandedObservedProperty op = getObservedProperty("Temperature");
        UnitOfMeasurement uom = getUnitOfMeasure("Celcius");

        return getDatastream(name, "Measures temperature", uom, "obsType", thingRefId, sensor, op, null);

    }

    public static UnitOfMeasurement getUnitOfMeasure(String name) {
        return new UnitOfMeasurement(name, "°C", "http://unitsofmeasure.org");
    }

    public static ExpandedSensor getSensor(String name) {
        return new ExpandedSensor(null, null, name, "Measures ambient humRefIdity", "application/pdf",
                "http://example.com/humRefIdity-sensor.pdf", null, null);

    }

    public static ExpandedSensor getSensor(String name, String descripton, String encodingType) {
        return new ExpandedSensor(null, null, name, descripton, "application/pdf",
                "http://example.com/humRefIdity-sensor.pdf", null, null);

    }

    public static ExpandedObservedProperty getObservedProperty(String name) {
        return getObservedProperty(name, "http://example.com/op/temperature");

    }

    public static ExpandedObservedProperty getObservedProperty(String name, String definition) {
        return new ExpandedObservedProperty(null, null, name, "Air temperature", definition, null, null);

    }

    public static ExpandedObservation getObservationLinkDatastream(String name, RefId datastreamRefId) {
        return getObservationLinkDatastream(name, datastreamRefId, null);

    }

    public static ExpandedObservation getObservationLinkDatastream(String name, RefId datastreamRefId,
            FeatureOfInterest featureOfInterest) {

        return new ExpandedObservation(null, "obs2", Instant.now(), Instant.now(), 5.0, "test", null, null, null, null,
                null, datastreamRefId, featureOfInterest);

    }

    public static ExpandedObservation getObservationLinkDatastream(String name, Object result, Instant PhenomTime,
            RefId datastreamRefId, FeatureOfInterest featureOfInterest) {

        return new ExpandedObservation(null, "obs2", PhenomTime, null, result, "test", null, null, null, null, null,
                datastreamRefId, featureOfInterest);

    }

    public static ExpandedObservation getObservation(String name) {
        return getObservationLinkDatastream(name, null, null);

    }

    public static ExpandedObservation getObservationLinkFeatureOfInterest(String name, String foiRefId) {
        return getObservationWithFeatureOfInterest(name,
                new FeatureOfInterest(null, getRefId(foiRefId), null, null, null, null, null));
    }

    public static ExpandedObservation getObservationWithFeatureOfInterest(String name, FeatureOfInterest feature) {
        return getObservationLinkDatastream(name, null, feature);

    }

    public static ExpandedLocation getIdLocation(Object RefId) {

        return new ExpandedLocation(null, RefId, null, null, null, null, null, null, null);
    }

    public static ExpandedDataStream getIdDatastream(Object RefId) {

        return new ExpandedDataStream(null, RefId, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    public static RefId getRefId(Object RefId) {

        return new RefId(RefId);
    }

    public static ExpandedDataStream getDatastreamLinkThingWithSensorObservedProperty(String name, RefId thing) {

        // Required unit
        UnitOfMeasurement uom = getUnitOfMeasure("Percent");

        ExpandedSensor sensor = getSensor("sensor");
        ExpandedObservedProperty op = getObservedProperty("op1");
        return new ExpandedDataStream(null, null, name, "Measures temperature", "obsType", uom, null, null, null, null,
                null, null, null, null, null, op, sensor, null, thing);
    }

    public static ExpandedLocation getLocation(String name) {

        return getLocationLinkThing(name, "application/vnd.geo+json", new Point(-122.4194, 37.7749), null);
    }

    public static ExpandedLocation getLocationLinkThing(String name, String encodingType, GeoJsonObject location,
            List<RefId> things) {
        return new ExpandedLocation(null, null, name, "location1 test", encodingType, location, null, null, things);
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
