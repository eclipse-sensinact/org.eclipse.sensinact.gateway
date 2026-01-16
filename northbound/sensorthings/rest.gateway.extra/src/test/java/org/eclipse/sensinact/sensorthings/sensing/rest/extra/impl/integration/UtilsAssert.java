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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;

import com.fasterxml.jackson.databind.JsonNode;

public class UtilsAssert {

    public static void assertDatastream(ExpandedDataStream expectedLocation, JsonNode datastream) {
        assertDatastream(expectedLocation, datastream, false);
    }

    public static void assertDatastream(ExpandedDataStream dto, JsonNode json, boolean expanded) {
        assertSelfLink(dto, json);

        assertEquals(dto.name(), json.get("name").asText(), "");
        assertEquals(dto.description(), json.get("description").asText(), "");
        if (expanded) {
            if (dto.observedProperty() != null) {
                JsonNode observedPropertyNode = json.get("ObservedProperty");
                assertNotNull(observedPropertyNode, "observedProperty array must be present");
                assertEquals(dto.observedProperty().name(), observedPropertyNode.get("name").asText(), "");
            }
            if (dto.observations() != null && dto.observations().size() > 0) {
                assertEquals(1, json.get("Observations").size());
                assertObservation(dto.observations().get(dto.observations().size() - 1),
                        json.get("Observations").get(0));

            }
        }
    }

    public static void assertLocation(ExpandedLocation dtoLocation, JsonNode json) {
        assertLocation(dtoLocation, json, false);
    }

    public static void assertFeatureOfInterest(FeatureOfInterest dtoFeatureOfInterest, JsonNode json) {
        assertFeatureOfInterest(dtoFeatureOfInterest, json, false);
    }

    public static void assertFeatureOfInterest(FeatureOfInterest dto, JsonNode json, boolean expanded) {
        assertSelfLink(dto, json);

        assertEquals(dto.name(), json.get("name").asText());
        assertEquals(dto.description(), json.get("description").asText());
        assertEquals(dto.feature().toJsonString(), json.get("feature").toString());
    }

    public static void assertLocation(ExpandedLocation dto, JsonNode json, boolean expanded) {
        assertSelfLink(dto, json);

        assertEquals(dto.name(), json.get("name").asText());
        assertEquals(dto.description(), json.get("description").asText());
        assertEquals(dto.location().toJsonString(), json.get("location").toString());

    }

    public static void assertSensor(Sensor dto, JsonNode json) {
        assertSelfLink(dto, json);
        assertEquals(dto.name(), json.get("name").asText());
        assertEquals(dto.description(), json.get("description").asText());
        assertEquals(dto.encodingType(), json.get("encodingType").asText());
        assertEquals(dto.properties(), json.get("properties"));
        assertEquals(dto.metadata(), json.get("metadata").asText());

    }

    public static void assertSelfLink(Id dto, JsonNode json) {

        // assertEquals(dto.selfLink(), json.get("selfLink").asText());
    }

    public static void assertObservedProperty(ObservedProperty dto, JsonNode json) {
        assertSelfLink(dto, json);

        assertEquals(dto.definition(), json.get("definition").asText());
        assertEquals(dto.description(), json.get("description").asText());
        assertEquals(dto.name(), json.get("name").asText());
        assertEquals(dto.properties(), json.get("properties"));

    }

    public static void assertObservation(ExpandedObservation dto, JsonNode json, boolean expanded) {

        assertEquals(dto.parameters(), json.get("parameters"));
        assertEquals(dto.resultQuality(), json.get("resultQuality").asText());
        if (expanded) {
            assertFeatureOfInterest(dto.featureOfInterest(), json.get("FeatureOfInterest"));
        }
    }

    public static void assertObservation(ExpandedObservation dto, JsonNode json) {
        assertObservation(dto, json, false);
    }

    public static void assertThing(ExpandedThing dto, JsonNode json) {
        assertThing(dto, json, false);
    }

    public static void assertThing(ExpandedThing dto, JsonNode json, boolean expanded) {
        assertSelfLink(dto, json);

        assertTrue(json.has("@iot.id"), "Response must contain @iot.id");
        assertEquals(dto.name(), json.get("name").asText());
        assertEquals(dto.description(), json.get("description").asText());
        if (expanded) {
            if (dto.locations() != null && dto.locations().size() > 0) {
                JsonNode locationsNode = json.get("Locations");
                if (dto.locations().size() > 1) {
                    assertNotNull(locationsNode, "Locations array must be present");
                    assertEquals(dto.locations().size(), locationsNode.size(), "Number of locations must match");
                    for (int i = 0; i < dto.locations().size(); i++) {
                        assertLocation(dto.locations().get(i), locationsNode.get(i));
                    }
                } else {
                    assertLocation(dto.locations().get(0), locationsNode.get(0));

                }
            }
            if (dto.datastreams() != null && dto.datastreams().size() > 0) {
                JsonNode datastreamNode = json.get("Datastreams");
                assertNotNull(datastreamNode, "Datastreams array must be present");
                assertEquals(dto.datastreams().size(), datastreamNode.size(), "Number of Datastreams must match");
                for (int i = 0; i < dto.datastreams().size(); i++) {
                    assertDatastream(dto.datastreams().get(i), datastreamNode.get(i));
                }
            }
        }
    }

}
