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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;

/**
 * All SQL of the unified history schema in one place: DDL, legacy migration,
 * the composable statement {@link Builder} (one {@code if} per query
 * dimension — deliberately no template matrix) and the prune statements.
 */
final class TimescaleSql {

    static final String TABLE = "sensinact.history";
    static final String SELECT_ROW = "SELECT time, value_kind, java_type, value_num::text AS value_num,"
            + " value_json::text AS value_json FROM " + TABLE;

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + "time TIMESTAMPTZ NOT NULL,"
            + "modelpackageuri VARCHAR(128), model VARCHAR(128),"
            + "provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL,"
            + "resource VARCHAR(128) NOT NULL,"
            + "value_kind SMALLINT NOT NULL,"
            + "java_type VARCHAR(128),"
            + "value_num NUMERIC,"
            + "value_json JSONB)";
    static final String CREATE_INDEX = "CREATE INDEX history_psr_time ON " + TABLE
            + " (provider, service, resource, time DESC)";
    static final String CREATE_HYPERTABLE = "SELECT create_hypertable('" + TABLE + "', 'time')";

    static final String INSERT = "INSERT INTO " + TABLE
            + " (time, modelpackageuri, model, provider, service, resource, value_kind, java_type, value_num, value_json)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?::jsonb)";

    static final List<String> MIGRATE_LEGACY = List.of(
            "INSERT INTO " + TABLE
                    + " (time, modelpackageuri, model, provider, service, resource, value_kind, value_num)"
                    + " SELECT time, modelpackageuri, model, provider, service, resource, "
                    + ValueKind.NUMBER.ordinal() + ", data FROM sensinact.numeric_data",
            "INSERT INTO " + TABLE
                    + " (time, modelpackageuri, model, provider, service, resource, value_kind, java_type, value_json)"
                    + " SELECT time, modelpackageuri, model, provider, service, resource, "
                    + ValueKind.STRING.ordinal() + ", 'java.lang.String', to_jsonb(data) FROM sensinact.text_data",
            "INSERT INTO " + TABLE
                    + " (time, modelpackageuri, model, provider, service, resource, value_kind, value_json)"
                    + " SELECT time, modelpackageuri, model, provider, service, resource, "
                    + ValueKind.GEOJSON.ordinal() + ", ST_AsGeoJSON(data)::jsonb FROM sensinact.geo_data");

    static final List<String> LEGACY_TABLES = List.of("numeric_data", "text_data", "geo_data");

    private TimescaleSql() {
    }

    static String renameLegacyTable(String legacyTable) {
        return "ALTER TABLE sensinact." + legacyTable + " RENAME TO " + legacyTable + "_migrated";
    }

    static String bucketExpression(boolean timescale) {
        return timescale ? "time_bucket(make_interval(secs => ?), time)"
                : "date_bin(make_interval(secs => ?), time, TIMESTAMPTZ 'epoch')";
    }

    /** Deletes oldest-first so a cap bounds the run deterministically. */
    static String pruneByAge(PruneRequest request) {
        return "DELETE FROM " + TABLE + " WHERE (provider, service, resource, time) IN ("
                + "SELECT provider, service, resource, time FROM " + TABLE + " WHERE time < ?"
                + pathsClause(request) + " ORDER BY time ASC" + capClause(request) + ")";
    }

    static String pruneByKeepCount(PruneRequest request) {
        return "DELETE FROM " + TABLE + " WHERE (provider, service, resource, time) IN ("
                + "SELECT provider, service, resource, time FROM ("
                + "SELECT provider, service, resource, time,"
                + " ROW_NUMBER() OVER (PARTITION BY provider, service, resource ORDER BY time DESC) AS rn FROM "
                + TABLE + " WHERE TRUE" + pathsClause(request) + ") ranked WHERE rn > ? ORDER BY time ASC"
                + capClause(request) + ")";
    }

    private static String capClause(PruneRequest request) {
        return request.maxDelete() == null ? "" : " LIMIT ?";
    }

    private static String pathsClause(PruneRequest request) {
        if (request.paths() == null || request.paths().isEmpty()) {
            return "";
        }
        return " AND (provider, service, resource) IN ("
                + String.join(",", request.paths().stream().map(p -> "(?,?,?)").toList()) + ")";
    }

    static int bindPaths(PreparedStatement ps, PruneRequest request, int index) throws SQLException {
        if (request.paths() != null) {
            for (ResourcePath path : request.paths()) {
                ps.setString(index++, path.provider());
                ps.setString(index++, path.service());
                ps.setString(index++, path.resource());
            }
        }
        return index;
    }

    /** Composable statement builder: one {@code if} per query dimension. */
    static final class Builder {
        private final StringBuilder sql = new StringBuilder();
        private final List<Object> parameters = new ArrayList<>();

        Builder(String select) {
            sql.append(select).append(" WHERE provider = ? AND service = ? AND resource = ?");
        }

        Builder path(ResourcePath path) {
            parameters.add(path.provider());
            parameters.add(path.service());
            parameters.add(path.resource());
            return this;
        }

        Builder range(TimeRange range) {
            if (range.from() != null) {
                sql.append(" AND time >").append(range.fromInclusive() ? "= ?" : " ?");
                parameters.add(Timestamp.from(range.from()));
            }
            if (range.to() != null) {
                sql.append(" AND time <").append(range.toInclusive() ? "= ?" : " ?");
                parameters.add(Timestamp.from(range.to()));
            }
            return this;
        }

        Builder valueFilter(ValueFilter filter) {
            if (filter != null) {
                for (ValueFilter.Condition condition : filter.conditions()) {
                    String op = switch (condition.op()) {
                    case EQ -> "=";
                    case NE -> "<>";
                    case LT -> "<";
                    case LE -> "<=";
                    case GT -> ">";
                    case GE -> ">=";
                    };
                    sql.append(" AND value_num ").append(op).append(" ?::numeric");
                    parameters.add(TimescaleValueCodec.numericLiteral(condition.literal()));
                }
            }
            return this;
        }

        Builder prefixParameter(Object parameter) {
            parameters.add(0, parameter);
            return this;
        }

        Builder append(String fragment, Object... params) {
            sql.append(fragment);
            for (Object parameter : params) {
                parameters.add(parameter);
            }
            return this;
        }

        String sql() {
            return sql.toString();
        }

        List<Object> parameters() {
            return List.copyOf(parameters);
        }

        PreparedStatement prepare(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql.toString());
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            return ps;
        }
    }
}
