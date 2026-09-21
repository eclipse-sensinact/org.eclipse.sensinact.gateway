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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter.Op;
import org.eclipse.sensinact.gateway.southbound.history.storage.PruneRequest;
import org.junit.jupiter.api.Test;

class TimescaleSqlTest {

    private static final ResourcePath PATH = new ResourcePath("p", "s", "r");
    private static final Instant T0 = Instant.parse("2020-01-01T00:00:00Z");

    @Test
    void baseSelectFiltersByPath() {
        TimescaleSql.Builder builder = new TimescaleSql.Builder("SELECT x FROM t").path(PATH);

        assertEquals("SELECT x FROM t WHERE provider = ? AND service = ? AND resource = ?", builder.sql());
        assertEquals(List.of("p", "s", "r"), builder.parameters());
    }

    @Test
    void unboundedRangeAddsNothing() {
        TimescaleSql.Builder builder = new TimescaleSql.Builder("S").path(PATH).range(TimeRange.ALL);

        assertFalse(builder.sql().contains("time >"));
        assertFalse(builder.sql().contains("time <"));
        assertEquals(3, builder.parameters().size());
    }

    @Test
    void inclusiveAndExclusiveBoundsProduceDistinctOperators() {
        TimescaleSql.Builder inclusive = new TimescaleSql.Builder("S").path(PATH)
                .range(TimeRange.closed(T0, T0.plusSeconds(10)));
        TimescaleSql.Builder exclusive = new TimescaleSql.Builder("S").path(PATH)
                .range(new TimeRange(T0, false, T0.plusSeconds(10), false));

        assertTrue(inclusive.sql().contains("time >= ?"));
        assertTrue(inclusive.sql().contains("time <= ?"));
        assertTrue(exclusive.sql().contains("time > ?"));
        assertTrue(exclusive.sql().contains("time < ?"));
        assertEquals(List.of("p", "s", "r", Timestamp.from(T0), Timestamp.from(T0.plusSeconds(10))),
                inclusive.parameters());
    }

    @Test
    void valueFilterAppendsOneConditionPerComparison() {
        ValueFilter filter = new ValueFilter(List.of(new ValueFilter.Condition(Op.GT, 5L),
                new ValueFilter.Condition(Op.LE, new BigDecimal("7.5"))));

        TimescaleSql.Builder builder = new TimescaleSql.Builder("S").path(PATH).valueFilter(filter);

        assertTrue(builder.sql().contains("value_num > ?::numeric"));
        assertTrue(builder.sql().contains("value_num <= ?::numeric"));
        assertEquals(List.of("p", "s", "r", "5", "7.5"), builder.parameters());
    }

    @Test
    void dimensionsComposeIntoOneStatement() {
        TimescaleSql.Builder builder = new TimescaleSql.Builder("S").path(PATH).range(TimeRange.atOrAfter(T0))
                .valueFilter(ValueFilter.of(Op.NE, 0L)).append(" ORDER BY time DESC")
                .append(" OFFSET ? LIMIT ?", 10L, 5);

        assertEquals("S WHERE provider = ? AND service = ? AND resource = ? AND time >= ?"
                + " AND value_num <> ?::numeric ORDER BY time DESC OFFSET ? LIMIT ?", builder.sql());
        assertEquals(List.of("p", "s", "r", Timestamp.from(T0), "0", 10L, 5), builder.parameters());
    }

    @Test
    void prefixParameterLeadsTheParameterList() {
        TimescaleSql.Builder builder = new TimescaleSql.Builder("S").path(PATH).prefixParameter(3600d);

        assertEquals(List.of(3600d, "p", "s", "r"), builder.parameters());
    }

    @Test
    void pruneByAgeWithoutScopeAndCap() {
        PruneRequest request = new PruneRequest(null, T0, null);

        String sql = TimescaleSql.pruneByAge(request);

        assertTrue(sql.contains("WHERE time < ? ORDER BY time ASC)"));
        assertFalse(sql.contains("LIMIT"));
        assertFalse(sql.contains("(provider, service, resource) IN"));
    }

    @Test
    void pruneByAgeWithPathsAndCap() {
        PruneRequest request = new PruneRequest(List.of(PATH, new ResourcePath("p2", "s2", "r2")), T0, null, 100L);

        String sql = TimescaleSql.pruneByAge(request);

        assertTrue(sql.contains("(provider, service, resource) IN ((?,?,?),(?,?,?))"));
        assertTrue(sql.endsWith("ORDER BY time ASC LIMIT ?)"));
    }

    @Test
    void pruneByKeepCountRanksPerResource() {
        PruneRequest request = new PruneRequest(null, null, 5L, 10L);

        String sql = TimescaleSql.pruneByKeepCount(request);

        assertTrue(sql.contains("ROW_NUMBER() OVER (PARTITION BY provider, service, resource ORDER BY time DESC)"));
        assertTrue(sql.contains("WHERE rn > ?"));
        assertTrue(sql.endsWith("LIMIT ?)"));
    }

    @Test
    void bucketExpressionSwitchesOnExtension() {
        assertTrue(TimescaleSql.bucketExpression(true).startsWith("time_bucket"));
        assertTrue(TimescaleSql.bucketExpression(false).startsWith("date_bin"));
    }

    @Test
    void legacyTableRename() {
        assertEquals("ALTER TABLE sensinact.text_data RENAME TO text_data_migrated",
                TimescaleSql.renameLegacyTable("text_data"));
    }
}
