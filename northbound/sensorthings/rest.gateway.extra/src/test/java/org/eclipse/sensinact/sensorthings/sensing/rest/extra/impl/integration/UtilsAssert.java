package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;

import com.fasterxml.jackson.databind.JsonNode;

public class UtilsAssert {

    public static void assertDatastream(ExpandedDataStream expectedLocation, JsonNode datastream) {
        assertDatastream(expectedLocation, datastream, false);
    }

    public static void assertDatastream(ExpandedDataStream expectedLocation, JsonNode datastream, boolean expanded) {
        assertEquals(expectedLocation.name(), datastream.get("name"), "");
        assertEquals(expectedLocation.description(), datastream.get("description"), "");

        if (expectedLocation.observedProperty() != null) {
            JsonNode observedPropertyNode = datastream.get("observedProperty");
            assertNotNull(observedPropertyNode, "Observations array must be present");
            assertEquals(expectedLocation.observedProperty(), observedPropertyNode, "");
        }
    }

    public static void assertLocation(ExpandedLocation dtoLocation, JsonNode json) {
        assertLocation(dtoLocation, json, false);
    }

    public static void assertLocation(ExpandedLocation dtoLocation, JsonNode json, boolean expanded) {

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
                JsonNode locationsNode = json.get("Locations").get(0);
                JsonNode location = locationsNode.get("location");
                if (dtoThing.locations().size() > 1) {
                    JsonNode listLocation = location.get("features");
                    assertNotNull(locationsNode, "Locations array must be present");
                    assertEquals(dtoThing.locations().size(), listLocation.size(), "Number of locations must match");
                    for (int i = 0; i < dtoThing.locations().size(); i++) {
                        String locationNameResult = listLocation.get(i).get("properties")
                                .get("sensorthings.location.name").asText();
                        assertEquals(dtoThing.locations().get(i).name(), locationNameResult);
                    }
                } else {
                    String locationNameResult = location.get("properties").get("sensorthings.location.name").asText();
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
