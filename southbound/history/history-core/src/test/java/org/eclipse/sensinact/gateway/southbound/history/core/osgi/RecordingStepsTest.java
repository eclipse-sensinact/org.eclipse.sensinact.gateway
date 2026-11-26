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
package org.eclipse.sensinact.gateway.southbound.history.core.osgi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStepFactory;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.junit.jupiter.api.Test;

class RecordingStepsTest {

    @Test
    void givenNoProviderNamePropertyWhenCheckedThenFactoryServesEveryProvider() {
        assertTrue(RecordingSteps.serves(null, "any"));
    }

    @Test
    void givenSingleNameWhenCheckedThenOnlyThatProviderIsServed() {
        assertTrue(RecordingSteps.serves("a", "a"));
        assertFalse(RecordingSteps.serves("a", "b"));
    }

    @Test
    void givenNameArrayWhenCheckedThenAnyListedProviderIsServed() {
        assertTrue(RecordingSteps.serves(new String[] { "a", "b" }, "b"));
        assertFalse(RecordingSteps.serves(new String[] { "a", "b" }, "c"));
    }

    @Test
    void givenNameCollectionWhenCheckedThenAnyListedProviderIsServed() {
        assertTrue(RecordingSteps.serves(Set.of("a", "b"), "a"));
        assertFalse(RecordingSteps.serves(List.of("a"), "b"));
    }

    @Test
    void givenFactoryReturnsStepWhenCreatedThenItIsUsedWithProviderNameAndStorage() {
        InMemoryHistoryStorage storage = new InMemoryHistoryStorage(10);
        HistoryRecordingStep expected = record -> {
        };
        String[] seenName = new String[1];
        HistoryStorage[] seenStorage = new HistoryStorage[1];
        HistoryRecordingStepFactory factory = context -> {
            seenName[0] = context.providerName();
            seenStorage[0] = context.storage();
            return expected;
        };

        HistoryRecordingStep step = RecordingSteps.create(factory, 1L, "test", storage);

        assertSame(expected, step);
        assertEquals("test", seenName[0]);
        assertSame(storage, seenStorage[0]);
    }

    @Test
    void givenFactoryThrowsWhenCreatedThenNoStepIsGiven() {
        HistoryRecordingStepFactory factory = context -> {
            throw new IllegalStateException("cannot create");
        };

        assertNull(RecordingSteps.create(factory, 1L, "test", new InMemoryHistoryStorage(10)));
    }

    @Test
    void givenFactoryReturnsNullWhenCreatedThenNoStepIsGiven() {
        HistoryRecordingStepFactory factory = context -> null;

        assertNull(RecordingSteps.create(factory, 1L, "test", new InMemoryHistoryStorage(10)));
    }

    @Test
    void givenStepThrowsWhenClosedThenFailureIsContained() {
        boolean[] closed = new boolean[1];
        HistoryRecordingStep step = new HistoryRecordingStep() {
            @Override
            public void record(HistoricalRecord record) {
            }

            @Override
            public void close() {
                closed[0] = true;
                throw new IllegalStateException("cannot close");
            }
        };

        assertDoesNotThrow(() -> RecordingSteps.close(step, "test"));
        assertTrue(closed[0]);
    }
}
