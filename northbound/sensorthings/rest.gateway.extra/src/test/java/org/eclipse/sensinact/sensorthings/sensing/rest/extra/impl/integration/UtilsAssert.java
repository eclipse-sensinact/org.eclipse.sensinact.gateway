package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;

import com.fasterxml.jackson.databind.JsonNode;

public class UtilsAssert {

    public static void assertDatastream(ExpandedDataStream expectedLocation, JsonNode datastream) {
        assertDatastream(expectedLocation, datastream, false);
    }

    public static void assertDatastream(ExpandedDataStream expectedDatastream, JsonNode datastream, boolean expanded) {
        assertEquals(expectedDatastream.name(), datastream.get("name").asText(), "");
        assertEquals(expectedDatastream.description(), datastream.get("description").asText(), "");
        if (expanded) {
            if (expectedDatastream.observedProperty() != null) {
                JsonNode observedPropertyNode = datastream.get("ObservedProperty");
                assertNotNull(observedPropertyNode, "observedProperty array must be present");
                assertEquals(expectedDatastream.observedProperty().name(), observedPropertyNode.get("name").asText(),
                        "");
            }
        }
    }

    public static void assertLocation(ExpandedLocation dtoLocation, JsonNode json) {
        assertLocation(dtoLocation, json, false);
    }

    public static void assertLocation(ExpandedLocation dtoLocation, JsonNode json, boolean expanded) {
        assertEquals(dtoLocation.name(), json.get("name").asText());
        assertEquals(dtoLocation.description(), json.get("description").asText());
        assertEquals(dtoLocation.location().toJsonString(), json.get("location").toString());
    }

    public static void assertSensor(ExpandedSensor sensor, JsonNode json) {
        assertEquals(sensor.name(), json.get("name"));
        assertEquals(sensor.description(), json.get("description"));
        assertEquals(sensor.encodingType(), json.get("encodingType"));
        assertEquals(sensor.properties(), json.get("properties"));
        assertEquals(sensor.metadata(), json.get("metadata"));
    }

    public static void assertObservedProperty(ExpandedObservedProperty observedProperty, JsonNode json) {
        assertEquals(observedProperty.definition(), json.get("definition"));
        assertEquals(observedProperty.description(), json.get("description"));
        assertEquals(observedProperty.name(), json.get("name"));
        assertEquals(observedProperty.properties(), json.get("properties"));

    }

    public static void assertObservation(ExpandedObservation observation, JsonNode json) {
        assertEquals(observation.parameters(), json.get("parameters"));
        assertEquals(observation.phenomenonTime(), json.get("phenomenonType"));
        assertEquals(observation.resultQuality(), json.get("resultQuality"));
        assertEquals(observation.resultTime(), json.get("resultTime"));
    }

    public static void assertThing(ExpandedThing dtoThing, JsonNode json) {
        assertThing(dtoThing, json, false);
    }

    public static void assertThing(ExpandedThing dtoThing, JsonNode json, boolean expanded) {
        assertTrue(json.has("@iot.id"), "Response must contain @iot.id");
        assertEquals(dtoThing.name(), json.get("name").asText());
        assertEquals(dtoThing.description(), json.get("description").asText());
        if (expanded) {
            if (dtoThing.locations() != null && dtoThing.locations().size() > 0) {
                JsonNode locationsNode = json.get("Locations");
                if (dtoThing.locations().size() > 1) {
                    assertNotNull(locationsNode, "Locations array must be present");
                    assertEquals(dtoThing.locations().size(), locationsNode.size(), "Number of locations must match");
                    for (int i = 0; i < dtoThing.locations().size(); i++) {
                        String locationNameResult = locationsNode.get(i).get("name").asText();
                        String locationNameExpected = dtoThing.locations().get(i).name();
                        assertEquals(locationNameExpected, locationNameResult);
                    }
                } else {
                    String locationNameResult = locationsNode.get(0).get("name").asText();
                    assertEquals(dtoThing.locations().get(0).name(), locationNameResult);

                }
            }
            if (dtoThing.datastreams() != null && dtoThing.datastreams().size() > 0) {
                JsonNode datastreamNode = json.get("Datastreams");
                assertNotNull(datastreamNode, "Datastreams array must be present");
                assertEquals(dtoThing.datastreams().size(), datastreamNode.size(), "Number of Datastreams must match");
                for (int i = 0; i < dtoThing.datastreams().size(); i++) {
                    assertDatastream(dtoThing.datastreams().get(i), datastreamNode.get(i));
                }
            }
        }
    }

}
