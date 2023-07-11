/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.metrics;

/**
 * Manages metrics.
 */
public interface IMetricsManager {

    /**
     * Enables metrics handling and starts the metrics reporters
     */
    void enableMetrics();

    /**
     * Disables metrics handling and stops the metrics reporters.
     *
     * Gauges can still be registered but won't be called. Metric-related methods
     * will return dummy objects (not null).
     */
    void disableMetrics();

    /**
     * Adds the given metrics to the explicitly activated pool.
     *
     * If no metrics were explicitly activated before, only the given ones will be
     * enabled and others will be implicitly deactivated. If the pool already had
     * values, the given ones will be added to it.
     *
     * @param names Names of metrics to add
     */
    void enableMetrics(String... names);

    /**
     * Removes the given metrics from the explicitly activated pool.
     *
     * If the explicitly activated pool is empty, all metrics are implicitly
     * activated.
     *
     * @param names Names of metrics to remove
     */
    void disableMetrics(String... names);

    /**
     * Resets the state of all counters, histograms and timers. Gauges are kepts as
     * is.
     */
    void clear();

    /**
     * Returns a timer that can be used in a try-with-resources pattern.
     *
     * The timer is started before being returned by this method and will stop when
     * it's {@link AutoCloseable#close()} method will be called.
     *
     * @param name Name of metric
     * @return
     */
    IMetricTimer withTimer(String name);

    /**
     * Defines multiple timer metrics for the same measurement
     *
     * @param names Names of the timers
     * @return Multiple timers handled as a single one
     */
    IMetricTimer withTimers(String... names);

    /**
     * Returns a simple counter. Counters only keep their current value.
     *
     * @param name Metric name
     * @return A simple counter
     */
    IMetricCounter getCounter(String name);

    /**
     * Returns a histogram. Histograms are counters that keep track of their
     * previous values to generate statistics
     *
     * @param name Metric name
     * @return A histogram
     */
    IMetricsHistogram getHistogram(String name);
}
