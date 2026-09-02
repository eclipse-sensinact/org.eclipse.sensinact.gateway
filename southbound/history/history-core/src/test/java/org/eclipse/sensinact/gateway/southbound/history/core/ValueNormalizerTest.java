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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.junit.jupiter.api.Test;

class ValueNormalizerTest {

    private static final Instant TS = Instant.parse("2026-01-01T00:00:00Z");

    private static HistoricalRecord normalize(Object value, Class<?> type) {
        return ValueNormalizer.normalize(new ResourceDataNotification("https://eclipse.org/sensinact/test",
                "testModel", "testProvider", "testService", "testResource", null, value, TS, type, Map.of()));
    }

    @Test
    void addressAndTimestampAreCarriedOver() {
        HistoricalRecord record = normalize("x", String.class);

        assertEquals(new ResourcePath("testProvider", "testService", "testResource"), record.path());
        assertEquals(TS, record.timestamp());
        assertEquals("testModel", record.model());
        assertNull(record.endTimestamp());
        assertEquals(Map.of(), record.annotations());
    }

    @Test
    void numbersKeepTheirBoxedType() {
        assertEquals(ValueKind.NUMBER, normalize(42, int.class).kind());
        assertSame(Integer.class, normalize(42, int.class).value().getClass());
        assertEquals(ValueKind.NUMBER, normalize(4.2d, Double.class).kind());
        assertEquals(new BigDecimal("1.5"), normalize(new BigDecimal("1.5"), BigDecimal.class).value());
    }

    @Test
    void floatSpecialsWidenToDouble() {
        assertEquals(Double.NaN, normalize(Float.NaN, float.class).value());
        assertEquals(Double.POSITIVE_INFINITY, normalize(Float.POSITIVE_INFINITY, Float.class).value());
        assertEquals(2.5f, normalize(2.5f, float.class).value());
    }

    @Test
    void booleansAndStrings() {
        assertEquals(ValueKind.BOOLEAN, normalize(true, boolean.class).kind());
        assertEquals(ValueKind.STRING, normalize("on", String.class).kind());
        assertEquals(ValueKind.STRING, normalize(new StringBuilder("sb"), StringBuilder.class).kind());
        assertEquals("sb", normalize(new StringBuilder("sb"), StringBuilder.class).value());
        assertEquals(ValueKind.STRING, normalize('c', char.class).kind());
    }

    @Test
    void geoJsonKeepsTheObject() {
        Point point = new Point(5.7d, 12.3d);
        HistoricalRecord record = normalize(point, GeoJsonObject.class);

        assertEquals(ValueKind.GEOJSON, record.kind());
        assertSame(point, record.value());
    }

    @Test
    void geoJsonTypedResourceWithForeignValueFallsBackToObject() {
        assertEquals(ValueKind.OBJECT, normalize("not geo", GeoJsonObject.class).kind());
    }

    @Test
    void structuredValuesBecomeObjects() {
        HistoricalRecord record = normalize(Map.of("a", List.of(1, 2)), Map.class);

        assertEquals(ValueKind.OBJECT, record.kind());
        assertEquals(Map.of("a", List.of(1, 2)), record.value());
    }

    @Test
    void nullValueKeepsDeclaredKind() {
        assertEquals(ValueKind.GEOJSON, normalize(null, GeoJsonObject.class).kind());
        assertEquals(ValueKind.NUMBER, normalize(null, Double.class).kind());
        assertNull(normalize(null, Double.class).value());
    }
}
