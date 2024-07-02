/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.mqtt.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ContextTest {

    @Test
    void testTopicContext() throws Exception {
        final MqttDeviceFactoryHandler handler = new MqttDeviceFactoryHandler();
        final Map<String, String> segments = new HashMap<>();

        // Empty topic (shouldn't exist according to the specification)
        handler.fillTopicSegments("", segments);
        assertEquals("", segments.get("topic"));
        assertEquals("", segments.get("topic-last"));
        assertEquals("", segments.get("topic-0"));
        assertFalse(segments.containsKey("topic-1"));

        // Space topic
        segments.clear();
        handler.fillTopicSegments(" ", segments);
        assertEquals(" ", segments.get("topic"));
        assertEquals(" ", segments.get("topic-last"));
        assertEquals(" ", segments.get("topic-0"));
        assertFalse(segments.containsKey("topic-1"));

        // Slash topic
        segments.clear();
        handler.fillTopicSegments("/", segments);
        assertEquals("/", segments.get("topic"));
        assertEquals("", segments.get("topic-last"));
        assertEquals("", segments.get("topic-0"));
        assertEquals("", segments.get("topic-1"));
        assertFalse(segments.containsKey("topic-2"));

        // Long topic
        segments.clear();
        handler.fillTopicSegments("foo/bar/foobar", segments);
        assertEquals("foo/bar/foobar", segments.get("topic"));
        assertEquals("foo", segments.get("topic-0"));
        assertEquals("bar", segments.get("topic-1"));
        assertEquals("foobar", segments.get("topic-2"));
        assertEquals("foobar", segments.get("topic-last"));
        assertFalse(segments.containsKey("topic-3"));

        // Starting slash shall not be ignored, as explained in the MQTT specification
        segments.clear();
        handler.fillTopicSegments("/foo/bar/foobar", segments);
        assertEquals("/foo/bar/foobar", segments.get("topic"));
        assertEquals("", segments.get("topic-0"));
        assertEquals("foo", segments.get("topic-1"));
        assertEquals("bar", segments.get("topic-2"));
        assertEquals("foobar", segments.get("topic-3"));
        assertEquals("foobar", segments.get("topic-last"));
        assertFalse(segments.containsKey("topic-4"));

        // Trailing slash shall not be ignored, as explained in the MQTT specification
        segments.clear();
        handler.fillTopicSegments("foo/bar/foobar/", segments);
        assertEquals("foo/bar/foobar/", segments.get("topic"));
        assertEquals("foo", segments.get("topic-0"));
        assertEquals("bar", segments.get("topic-1"));
        assertEquals("foobar", segments.get("topic-2"));
        assertEquals("", segments.get("topic-3"));
        assertEquals("", segments.get("topic-last"));
        assertFalse(segments.containsKey("topic-4"));

        // Double-slashes shall be considered as 1, as explained in the MQTT
        // specification
        segments.clear();
        handler.fillTopicSegments("foo//bar", segments);
        assertEquals("foo//bar", segments.get("topic"));
        assertEquals("foo", segments.get("topic-0"));
        assertEquals("bar", segments.get("topic-1"));
        assertEquals("bar", segments.get("topic-last"));
        assertFalse(segments.containsKey("topic-3"));
    }
}
