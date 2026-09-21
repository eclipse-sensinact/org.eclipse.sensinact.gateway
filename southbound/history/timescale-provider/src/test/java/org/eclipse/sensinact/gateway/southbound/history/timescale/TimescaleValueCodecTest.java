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
package org.eclipse.sensinact.gateway.southbound.history.timescale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.junit.jupiter.api.Test;

class TimescaleValueCodecTest {

    private final TimescaleValueCodec codec = new TimescaleValueCodec();

    private static ResultSet row(ValueKind kind, String javaType, String num, String json) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getShort("value_kind")).thenReturn((short) kind.ordinal());
        when(rs.getString("java_type")).thenReturn(javaType);
        when(rs.getString("value_num")).thenReturn(num);
        when(rs.getString("value_json")).thenReturn(json);
        return rs;
    }

    @Test
    void numericLiterals() {
        assertEquals("42", TimescaleValueCodec.numericLiteral(42L));
        assertEquals("4.2", TimescaleValueCodec.numericLiteral(4.2d));
        assertEquals("NaN", TimescaleValueCodec.numericLiteral(Double.NaN));
        assertEquals("Infinity", TimescaleValueCodec.numericLiteral(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", TimescaleValueCodec.numericLiteral(Double.NEGATIVE_INFINITY));
        assertEquals("0.000000000000000000001",
                TimescaleValueCodec.numericLiteral(new BigDecimal("1E-21")));
        assertNull(TimescaleValueCodec.numericLiteral(null));
    }

    @Test
    void jsonValueOnlyForStructuredKinds() {
        ResourcePath path = new ResourcePath("p", "s", "r");
        Instant t = Instant.EPOCH;

        assertNull(codec.jsonValue(new HistoricalRecord("u", "m", path, t, ValueKind.NUMBER, 42L)));
        assertNull(codec.jsonValue(new HistoricalRecord("u", "m", path, t, ValueKind.STRING, null)));
        assertEquals("\"on\"", codec.jsonValue(new HistoricalRecord("u", "m", path, t, ValueKind.STRING, "on")));
        assertEquals("{\"coordinates\":[5.7,12.3],\"type\":\"Point\"}",
                codec.jsonValue(new HistoricalRecord("u", "m", path, t, ValueKind.GEOJSON, new Point(5.7, 12.3))));
    }

    @Test
    void numbersRestoreTheirExactType() throws SQLException {
        assertEquals(42L, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Long", "42", null)));
        assertEquals(7, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Integer", "7", null)));
        assertEquals((short) 3, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Short", "3", null)));
        assertEquals((byte) 1, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Byte", "1", null)));
        assertEquals(4.2d, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Double", "4.2", null)));
        assertEquals(2.5f, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Float", "2.5", null)));
        assertEquals(new BigInteger("12345678901234567890"),
                codec.reconstruct(row(ValueKind.NUMBER, "java.math.BigInteger", "12345678901234567890", null)));
        assertEquals(new BigDecimal("1.000000000000000000000000000001"), codec.reconstruct(
                row(ValueKind.NUMBER, "java.math.BigDecimal", "1.000000000000000000000000000001", null)));
        assertEquals(Double.NaN, codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Double", "NaN", null)));
        assertEquals(Double.NEGATIVE_INFINITY,
                codec.reconstruct(row(ValueKind.NUMBER, "java.lang.Double", "-Infinity", null)));
    }

    @Test
    void legacyRowsWithoutJavaTypeKeepTheHistoricalNarrowing() throws SQLException {
        assertEquals(42L, codec.reconstruct(row(ValueKind.NUMBER, null, "42", null)));
        assertEquals(4.2d, codec.reconstruct(row(ValueKind.NUMBER, null, "4.2", null)));
        assertEquals(2.0d, codec.reconstruct(row(ValueKind.NUMBER, null, "2.0", null)));
        assertEquals(Double.POSITIVE_INFINITY,
                codec.reconstruct(row(ValueKind.NUMBER, null, "Infinity", null)));
        assertNull(codec.reconstruct(row(ValueKind.NUMBER, null, null, null)));
    }

    @Test
    void structuredKindsReadFromJson() throws SQLException {
        assertEquals(Boolean.TRUE, codec.reconstruct(row(ValueKind.BOOLEAN, "java.lang.Boolean", null, "true")));
        assertEquals("on", codec.reconstruct(row(ValueKind.STRING, "java.lang.String", null, "\"on\"")));
        assertEquals(new Point(5.7, 12.3), codec.reconstruct(row(ValueKind.GEOJSON,
                "org.eclipse.sensinact.gateway.geojson.Point", null,
                "{\"type\":\"Point\",\"coordinates\":[5.7,12.3]}")));
        assertNull(codec.reconstruct(row(ValueKind.STRING, null, null, null)));
    }
}
