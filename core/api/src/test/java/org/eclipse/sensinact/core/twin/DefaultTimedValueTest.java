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
package org.eclipse.sensinact.core.twin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeType;

public class DefaultTimedValueTest {

    private static final Instant TIME = Instant.parse("2025-01-23T11:30:15.123Z");
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = JsonMapper.builder()
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, false)
                .configure(EnumFeature.WRITE_ENUMS_USING_TO_STRING, false)
                .build();
    }

    @Test
    void testStringValue() throws Exception {
        TimedValue<String> tv = new DefaultTimedValue<>("test", TIME);

        assertEquals("test", tv.getValue());
        assertEquals(TIME, tv.getTimestamp());

        JsonNode node = mapper.convertValue(tv, JsonNode.class);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
        assertEquals(2, node.properties().size());
        assertEquals("test", node.get("value").asString());
        assertEquals(TIME.toString(), node.get("timestamp").asString());

        TimedValue<String> copy = mapper.convertValue(node,
                new TypeReference<TimedValue<String>>() {});

        assertEquals(tv.getValue(), copy.getValue());
        assertEquals(tv.getTimestamp(), copy.getTimestamp());
    }

    @Test
    void testNumberValue() throws Exception {
        TimedValue<Integer> tv = new DefaultTimedValue<>(5, TIME);

        assertEquals(5, tv.getValue());
        assertEquals(TIME, tv.getTimestamp());

        JsonNode node = mapper.convertValue(tv, JsonNode.class);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
        assertEquals(2, node.properties().size());
        assertEquals(5, node.get("value").asInt());
        assertEquals(TIME.toString(), node.get("timestamp").asString());

        TimedValue<Integer> copy = mapper.convertValue(node,
                new TypeReference<TimedValue<Integer>>() {});

        assertEquals(tv.getValue(), copy.getValue());
        assertEquals(tv.getTimestamp(), copy.getTimestamp());
    }

    @Test
    void testValueConversion() throws Exception {
        TimedValue<String> tv = new DefaultTimedValue<>("5", TIME);

        assertEquals("5", tv.getValue());
        assertEquals(TIME, tv.getTimestamp());

        JsonNode node = mapper.convertValue(tv, JsonNode.class);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
        assertEquals(2, node.properties().size());
        assertEquals(JsonNodeType.STRING, node.get("value").getNodeType());
        assertEquals("5", node.get("value").asString());
        assertEquals(TIME.toString(), node.get("timestamp").asString());

        TimedValue<Integer> copy = mapper.convertValue(node,
                new TypeReference<TimedValue<Integer>>() {});

        assertEquals(Integer.parseInt(tv.getValue()), copy.getValue());
        assertEquals(tv.getTimestamp(), copy.getTimestamp());
    }

}
