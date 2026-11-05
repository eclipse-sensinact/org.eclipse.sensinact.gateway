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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Reports the errors of one mapping configuration. When the errors are not
 * logged (<code>log.errors</code> false), the first error of each type is
 * logged anyway, so that a misconfigured mapping gives a hint, then at most one
 * per type and per <code>log.errors.interval</code>, with the number of errors
 * not logged since the previous one: a source that degrades later stays
 * visible without filling the logs. Every error is counted in the
 * metrics, as <code>device.factory.error.&lt;type&gt;</code>, and every
 * tolerated value as <code>device.factory.tolerated.&lt;type&gt;</code>.
 * <p>
 * The arguments of the messages are truncated and their line breaks escaped,
 * as they may come from the payload. The message of a logged exception is not:
 * exceptions are only logged when the errors are logged.
 */
public class ErrorReporter {

    /**
     * Types of reported errors
     */
    public enum Type {
        /**
         * The mapping configuration can't be used: no parser, invalid mapping
         */
        CONFIGURATION(Level.ERROR),

        /**
         * The payload can't be parsed
         */
        PAYLOAD(Level.ERROR),

        /**
         * The parser found no record
         */
        NO_RECORD(Level.ERROR),

        /**
         * A record is rejected by a place holder or a variable
         */
        RECORD(Level.ERROR),

        /**
         * A record is rejected by a missing required value
         */
        MISSING_VALUE(Level.ERROR),

        /**
         * A record is rejected by a value that can't be converted
         */
        INVALID_VALUE(Level.ERROR),

        /**
         * The digital twin update failed
         */
        UPDATE(Level.ERROR),

        /**
         * A missing optional value was tolerated
         */
        OPTIONAL_MISSING(Level.WARN),

        /**
         * A value that can't be converted was tolerated
         */
        LENIENT_INVALID(Level.WARN);

        private final Level level;

        Type(final Level level) {
            this.level = level;
        }

        /**
         * Returns the log level of this type
         */
        public Level getLevel() {
            return level;
        }

        /**
         * Returns the name of the metrics counter of this type: tolerated values
         * are not errors
         */
        public String getCounterName() {
            final String prefix = level == Level.WARN ? "device.factory.tolerated." : "device.factory.error.";
            return prefix + name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Maximum length of a value interpolated in a message: values may come from
     * the payload
     */
    static final int MAX_VALUE_LENGTH = 200;

    private final Logger logger;

    /**
     * Counter of each type, null without metrics
     */
    private final IMetricCounter[] counters;

    /**
     * Value of {@link #lastLogged} before the first log of a type
     */
    private static final long NEVER = Long.MIN_VALUE;

    /**
     * Clock, in milliseconds
     */
    private final LongSupplier clock;

    /**
     * Time of the last log of each type, without <code>log.errors</code>
     */
    private final AtomicLong[] lastLogged;

    /**
     * Errors of each type not logged since its last log
     */
    private final AtomicLong[] suppressed;

    /**
     * @param logger  Logger to use
     * @param metrics Metrics manager, can be null
     */
    public ErrorReporter(final Logger logger, final IMetricsManager metrics) {
        this(logger, metrics, System::currentTimeMillis);
    }

    /**
     * @param logger  Logger to use
     * @param metrics Metrics manager, can be null
     * @param clock   Clock in milliseconds, for tests
     */
    ErrorReporter(final Logger logger, final IMetricsManager metrics, final LongSupplier clock) {
        this.logger = logger;
        this.clock = clock;

        final Type[] types = Type.values();
        lastLogged = new AtomicLong[types.length];
        suppressed = new AtomicLong[types.length];
        counters = metrics != null ? new IMetricCounter[types.length] : null;
        for (final Type type : types) {
            lastLogged[type.ordinal()] = new AtomicLong(NEVER);
            suppressed[type.ordinal()] = new AtomicLong();
            if (counters != null) {
                // Built once: the manager creates a new counter at each call
                counters[type.ordinal()] = metrics.getCounter(type.getCounterName());
            }
        }
    }

    /**
     * Counts an error and tells if it must be logged: always with
     * <code>log.errors</code>, otherwise the first of its type and then one per
     * interval. Lets the caller build its message only when it is logged.
     *
     * @param type    Error type
     * @param options Mapping options: <code>log.errors</code> and
     *                <code>log.errors.interval</code>
     * @return True if the error must be logged
     */
    public boolean count(final Type type, final DeviceMappingOptionsDTO options) {
        final int index = type.ordinal();
        if (counters != null && counters[index] != null) {
            counters[index].inc();
        }

        if (options.logErrors) {
            return true;
        }

        final AtomicLong last = lastLogged[index];
        final long previous = last.get();
        final long now = clock.getAsLong();
        final long interval = options.logErrorsInterval * 1000L;
        final boolean due = previous == NEVER || (interval > 0 && now - previous >= interval);
        // Only one of concurrent errors wins the slot
        if (due && last.compareAndSet(previous, now)) {
            return true;
        }

        suppressed[index].incrementAndGet();
        return false;
    }

    /**
     * Logs an error counted with {@link #count(Type, DeviceMappingOptionsDTO)},
     * with the number of errors of its type not logged since the previous one
     *
     * @param type    Error type
     * @param options Mapping options
     * @param source  Description of the source of the payload, can be null
     * @param format  Message format
     * @param args    Message arguments, the last one can be an exception
     */
    public void log(final Type type, final DeviceMappingOptionsDTO options, final String source,
            final String format, final Object... args) {
        final Object[] safe = safeArguments(options.logErrors, args);
        final long notLogged = suppressed[type.ordinal()].getAndSet(0);

        final StringBuilder fullFormat = new StringBuilder();
        final List<Object> fullArgs = new ArrayList<>(safe.length + 2);
        if (source != null) {
            fullFormat.append("{}: ");
            fullArgs.add(sanitize(source));
        }
        fullFormat.append(format);
        if (notLogged > 0) {
            fullFormat.append(" ({} similar errors not logged since the previous one)");
        }

        // The exception, if any, stays last
        final boolean withThrowable = safe.length > 0 && safe[safe.length - 1] instanceof Throwable;
        final int plain = withThrowable ? safe.length - 1 : safe.length;
        for (int i = 0; i < plain; i++) {
            fullArgs.add(safe[i]);
        }
        if (notLogged > 0) {
            fullArgs.add(notLogged);
        }
        if (withThrowable) {
            fullArgs.add(safe[safe.length - 1]);
        }
        logger.atLevel(type.getLevel()).log(fullFormat.toString(), fullArgs.toArray());
    }

    /**
     * Counts an error and logs it if needed
     *
     * @param type    Error type
     * @param options Mapping options
     * @param source  Description of the source of the payload, can be null
     * @param format  Message format
     * @param args    Message arguments, the last one can be an exception
     */
    public void report(final Type type, final DeviceMappingOptionsDTO options, final String source,
            final String format, final Object... args) {
        if (count(type, options)) {
            log(type, options, source, format, args);
        }
    }

    /**
     * Makes the message arguments safe to log
     */
    private static Object[] safeArguments(final boolean logErrors, final Object[] args) {
        int length = args.length;
        final boolean withThrowable = length > 0 && args[length - 1] instanceof Throwable;
        if (withThrowable && !logErrors) {
            // Stack traces only on demand
            length--;
        }

        final Object[] safe = new Object[length];
        for (int i = 0; i < length; i++) {
            final Object arg = args[i];
            safe[i] = arg instanceof Throwable ? arg : sanitize(arg);
        }
        return safe;
    }

    /**
     * Truncates a value and escapes its line breaks, so that a payload can't
     * forge log lines
     *
     * @param value Value to log
     * @return The safe string, null for a null value
     */
    static String sanitize(final Object value) {
        if (value == null) {
            return null;
        }

        String str = String.valueOf(value);
        if (str.length() > MAX_VALUE_LENGTH) {
            str = str.substring(0, MAX_VALUE_LENGTH) + "...";
        }
        return str.replace("\r", "\\r").replace("\n", "\\n");
    }
}
