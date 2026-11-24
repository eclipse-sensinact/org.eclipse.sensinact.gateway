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
*   Kentyou - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.junit.jupiter.api.Test;

class DirectRecordingStepTest {

    private static final ResourcePath PATH = new ResourcePath("prov", "svc", "res");

    private static HistoricalRecord record(long value) {
        return new HistoricalRecord("uri", "model", PATH, Instant.parse("2026-01-01T00:00:00Z").plusSeconds(value),
                ValueKind.NUMBER, value);
    }

    @Test
    void givenRecordWhenRecordedThenStorageIsCalledOnceWithThatRecordAlone() {
        List<List<HistoricalRecord>> calls = new ArrayList<>();
        InMemoryHistoryStorage storage = new InMemoryHistoryStorage(10) {
            @Override
            public void store(List<HistoricalRecord> records) {
                calls.add(records);
                super.store(records);
            }
        };
        HistoricalRecord record = record(1);

        new DirectRecordingStep(storage).record(record);

        assertEquals(1, calls.size());
        assertEquals(1, calls.get(0).size());
        assertSame(record, calls.get(0).get(0));
        assertEquals(1, storage.count(PATH, TimeRange.ALL));
    }

    @Test
    void givenSeveralRecordsWhenRecordedThenEachIsStoredByItsOwnCallInOrder() {
        List<Object> values = new ArrayList<>();
        InMemoryHistoryStorage storage = new InMemoryHistoryStorage(10) {
            @Override
            public void store(List<HistoricalRecord> records) {
                records.forEach(record -> values.add(record.value()));
                super.store(records);
            }
        };
        DirectRecordingStep step = new DirectRecordingStep(storage);

        step.record(record(1));
        step.record(record(2));
        step.record(record(3));

        assertEquals(List.of(1L, 2L, 3L), values);
    }

    @Test
    void givenStorageFailsWhenRecordedThenTheFailureReachesTheCaller() {
        InMemoryHistoryStorage storage = new InMemoryHistoryStorage(10) {
            @Override
            public void store(List<HistoricalRecord> records) {
                throw new IllegalStateException("backend down");
            }
        };
        DirectRecordingStep step = new DirectRecordingStep(storage);
        HistoricalRecord record = record(1);

        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> step.record(record));

        assertEquals("backend down", failure.getMessage());
    }

    @Test
    void givenNoStorageWhenCreatedThenItIsRefused() {
        assertThrows(NullPointerException.class, () -> new DirectRecordingStep(null));
    }

    @Test
    void givenStepWhenClosedThenItKeepsWritingToTheStorage() {
        InMemoryHistoryStorage storage = new InMemoryHistoryStorage(10);
        DirectRecordingStep step = new DirectRecordingStep(storage);

        step.close();
        step.record(record(1));

        assertEquals(1, storage.count(PATH, TimeRange.ALL));
    }
}
