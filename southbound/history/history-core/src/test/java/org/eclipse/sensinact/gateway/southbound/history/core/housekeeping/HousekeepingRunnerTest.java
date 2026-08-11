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
package org.eclipse.sensinact.gateway.southbound.history.core.housekeeping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HousekeepingRunnerTest {

    private static final ResourcePath PATH = new ResourcePath("p", "s", "r");
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant NOW = T0.plus(Duration.ofDays(10));

    private InMemoryHistoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryHistoryStorage(10_000);
        storage.store(IntStream.range(0, 10)
                .mapToObj(i -> new HistoricalRecord("uri", "model", PATH, T0.plus(Duration.ofDays(i)),
                        ValueKind.NUMBER, (long) i))
                .toList());
    }

    private long count() {
        return storage.count(PATH, TimeRange.ALL);
    }

    @Test
    void retentionPolicyDeletesOldRecords() {
        HousekeepingPolicy policy = new HousekeepingPolicy("age", List.of(), Duration.ofDays(5), null, null,
                Duration.ofHours(24));

        long deleted = HousekeepingRunner.run(policy, Map.of("test", storage), NOW);

        assertEquals(5, deleted);
        assertEquals(5, count());
    }

    @Test
    void keepCountPolicyKeepsNewestRecords() {
        HousekeepingPolicy policy = new HousekeepingPolicy("keep", List.of(), null, 3L, null, Duration.ofHours(24));

        long deleted = HousekeepingRunner.run(policy, Map.of("test", storage), NOW);

        assertEquals(7, deleted);
        assertEquals(3, count());
    }

    @Test
    void maxDeleteCapsARun() {
        HousekeepingPolicy policy = new HousekeepingPolicy("capped", List.of(), Duration.ofDays(1), null, 4L,
                Duration.ofHours(24));

        long deleted = HousekeepingRunner.run(policy, Map.of("test", storage), NOW);

        assertEquals(4, deleted);
        assertEquals(6, count());
    }

    @Test
    void targetScopingSkipsOtherProviders() {
        HousekeepingPolicy policy = new HousekeepingPolicy("scoped", List.of("other"), Duration.ofDays(1), null,
                null, Duration.ofHours(24));

        long deleted = HousekeepingRunner.run(policy, Map.of("test", storage), NOW);

        assertEquals(0, deleted);
        assertEquals(10, count());
    }

    @Test
    void backendFailureIsContained() {
        InMemoryHistoryStorage broken = new InMemoryHistoryStorage(10) {
            @Override
            public long prune(PruneRequest request) {
                throw new IllegalStateException("backend down");
            }
        };
        HousekeepingPolicy policy = new HousekeepingPolicy("mixed", List.of(), Duration.ofDays(5), null, null,
                Duration.ofHours(24));

        long deleted = HousekeepingRunner.run(policy, Map.of("broken", broken, "test", storage), NOW);

        assertEquals(5, deleted);
    }

    @Test
    void policyWithoutAnyBoundIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new HousekeepingPolicy("invalid", List.of(), null, null, null, Duration.ofHours(24)));
    }
}
