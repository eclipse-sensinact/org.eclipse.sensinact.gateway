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
package org.eclipse.sensinact.prototype.metrics.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsHistogram;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

/**
 * Metrics-based SensiNact metrics manager
 */
@Component(immediate = true, service = IMetricsManager.class, configurationPid = MetricsManager.PID, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class MetricsManager implements IMetricsManager {

    private static final Logger logger = LoggerFactory.getLogger(IMetricsManager.class);

    /**
     * Shared PID
     */
    static final String PID = "sensinact.metrics";

    /**
     * Metrics activation flag
     */
    private boolean isActive = true;

    /**
     * Provider name
     */
    private String metricsProvider;

    /**
     * Update period rate
     */
    private int updateRate;

    /**
     * Explicitly activated metrics
     */
    private final Set<String> activeMetrics = new HashSet<>();

    /**
     * Metrics registry
     */
    private MetricRegistry registry;

    /**
     * Internal metrics reporter
     */
    private CallbackReporter callbackReporter;

    /**
     * Console reporter activation flag
     */
    private boolean allowConsoleReporter;

    /**
     * Console reporter
     */
    private ConsoleReporter consoleReporter;

    /**
     * List of update consumers
     */
    private final Map<Integer, Consumer<BulkGenericDto>> consumers = new HashMap<>();

    /**
     * Holder of next listener ID
     */
    private AtomicInteger nextListenerId;

    @Activate
    void activate(final MetricsConfiguration config) {
        nextListenerId = new AtomicInteger();
        activeMetrics.clear();
        registry = new MetricRegistry();

        update(config);
    }

    @Modified
    void update(final MetricsConfiguration config) {
        isActive = config.enabled();
        metricsProvider = config.provider_name();
        updateRate = config.metrics_rate() > 0 ? config.metrics_rate() : MetricsConfiguration.DEFAULT_RATE;

        activeMetrics.clear();
        activeMetrics.addAll(Arrays.asList(config.metrics_enabled()));

        allowConsoleReporter = config.console_enabled();

        if (isActive) {
            enableMetrics();
        } else {
            disableMetrics();
        }
    }

    @Deactivate
    void deactivate() {
        isActive = false;
        activeMetrics.clear();

        if (consoleReporter != null) {
            consoleReporter.stop();
            consoleReporter = null;
        }

        disableMetrics();

        // Clean up the Metrics registry
        if (registry != null) {
            for (String name : registry.getNames()) {
                registry.remove(name);
            }
            registry = null;
        }

        // Remove listeners
        nextListenerId = null;
        consumers.clear();
    }

    @Override
    public int registerListener(Consumer<BulkGenericDto> listener) {
        if (listener == null || nextListenerId == null) {
            return -1;
        }

        final int listenerId = nextListenerId.incrementAndGet();
        consumers.put(listenerId, listener);
        return listenerId;
    }

    @Override
    public void unregisterListener(int listenerId) {
        consumers.remove(listenerId);
    }

    /**
     * Called back by the reporter when new DTOs are ready
     *
     * @param dtos Metrics DTOs
     */
    private void reporterCallback(final BulkGenericDto dtos) {
        for (Consumer<BulkGenericDto> consumer : consumers.values()) {
            try {
                consumer.accept(dtos);
            } catch (Throwable e) {
                logger.error("Error updating SensiNact metrics: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Checks if metrics are enabled for that name
     *
     * @param name Metrics name
     * @return True if the metric is allows
     */
    private boolean isEnabled(final String name) {
        return isActive && registry != null && (activeMetrics.isEmpty() || activeMetrics.contains(name));
    }

    @Override
    public void enableMetrics() {
        isActive = true;

        // Restart the console reporter if allowed
        if (allowConsoleReporter) {
            if (consoleReporter != null) {
                consoleReporter.close();
                consoleReporter = null;
            }

            consoleReporter = ConsoleReporter.forRegistry(registry).convertDurationsTo(TimeUnit.MILLISECONDS)
                    .convertRatesTo(TimeUnit.SECONDS).build();
            consoleReporter.start(updateRate, TimeUnit.SECONDS);
        } else if (consoleReporter != null) {
            consoleReporter.close();
            consoleReporter = null;
        }

        // Restart the callback reporter
        if (callbackReporter != null) {
            callbackReporter.close();
            callbackReporter = null;
        }

        if (callbackReporter == null) {
            callbackReporter = new CallbackReporter(this::reporterCallback, registry, metricsProvider);
            callbackReporter.start(updateRate, TimeUnit.SECONDS);
        }
    }

    @Override
    public void disableMetrics() {
        isActive = false;

        if (consoleReporter != null) {
            consoleReporter.close();
            consoleReporter = null;
        }

        if (callbackReporter != null) {
            callbackReporter.close();
            callbackReporter = null;
        }

        // Clear metrics
        clear();
    }

    @Override
    public void clear() {
        // Remove all metrics but the gauges
        final Set<String> toRemove = new HashSet<>(registry.getNames());
        toRemove.removeAll(registry.getGauges().keySet());
        registry.removeMatching((name, metric) -> toRemove.contains(name));
    }

    @Override
    public void enableMetrics(String... names) {
        if (names != null) {
            activeMetrics.addAll(Arrays.asList(names));
        }
    }

    @Override
    public void disableMetrics(String... names) {
        if (names != null) {
            activeMetrics.removeAll(Arrays.asList(names));
        }
    }

    @Override
    public IMetricTimer withTimer(String name) {
        if (!isEnabled(name)) {
            return new DummyTimer(name);
        }

        return new MetricsTimer(registry, name);
    }

    @Override
    public <T> void registerGauge(final String name, final Callable<T> gaugeCallback) {
        if (registry != null && gaugeCallback != null) {
            registry.registerGauge(name, new Gauge<T>() {
                @Override
                public T getValue() {
                    try {
                        return gaugeCallback.call();
                    } catch (Exception e) {
                        logger.error("Error calling gauge {}: {}", name, e.getMessage(), e);
                        return null;
                    }
                }
            });
        }
    }

    @Override
    public void unregisterGauge(String name) {
        if (registry != null) {
            registry.remove(name);
        }
    }

    @Override
    public void unregisterGaugesByPrefix(String prefix) {
        if (registry != null && prefix != null && !prefix.isBlank()) {
            for (String key : registry.getGauges().keySet()) {
                if (key.startsWith(prefix)) {
                    registry.remove(key);
                }
            }
        }
    }

    @Override
    public IMetricCounter getCounter(String name) {
        if (!isEnabled(name)) {
            // Return a dummy counter
            return new DummyCounter(name);
        }

        return new MetricsCounter(registry, name);
    }

    @Override
    public IMetricsHistogram getHistogram(String name) {
        if (!isEnabled(name)) {
            // Return a dummy counter
            return new DummyHistogram(name);
        }

        return new MetricsHistogram(registry, name);
    }
}
