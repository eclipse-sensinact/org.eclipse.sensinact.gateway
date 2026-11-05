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
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * Tests the reporting of the errors of a mapping configuration
 */
public class ErrorReporterTest {

    /**
     * Errors not logged, default interval
     */
    private static final DeviceMappingOptionsDTO QUIET = new DeviceMappingOptionsDTO();

    /**
     * Errors logged
     */
    private static final DeviceMappingOptionsDTO LOGGED = new DeviceMappingOptionsDTO();

    static {
        LOGGED.logErrors = true;
    }

    private Logger logger;

    private ErrorReporter reporter;

    @BeforeEach
    void setup() {
        logger = mock(Logger.class, Answers.RETURNS_DEEP_STUBS);
        reporter = new ErrorReporter(logger, null);
    }

    @Test
    void testFirstErrorOfEachTypeLogged() {
        reporter.report(ErrorReporter.Type.PAYLOAD, QUIET, null, "first payload");
        reporter.report(ErrorReporter.Type.PAYLOAD, QUIET, null, "second payload");
        verify(logger, times(1)).atLevel(Level.ERROR);

        // Another type is reported once too, at its own level
        reporter.report(ErrorReporter.Type.LENIENT_INVALID, QUIET, null, "first warning");
        reporter.report(ErrorReporter.Type.LENIENT_INVALID, QUIET, null, "second warning");
        verify(logger, times(1)).atLevel(Level.WARN);

        // A warning doesn't hide the first error of another type
        reporter.report(ErrorReporter.Type.MISSING_VALUE, QUIET, null, "first missing value");
        verify(logger, times(2)).atLevel(Level.ERROR);
    }

    @Test
    void testAllErrorsLoggedWithLogErrors() {
        reporter.report(ErrorReporter.Type.RECORD, LOGGED, null, "first");
        reporter.report(ErrorReporter.Type.RECORD, LOGGED, null, "second");
        reporter.report(ErrorReporter.Type.RECORD, LOGGED, null, "third");
        verify(logger, times(3)).atLevel(Level.ERROR);
        verify(logger, never()).atLevel(Level.WARN);
    }

    @Test
    void testNewReporterLogsAgain() {
        reporter.report(ErrorReporter.Type.UPDATE, QUIET, null, "first");
        // A new configuration gets a new reporter
        new ErrorReporter(logger, null).report(ErrorReporter.Type.UPDATE, QUIET, null, "first again");
        verify(logger, times(2)).atLevel(Level.ERROR);
    }

    @Test
    void testSanitize() {
        assertNull(ErrorReporter.sanitize(null));
        assertEquals("a\\nb\\rc", ErrorReporter.sanitize("a\nb\rc"));
        final String sanitized = ErrorReporter.sanitize("x".repeat(500));
        assertEquals(ErrorReporter.MAX_VALUE_LENGTH + 3, sanitized.length());
    }

    @Test
    void testStackTraceOnlyWithLogErrors() {
        final LoggingEventBuilder builder = mock(LoggingEventBuilder.class);
        when(logger.atLevel(Level.ERROR)).thenReturn(builder);
        final Exception error = new Exception("boom");

        reporter.report(ErrorReporter.Type.PAYLOAD, QUIET, null, "Error: {}", "line1\nline2", error);
        verify(builder).log("Error: {}", new Object[] { "line1\\nline2" });

        reporter.report(ErrorReporter.Type.PAYLOAD, LOGGED, null, "Error: {}", "value", error);
        verify(builder).log("Error: {}", new Object[] { "value", error });
    }

    @Test
    void testCounters() {
        final IMetricsManager metrics = mock(IMetricsManager.class);
        final IMetricCounter counter = mock(IMetricCounter.class);
        when(metrics.getCounter("device.factory.error.invalid_value")).thenReturn(counter);

        final ErrorReporter counted = new ErrorReporter(logger, metrics);
        counted.report(ErrorReporter.Type.INVALID_VALUE, QUIET, null, "first");
        counted.report(ErrorReporter.Type.INVALID_VALUE, QUIET, null, "second, not logged but counted");
        verify(counter, times(2)).inc();
    }

    @Test
    void testSourceNamed() {
        final LoggingEventBuilder builder = mock(LoggingEventBuilder.class);
        when(logger.atLevel(Level.ERROR)).thenReturn(builder);

        reporter.report(ErrorReporter.Type.RECORD, QUIET, "MQTT handler 'h', topic 'a/b'", "Error: {}", "value");
        verify(builder).log("{}: Error: {}", new Object[] { "MQTT handler 'h', topic 'a/b'", "value" });
    }

    @Test
    void testOptionalMissingAtDebug() {
        // Expected by the mapping: never above debug
        assertEquals(Level.DEBUG, ErrorReporter.Type.OPTIONAL_MISSING.getLevel());
        assertEquals(Level.WARN, ErrorReporter.Type.LENIENT_INVALID.getLevel());
    }

    @Test
    void testCounterNames() {
        assertEquals("device.factory.error.missing_value", ErrorReporter.Type.MISSING_VALUE.getCounterName());
        assertEquals("device.factory.tolerated.optional_missing",
                ErrorReporter.Type.OPTIONAL_MISSING.getCounterName());
    }

    @Test
    void testCountersBuiltOnce() {
        final IMetricsManager metrics = mock(IMetricsManager.class);
        final ErrorReporter counted = new ErrorReporter(logger, metrics);
        counted.report(ErrorReporter.Type.PAYLOAD, QUIET, null, "first");
        counted.report(ErrorReporter.Type.PAYLOAD, QUIET, null, "second");
        verify(metrics, times(1)).getCounter("device.factory.error.payload");
    }

    @Test
    void testOneLogPerInterval() {
        final AtomicLong now = new AtomicLong(1_000_000);
        final ErrorReporter timed = new ErrorReporter(logger, null, now::get);
        final DeviceMappingOptionsDTO options = new DeviceMappingOptionsDTO();
        options.logErrorsInterval = 60;

        assertTrue(timed.count(ErrorReporter.Type.MISSING_VALUE, options), "First error");
        assertFalse(timed.count(ErrorReporter.Type.MISSING_VALUE, options), "Within the interval");
        now.addAndGet(59_999);
        assertFalse(timed.count(ErrorReporter.Type.MISSING_VALUE, options), "Just before the interval");
        now.addAndGet(1);
        assertTrue(timed.count(ErrorReporter.Type.MISSING_VALUE, options), "After the interval");
        assertFalse(timed.count(ErrorReporter.Type.MISSING_VALUE, options), "New interval");
    }

    @Test
    void testOnlyFirstWithoutInterval() {
        final AtomicLong now = new AtomicLong();
        final ErrorReporter timed = new ErrorReporter(logger, null, now::get);
        final DeviceMappingOptionsDTO options = new DeviceMappingOptionsDTO();
        options.logErrorsInterval = 0;

        assertTrue(timed.count(ErrorReporter.Type.PAYLOAD, options));
        now.addAndGet(3_600_000);
        assertFalse(timed.count(ErrorReporter.Type.PAYLOAD, options));
    }

    @Test
    void testNotLoggedErrorsCounted() {
        final LoggingEventBuilder builder = mock(LoggingEventBuilder.class);
        when(logger.atLevel(Level.ERROR)).thenReturn(builder);
        final AtomicLong now = new AtomicLong();
        final ErrorReporter timed = new ErrorReporter(logger, null, now::get);
        final DeviceMappingOptionsDTO options = new DeviceMappingOptionsDTO();
        options.logErrorsInterval = 60;

        timed.report(ErrorReporter.Type.RECORD, options, "src", "Error: {}", "first");
        timed.report(ErrorReporter.Type.RECORD, options, "src", "Error: {}", "second");
        timed.report(ErrorReporter.Type.RECORD, options, "src", "Error: {}", "third");
        now.addAndGet(60_000);
        timed.report(ErrorReporter.Type.RECORD, options, "src", "Error: {}", "fourth", new Exception("hidden"));

        verify(builder).log("{}: Error: {}", new Object[] { "src", "first" });
        verify(builder).log("{}: Error: {} ({} similar errors not logged since the previous one)",
                new Object[] { "src", "fourth", 2L });
    }
}
