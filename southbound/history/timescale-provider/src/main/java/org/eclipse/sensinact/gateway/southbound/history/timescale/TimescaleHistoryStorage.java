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
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateBucket;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateFunction;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.storage.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL/TimescaleDB {@link HistoryStorage} on the unified schema decided
 * in the storage ADR (see docs/source/southbound/history/history-storage-adr.md).
 * With the TimescaleDB extension the table becomes a hypertable and
 * aggregation uses {@code time_bucket}; plain PostgreSQL falls back to
 * {@code date_bin}. SQL text lives in {@link TimescaleSql}, value conversion
 * in {@link TimescaleValueCodec}.
 */
public class TimescaleHistoryStorage implements HistoryStorage {

    /** Runs an operation within a transaction (tx-control in production). */
    public interface TxRunner {
        <T> T inTransaction(Callable<T> operation);
    }

    private static final Logger logger = LoggerFactory.getLogger(TimescaleHistoryStorage.class);

    private final TxRunner tx;
    private final Supplier<Connection> connection;
    private final int maxPageSize;
    private final TimescaleValueCodec codec = new TimescaleValueCodec();

    private boolean timescale;

    public TimescaleHistoryStorage(TxRunner tx, Supplier<Connection> connection, int maxPageSize) {
        this.tx = tx;
        this.connection = connection;
        this.maxPageSize = maxPageSize;
    }

    /** Creates the schema, enables the extension if available, migrates legacy data. */
    public void initialize() {
        tx.inTransaction(() -> {
            Connection conn = connection.get();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS sensinact");
                timescale = tryEnableTimescale(stmt);
                if (!tableExists(conn, "history")) {
                    stmt.execute(TimescaleSql.CREATE_TABLE);
                    stmt.execute(TimescaleSql.CREATE_INDEX);
                    if (timescale) {
                        stmt.execute(TimescaleSql.CREATE_HYPERTABLE);
                    }
                    migrateLegacyTables(conn, stmt);
                }
            }
            return null;
        });
        logger.info("History schema ready (TimescaleDB {})", timescale ? "enabled" : "not available");
    }

    /**
     * Probes availability read-only first: a failed CREATE EXTENSION would
     * abort the surrounding transaction on plain PostgreSQL.
     */
    private static boolean tryEnableTimescale(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt
                .executeQuery("SELECT 1 FROM pg_available_extensions WHERE name = 'timescaledb'")) {
            if (!rs.next()) {
                logger.info("TimescaleDB extension not available, using plain PostgreSQL");
                return false;
            }
        }
        stmt.execute("CREATE EXTENSION IF NOT EXISTS timescaledb");
        return true;
    }

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, "sensinact", table, null)) {
            return rs.next();
        }
    }

    /**
     * One-time migration of the pre-rework schema: numeric rows keep no Java
     * type (legacy read shape), geo rows become GeoJSON documents. The old
     * tables are renamed, not dropped.
     */
    private void migrateLegacyTables(Connection conn, Statement stmt) throws SQLException {
        if (!tableExists(conn, TimescaleSql.LEGACY_TABLES.get(0))) {
            return;
        }
        logger.info("Migrating legacy history tables to the unified schema");
        for (String migration : TimescaleSql.MIGRATE_LEGACY) {
            stmt.execute(migration);
        }
        for (String legacyTable : TimescaleSql.LEGACY_TABLES) {
            stmt.execute(TimescaleSql.renameLegacyTable(legacyTable));
        }
    }

    boolean isTimescale() {
        return timescale;
    }

    @Override
    public Set<HistoryCapability> capabilities() {
        return Set.of(HistoryCapability.AGGREGATION, HistoryCapability.VALUE_FILTERING);
    }

    @Override
    public int maxPageSize() {
        return maxPageSize;
    }

    @Override
    public void store(List<HistoricalRecord> records) {
        tx.inTransaction(() -> {
            try (PreparedStatement ps = connection.get().prepareStatement(TimescaleSql.INSERT)) {
                for (HistoricalRecord record : records) {
                    ps.setTimestamp(1, Timestamp.from(record.timestamp()));
                    ps.setString(2, record.modelPackageUri());
                    ps.setString(3, record.model());
                    ps.setString(4, record.path().provider());
                    ps.setString(5, record.path().service());
                    ps.setString(6, record.path().resource());
                    ps.setShort(7, (short) record.kind().ordinal());
                    ps.setString(8, record.value() == null ? null : record.value().getClass().getName());
                    ps.setString(9,
                            record.kind() == ValueKind.NUMBER ? TimescaleValueCodec.numericLiteral(record.value())
                                    : null);
                    ps.setString(10, codec.jsonValue(record));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return null;
        });
    }

    @Override
    public Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at) {
        return singleRow(new TimescaleSql.Builder(TimescaleSql.SELECT_ROW).path(path)
                .append(" AND time <= ? ORDER BY time DESC LIMIT 1", Timestamp.from(at)));
    }

    @Override
    public Optional<TimedValue<?>> firstValue(ResourcePath path) {
        return singleRow(new TimescaleSql.Builder(TimescaleSql.SELECT_ROW).path(path)
                .append(" ORDER BY time ASC LIMIT 1"));
    }

    @Override
    public Optional<TimedValue<?>> latestValue(ResourcePath path) {
        return singleRow(new TimescaleSql.Builder(TimescaleSql.SELECT_ROW).path(path)
                .append(" ORDER BY time DESC LIMIT 1"));
    }

    private Optional<TimedValue<?>> singleRow(TimescaleSql.Builder query) {
        return tx.inTransaction(() -> {
            try (PreparedStatement ps = query.prepare(connection.get()); ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional
                        .of(new DefaultTimedValue<>(codec.reconstruct(rs), rs.getTimestamp("time").toInstant()));
            }
        });
    }

    @Override
    public long count(ResourcePath path, TimeRange range, ValueFilter valueFilter) {
        TimescaleSql.Builder query = new TimescaleSql.Builder("SELECT COUNT(*) FROM " + TimescaleSql.TABLE)
                .path(path).range(range).valueFilter(valueFilter);
        return tx.inTransaction(() -> {
            try (PreparedStatement ps = query.prepare(connection.get()); ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        });
    }

    @Override
    public HistoryPage values(HistoryQuery historyQuery) {
        int limit = historyQuery.limit() == HistoryQuery.PROVIDER_DEFAULT_LIMIT ? maxPageSize
                : Math.min(historyQuery.limit(), maxPageSize);
        TimescaleSql.Builder query = new TimescaleSql.Builder(TimescaleSql.SELECT_ROW).path(historyQuery.path())
                .range(historyQuery.range()).valueFilter(historyQuery.valueFilter())
                .append(" ORDER BY time " + (historyQuery.order() == SortOrder.DESCENDING ? "DESC" : "ASC"))
                .append(" OFFSET ? LIMIT ?", historyQuery.offset(), limit + 1);

        return tx.inTransaction(() -> {
            List<TimedValue<?>> values = new ArrayList<>();
            boolean hasMore = false;
            try (PreparedStatement ps = query.prepare(connection.get()); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (values.size() == limit) {
                        hasMore = true;
                        break;
                    }
                    values.add(new DefaultTimedValue<>(codec.reconstruct(rs), rs.getTimestamp("time").toInstant()));
                }
            }
            return HistoryPage.of(values, historyQuery.offset(), hasMore);
        });
    }

    @Override
    public List<AggregateBucket> aggregate(AggregationQuery aggregation) {
        TimescaleSql.Builder query = new TimescaleSql.Builder("SELECT " + TimescaleSql.bucketExpression(timescale)
                + " AS bucket, COUNT(*) AS cnt, MIN(value_num) AS mn, MAX(value_num) AS mx,"
                + " AVG(value_num) AS av, SUM(value_num) AS sm FROM " + TimescaleSql.TABLE)
                .prefixParameter((double) aggregation.bucketSize().toSeconds())
                .path(aggregation.path()).range(aggregation.range()).append(" GROUP BY bucket ORDER BY bucket");

        boolean needsEdges = aggregation.functions().contains(AggregateFunction.FIRST)
                || aggregation.functions().contains(AggregateFunction.LAST);

        return tx.inTransaction(() -> {
            List<AggregateBucket> buckets = new ArrayList<>();
            try (PreparedStatement ps = query.prepare(connection.get()); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<AggregateFunction, Object> results = new EnumMap<>(AggregateFunction.class);
                    for (AggregateFunction function : aggregation.functions()) {
                        Object value = switch (function) {
                        case COUNT -> rs.getLong("cnt");
                        case MIN -> rs.getBigDecimal("mn");
                        case MAX -> rs.getBigDecimal("mx");
                        case AVG -> rs.getBigDecimal("av");
                        case SUM -> rs.getBigDecimal("sm");
                        case FIRST, LAST -> null;
                        };
                        if (value != null) {
                            results.put(function, value);
                        }
                    }
                    buckets.add(new AggregateBucket(rs.getTimestamp("bucket").toInstant(), results));
                }
            }
            if (needsEdges) {
                resolveBucketEdges(aggregation, buckets);
            }
            return buckets;
        });
    }

    private void resolveBucketEdges(AggregationQuery aggregation, List<AggregateBucket> buckets)
            throws SQLException {
        for (int i = 0; i < buckets.size(); i++) {
            AggregateBucket bucket = buckets.get(i);
            Instant start = bucket.bucketStart();
            Instant end = start.plus(aggregation.bucketSize());
            Map<AggregateFunction, Object> results = new EnumMap<>(AggregateFunction.class);
            results.putAll(bucket.results());
            if (aggregation.functions().contains(AggregateFunction.FIRST)) {
                edgeValue(aggregation.path(), start, end, true)
                        .ifPresent(value -> results.put(AggregateFunction.FIRST, value));
            }
            if (aggregation.functions().contains(AggregateFunction.LAST)) {
                edgeValue(aggregation.path(), start, end, false)
                        .ifPresent(value -> results.put(AggregateFunction.LAST, value));
            }
            buckets.set(i, new AggregateBucket(start, results));
        }
    }

    private Optional<Object> edgeValue(ResourcePath path, Instant start, Instant end, boolean first)
            throws SQLException {
        TimescaleSql.Builder query = new TimescaleSql.Builder(TimescaleSql.SELECT_ROW).path(path)
                .range(new TimeRange(start, true, end, false))
                .append(" ORDER BY time " + (first ? "ASC" : "DESC") + " LIMIT 1");
        try (PreparedStatement ps = query.prepare(connection.get()); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(codec.reconstruct(rs));
        }
    }

    @Override
    public long prune(PruneRequest request) {
        return tx.inTransaction(() -> {
            long deleted = 0;
            Long remainingCap = request.maxDelete();
            if (request.olderThan() != null) {
                deleted += pruneByAge(request, remainingCap);
                if (remainingCap != null) {
                    remainingCap = Math.max(0, remainingCap - deleted);
                }
            }
            if (request.keepLatestPerResource() != null && (remainingCap == null || remainingCap > 0)) {
                deleted += pruneByKeepCount(request, remainingCap);
            }
            return deleted;
        });
    }

    private long pruneByAge(PruneRequest request, Long cap) throws SQLException {
        try (PreparedStatement ps = connection.get().prepareStatement(TimescaleSql.pruneByAge(request))) {
            int index = 1;
            ps.setTimestamp(index++, Timestamp.from(request.olderThan()));
            index = TimescaleSql.bindPaths(ps, request, index);
            if (request.maxDelete() != null) {
                ps.setLong(index, cap);
            }
            return ps.executeUpdate();
        }
    }

    private long pruneByKeepCount(PruneRequest request, Long cap) throws SQLException {
        try (PreparedStatement ps = connection.get().prepareStatement(TimescaleSql.pruneByKeepCount(request))) {
            int index = TimescaleSql.bindPaths(ps, request, 1);
            ps.setLong(index++, request.keepLatestPerResource());
            if (request.maxDelete() != null) {
                ps.setLong(index, cap);
            }
            return ps.executeUpdate();
        }
    }
}
