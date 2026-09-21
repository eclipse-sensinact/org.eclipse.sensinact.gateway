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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converts values between their Java form and the unified schema columns:
 * numbers as canonical literals in the NUMERIC column (NaN/±Infinity as
 * PostgreSQL special literals), everything structured as JSONB. The stored
 * Java type restores the exact original class; rows without one (migrated
 * from the pre-rework schema) keep the historical narrowing (scale <= 0 as
 * Long, otherwise Double).
 */
final class TimescaleValueCodec {

    private final ObjectMapper mapper = JsonMapper.builder().enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .build();

    static String numericLiteral(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double d) {
            if (d.isNaN()) {
                return "NaN";
            }
            if (d.isInfinite()) {
                return d > 0 ? "Infinity" : "-Infinity";
            }
        }
        if (value instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        return value.toString();
    }

    String jsonValue(HistoricalRecord record) {
        if (record.value() == null || record.kind() == ValueKind.NUMBER) {
            return null;
        }
        return mapper.writeValueAsString(record.value());
    }

    /** Reads the value of the current row of a {@code SELECT_ROW} result. */
    Object reconstruct(ResultSet rs) throws SQLException {
        ValueKind kind = ValueKind.values()[rs.getShort("value_kind")];
        String javaType = rs.getString("java_type");
        if (kind == ValueKind.NUMBER) {
            return reconstructNumber(rs.getString("value_num"), javaType);
        }
        String json = rs.getString("value_json");
        if (json == null) {
            return null;
        }
        return switch (kind) {
        case BOOLEAN -> mapper.readValue(json, Boolean.class);
        case STRING -> mapper.readValue(json, String.class);
        case GEOJSON -> mapper.readValue(json, GeoJsonObject.class);
        case OBJECT -> mapper.readValue(json, Object.class);
        case NUMBER -> throw new IllegalStateException("unreachable");
        };
    }

    private static Object reconstructNumber(String literal, String javaType) {
        if (literal == null) {
            return null;
        }
        if (javaType == null) {
            // legacy row: keep the historical read shape
            return switch (literal) {
            case "NaN", "Infinity", "-Infinity" -> parseDouble(literal);
            default -> {
                BigDecimal legacy = new BigDecimal(literal);
                yield legacy.scale() <= 0 ? (Object) legacy.longValueExact() : (Object) legacy.doubleValue();
            }
            };
        }
        return switch (javaType) {
        case "java.lang.Long" -> Long.valueOf(literal);
        case "java.lang.Integer" -> Integer.valueOf(literal);
        case "java.lang.Short" -> Short.valueOf(literal);
        case "java.lang.Byte" -> Byte.valueOf(literal);
        case "java.lang.Double" -> parseDouble(literal);
        case "java.lang.Float" -> parseDouble(literal).floatValue();
        case "java.math.BigInteger" -> new BigInteger(literal);
        default -> new BigDecimal(literal);
        };
    }

    private static Double parseDouble(String literal) {
        return switch (literal) {
        case "NaN" -> Double.NaN;
        case "Infinity" -> Double.POSITIVE_INFINITY;
        case "-Infinity" -> Double.NEGATIVE_INFINITY;
        default -> Double.valueOf(literal);
        };
    }
}
