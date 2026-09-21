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
package org.eclipse.sensinact.gateway.southbound.history.core.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ChangeCondition.Mode;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.junit.jupiter.api.Test;

class ChangeConditionTest {

    private static final ResourcePath PATH = new ResourcePath("p", "s", "temperature");
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static HistoricalRecord numberAt(Object value, Instant timestamp) {
        return new HistoricalRecord("uri", "model", PATH, timestamp, ValueKind.NUMBER, value);
    }

    private static Optional<TimedValue<?>> stored(Object value, Instant timestamp) {
        return Optional.of(new DefaultTimedValue<>(value, timestamp));
    }

    private static ChangeCondition deadband(String threshold) {
        return new ChangeCondition(Mode.DEADBAND, new BigDecimal(threshold), null, null);
    }

    @Test
    void firstUpdateIsAlwaysStored() {
        assertTrue(deadband("0.5").shouldStore(numberAt(21.0d, T0), Optional.empty()));
    }

    @Test
    void deadbandSuppressesSmallDrift() {
        ChangeCondition condition = deadband("0.5");

        assertFalse(condition.shouldStore(numberAt(21.2d, T0.plusSeconds(10)), stored(21.0d, T0)));
        assertTrue(condition.shouldStore(numberAt(22.0d, T0.plusSeconds(20)), stored(21.0d, T0)));
    }

    @Test
    void driftAccumulatesAgainstLastStoredValue() {
        ChangeCondition condition = deadband("0.5");
        Optional<TimedValue<?>> lastStored = stored(21.0d, T0);

        assertFalse(condition.shouldStore(numberAt(21.2d, T0.plusSeconds(10)), lastStored));
        assertFalse(condition.shouldStore(numberAt(21.4d, T0.plusSeconds(20)), lastStored));
        assertTrue(condition.shouldStore(numberAt(21.5d, T0.plusSeconds(30)), lastStored));
    }

    @Test
    void percentThresholdIsRelativeToLastStoredValue() {
        ChangeCondition condition = new ChangeCondition(Mode.DEADBAND, null, new BigDecimal("10"), null);

        assertFalse(condition.shouldStore(numberAt(107.0d, T0.plusSeconds(10)), stored(100.0d, T0)));
        assertTrue(condition.shouldStore(numberAt(110.0d, T0.plusSeconds(20)), stored(100.0d, T0)));
    }

    @Test
    void heartbeatStoresFlatSignal() {
        ChangeCondition condition = new ChangeCondition(Mode.DEADBAND, new BigDecimal("0.5"), null,
                Duration.ofMinutes(5));

        assertFalse(condition.shouldStore(numberAt(21.0d, T0.plusSeconds(60)), stored(21.0d, T0)));
        assertTrue(condition.shouldStore(numberAt(21.0d, T0.plus(Duration.ofMinutes(5))), stored(21.0d, T0)));
    }

    @Test
    void onChangeComparesEquality() {
        ChangeCondition condition = new ChangeCondition(Mode.ON_CHANGE, null, null, null);

        assertFalse(condition.shouldStore(numberAt(21.0d, T0.plusSeconds(10)), stored(21.0d, T0)));
        assertTrue(condition.shouldStore(numberAt(21.1d, T0.plusSeconds(20)), stored(21.0d, T0)));
    }

    @Test
    void deadbandDegradesToOnChangeForNonNumericValues() {
        ChangeCondition condition = deadband("0.5");
        HistoricalRecord text = new HistoricalRecord("uri", "model", PATH, T0.plusSeconds(10), ValueKind.STRING,
                "on");

        assertFalse(condition.shouldStore(text, stored("on", T0)));
        assertTrue(condition.shouldStore(
                new HistoricalRecord("uri", "model", PATH, T0.plusSeconds(20), ValueKind.STRING, "off"),
                stored("on", T0)));
    }

    @Test
    void deadbandWithoutThresholdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ChangeCondition(Mode.DEADBAND, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ChangeCondition(Mode.DEADBAND, BigDecimal.ONE, BigDecimal.ONE, null));
    }
}
