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

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Reports the errors of one mapping configuration: the first error of each
 * type is always logged, so that a misconfigured mapping gives a hint, the
 * next ones only when the errors are logged. Every error is counted in the
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
     * Types already reported at least once
     */
    private final AtomicBoolean[] reported;

    /**
     * @param logger  Logger to use
     * @param metrics Metrics manager, can be null
     */
    public ErrorReporter(final Logger logger, final IMetricsManager metrics) {
        this.logger = logger;

        final Type[] types = Type.values();
        reported = new AtomicBoolean[types.length];
        counters = metrics != null ? new IMetricCounter[types.length] : null;
        for (final Type type : types) {
            reported[type.ordinal()] = new AtomicBoolean();
            if (counters != null) {
                // Built once: the manager creates a new counter at each call
                counters[type.ordinal()] = metrics.getCounter(type.getCounterName());
            }
        }
    }

    /**
     * Counts an error and tells if it must be logged: the first of its type, or
     * any when the errors are logged. Lets the caller build its message only
     * when it is logged.
     *
     * @param type      Error type
     * @param logErrors Value of the <code>log.errors</code> mapping option
     * @return True if the error must be logged
     */
    public boolean count(final Type type, final boolean logErrors) {
        if (counters != null && counters[type.ordinal()] != null) {
            counters[type.ordinal()].inc();
        }

        final AtomicBoolean done = reported[type.ordinal()];
        // Read first: no write once the type has been reported
        final boolean first = !done.get() && !done.getAndSet(true);
        return first || logErrors;
    }

    /**
     * Logs an error counted with {@link #count(Type, boolean)}
     *
     * @param type      Error type
     * @param logErrors Value of the <code>log.errors</code> mapping option
     * @param source    Description of the source of the payload, can be null
     * @param format    Message format
     * @param args      Message arguments, the last one can be an exception
     */
    public void log(final Type type, final boolean logErrors, final String source, final String format,
            final Object... args) {
        final Object[] safe = safeArguments(logErrors, args);
        if (source == null) {
            logger.atLevel(type.getLevel()).log(format, safe);
        } else {
            final Object[] withSource = new Object[safe.length + 1];
            withSource[0] = sanitize(source);
            System.arraycopy(safe, 0, withSource, 1, safe.length);
            logger.atLevel(type.getLevel()).log("{}: " + format, withSource);
        }
    }

    /**
     * Counts an error and logs it if needed
     *
     * @param type      Error type
     * @param logErrors Value of the <code>log.errors</code> mapping option
     * @param source    Description of the source of the payload, can be null
     * @param format    Message format
     * @param args      Message arguments, the last one can be an exception
     */
    public void report(final Type type, final boolean logErrors, final String source, final String format,
            final Object... args) {
        if (count(type, logErrors)) {
            log(type, logErrors, source, format, args);
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
