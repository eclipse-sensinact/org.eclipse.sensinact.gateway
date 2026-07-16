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
package org.eclipse.sensinact.gateway.southbound.history.spikes.postgres;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Bucket;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Function;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueKind;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Candidate A/B: unified single-table PostgreSQL schema — type discriminator,
 * exact Java type column, NUMERIC for numbers, JSONB for everything
 * structured. With {@code timescale=true} the table becomes a hypertable and
 * aggregation uses time_bucket, otherwise plain PostgreSQL date_bin.
 *
 * Spike-grade: single connection, (path, time) assumed unique, no geometry
 * column yet (GeoJSON round-trips via JSONB; spatial indexing is a separate
 * measurement).
 */
public class PostgresUnifiedHistoryStorage implements HistoryStorage {

    private static final String TABLE = "sensinact_spike.history";

    private final Connection connection;
    private final boolean timescale;
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build();

    public PostgresUnifiedHistoryStorage(Connection connection, boolean timescale) throws SQLException {
        this.connection = connection;
        this.timescale = timescale;
        setupSchema();
    }

    private void setupSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS sensinact_spike");
            stmt.execute("DROP TABLE IF EXISTS " + TABLE);
            stmt.execute("CREATE TABLE " + TABLE + " ("
                    + "time TIMESTAMPTZ NOT NULL,"
                    + "modelpackageuri VARCHAR(128), model VARCHAR(128),"
                    + "provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL,"
                    + "value_kind SMALLINT NOT NULL,"
                    + "java_type VARCHAR(128),"
                    + "value_num NUMERIC,"
                    + "value_json JSONB)");
            stmt.execute("CREATE INDEX ON " + TABLE + " (provider, service, resource, time DESC)");
            if (timescale) {
                stmt.execute("CREATE EXTENSION IF NOT EXISTS timescaledb");
                stmt.execute("SELECT create_hypertable('" + TABLE + "', 'time')");
            }
        }
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.AGGREGATION, Capability.VALUE_FILTERING);
    }

    @Override
    public int maxPageSize() {
        return 10_000;
    }

    @Override
    public void store(List<HistoricalRecord> records) {
        String sql = "INSERT INTO " + TABLE
                + " (time, modelpackageuri, model, provider, service, resource, value_kind, java_type, value_num, value_json)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?::jsonb)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (HistoricalRecord record : records) {
                ps.setTimestamp(1, Timestamp.from(record.timestamp()));
                ps.setString(2, record.modelPackageUri());
                ps.setString(3, record.model());
                ps.setString(4, record.path().provider());
                ps.setString(5, record.path().service());
                ps.setString(6, record.path().resource());
                ps.setShort(7, (short) record.kind().ordinal());
                ps.setString(8, record.value() == null ? null : record.value().getClass().getName());
                ps.setString(9, record.kind() == ValueKind.NUMBER ? numericLiteral(record.value()) : null);
                ps.setString(10, jsonValue(record));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("store failed", e);
        }
    }

    private static String numericLiteral(Object value) {
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
        if (value instanceof Float f) {
            if (f.isNaN()) {
                return "NaN";
            }
            if (f.isInfinite()) {
                return f > 0 ? "Infinity" : "-Infinity";
            }
        }
        if (value instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        return value.toString();
    }

    private String jsonValue(HistoricalRecord record) {
        if (record.value() == null || record.kind() == ValueKind.NUMBER) {
            return null;
        }
        return mapper.writeValueAsString(record.value());
    }

    private Object reconstruct(ResultSet rs) throws SQLException {
        ValueKind kind = ValueKind.values()[rs.getShort("value_kind")];
        String javaType = rs.getString("java_type");
        if (javaType == null) {
            return null;
        }
        return switch (kind) {
        case NUMBER -> reconstructNumber(rs.getString("value_num"), javaType);
        case BOOLEAN -> Boolean.valueOf(rs.getString("value_json"));
        case STRING -> mapper.readValue(rs.getString("value_json"), String.class);
        case GEOJSON -> mapper.readValue(rs.getString("value_json"), GeoJsonObject.class);
        case OBJECT -> mapper.readValue(rs.getString("value_json"), Object.class);
        };
    }

    private static Object reconstructNumber(String literal, String javaType) {
        return switch (javaType) {
        case "java.lang.Long" -> Long.valueOf(literal);
        case "java.lang.Integer" -> Integer.valueOf(literal);
        case "java.lang.Short" -> Short.valueOf(literal);
        case "java.lang.Byte" -> Byte.valueOf(literal);
        case "java.lang.Double" -> parseDouble(literal);
        case "java.lang.Float" -> parseDouble(literal).floatValue();
        case "java.math.BigInteger" -> new BigInteger(literal);
        case "java.math.BigDecimal" -> new BigDecimal(literal);
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

    /** Composable WHERE builder: one {@code if} per query dimension. */
    private static final class Query {
        final StringBuilder sql = new StringBuilder();
        final List<Object> parameters = new ArrayList<>();

        Query(String select) {
            sql.append(select).append(" WHERE provider = ? AND service = ? AND resource = ?");
        }

        Query path(ResourcePath path) {
            parameters.add(path.provider());
            parameters.add(path.service());
            parameters.add(path.resource());
            return this;
        }

        Query range(TimeRange range) {
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

        Query valueFilter(ValueFilter filter) {
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
                    parameters.add(numericLiteral(condition.literal()));
                }
            }
            return this;
        }

        Query append(String fragment, Object... params) {
            sql.append(fragment);
            for (Object p : params) {
                parameters.add(p);
            }
            return this;
        }

        PreparedStatement prepare(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(sql.toString());
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            return ps;
        }
    }

    private static final String SELECT_ROW = "SELECT time, value_kind, java_type, value_num::text AS value_num, value_json::text AS value_json FROM "
            + TABLE;

    @Override
    public Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at) {
        return singleRow(new Query(SELECT_ROW).path(path)
                .append(" AND time <= ? ORDER BY time DESC LIMIT 1", Timestamp.from(at)));
    }

    @Override
    public Optional<TimedValue<?>> firstValue(ResourcePath path) {
        return singleRow(new Query(SELECT_ROW).path(path).append(" ORDER BY time ASC LIMIT 1"));
    }

    @Override
    public Optional<TimedValue<?>> latestValue(ResourcePath path) {
        return singleRow(new Query(SELECT_ROW).path(path).append(" ORDER BY time DESC LIMIT 1"));
    }

    private Optional<TimedValue<?>> singleRow(Query query) {
        try (PreparedStatement ps = query.prepare(connection); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(new DefaultTimedValue<>(reconstruct(rs), rs.getTimestamp("time").toInstant()));
        } catch (SQLException e) {
            throw new IllegalStateException("query failed: " + query.sql, e);
        }
    }

    @Override
    public long count(ResourcePath path, TimeRange range) {
        Query query = new Query("SELECT COUNT(*) FROM " + TABLE).path(path).range(range);
        try (PreparedStatement ps = query.prepare(connection); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException("count failed", e);
        }
    }

    @Override
    public HistoryPage values(HistoryQuery historyQuery) {
        int limit = Math.min(historyQuery.limit(), maxPageSize());
        Query query = new Query(SELECT_ROW).path(historyQuery.path()).range(historyQuery.range())
                .valueFilter(historyQuery.valueFilter())
                .append(" ORDER BY time " + (historyQuery.order() == SortOrder.DESCENDING ? "DESC" : "ASC"))
                .append(" OFFSET ? LIMIT ?", historyQuery.offset(), limit + 1);

        List<TimedValue<?>> values = new ArrayList<>();
        boolean hasMore = false;
        try (PreparedStatement ps = query.prepare(connection); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (values.size() == limit) {
                    hasMore = true;
                    break;
                }
                values.add(new DefaultTimedValue<>(reconstruct(rs), rs.getTimestamp("time").toInstant()));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("values failed: " + query.sql, e);
        }
        return new HistoryPage(values, historyQuery.offset(), hasMore);
    }

    @Override
    public List<Bucket> aggregate(AggregationQuery aggregation) {
        String bucketExpr = timescale ? "time_bucket(make_interval(secs => ?), time)"
                : "date_bin(make_interval(secs => ?), time, TIMESTAMPTZ 'epoch')";
        Query query = new Query("SELECT " + bucketExpr + " AS bucket,"
                + " COUNT(*) AS cnt, MIN(value_num) AS mn, MAX(value_num) AS mx, AVG(value_num) AS av, SUM(value_num) AS sm FROM "
                + TABLE);
        query.parameters.add(0, (double) aggregation.bucketSize().toSeconds());
        query.path(aggregation.path()).range(aggregation.range()).append(" GROUP BY bucket ORDER BY bucket");

        List<Bucket> buckets = new ArrayList<>();
        try (PreparedStatement ps = query.prepare(connection); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<Function, Object> results = new EnumMap<>(Function.class);
                for (Function fn : aggregation.functions()) {
                    results.put(fn, switch (fn) {
                    case COUNT -> rs.getLong("cnt");
                    case MIN -> rs.getBigDecimal("mn");
                    case MAX -> rs.getBigDecimal("mx");
                    case AVG -> rs.getBigDecimal("av");
                    case SUM -> rs.getBigDecimal("sm");
                    });
                }
                buckets.add(new Bucket(rs.getTimestamp("bucket").toInstant(), results));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("aggregate failed", e);
        }
        return buckets;
    }

    @Override
    public long prune(PruneRequest request) {
        long deleted = 0;
        String scope = request.scope() == null ? "TRUE" : "provider = ? AND service = ? AND resource = ?";
        try {
            if (request.olderThan() != null) {
                try (PreparedStatement ps = connection
                        .prepareStatement("DELETE FROM " + TABLE + " WHERE " + scope + " AND time < ?")) {
                    int i = bindScope(ps, request, 1);
                    ps.setTimestamp(i, Timestamp.from(request.olderThan()));
                    deleted += ps.executeUpdate();
                }
            }
            if (request.keepLatestPerResource() != null) {
                String sql = "DELETE FROM " + TABLE + " h USING ("
                        + "SELECT provider, service, resource, time,"
                        + " ROW_NUMBER() OVER (PARTITION BY provider, service, resource ORDER BY time DESC) AS rn"
                        + " FROM " + TABLE + " WHERE " + scope + ") ranked"
                        + " WHERE h.provider = ranked.provider AND h.service = ranked.service"
                        + " AND h.resource = ranked.resource AND h.time = ranked.time AND ranked.rn > ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    int i = bindScope(ps, request, 1);
                    ps.setLong(i, request.keepLatestPerResource());
                    deleted += ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("prune failed", e);
        }
        return deleted;
    }

    private static int bindScope(PreparedStatement ps, PruneRequest request, int index) throws SQLException {
        if (request.scope() != null) {
            ps.setString(index++, request.scope().provider());
            ps.setString(index++, request.scope().service());
            ps.setString(index++, request.scope().resource());
        }
        return index;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
