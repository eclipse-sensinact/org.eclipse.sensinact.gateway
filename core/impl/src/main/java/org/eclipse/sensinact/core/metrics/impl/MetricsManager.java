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
package org.eclipse.sensinact.core.metrics.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsGauge;
import org.eclipse.sensinact.core.metrics.IMetricsHistogram;
import org.eclipse.sensinact.core.metrics.IMetricsListener;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.metrics.IMetricsMultiGauge;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.Rule;
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
     * List of listeners
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, fieldOption = FieldOption.UPDATE, policy = ReferencePolicy.DYNAMIC)
    private final List<IMetricsListener> listener = new CopyOnWriteArrayList<>();

    @Activate
    void activate(final MetricsConfiguration config) {
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
    }

    /**
     * Found a new gauge service
     *
     * @param gauge      Gauge service
     * @param properties Gauge service properties
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void registerGauge(final IMetricsGauge gauge, final Map<?, ?> properties) {

        final String name = (String) properties.get(IMetricsGauge.NAME);
        if (name == null || name.isBlank()) {
            logger.warn("Gauge service registered without the {} property", IMetricsGauge.NAME);
            return;
        }

        registerGauge(name, gauge::gauge);
    }

    /**
     * A gauge service went away
     *
     * @param properties Gauge service properties
     */
    void unregisterGauge(final Map<?, ?> properties) {
        final String name = (String) properties.get(IMetricsGauge.NAME);
        if (name != null && !name.isBlank()) {
            unregisterGauge(name);
        }
    }

    /**
     * Extracts the names of the gauges provided by the service
     *
     * @param rawNames Raw service property value
     * @return The list of names (never null, can be empty)
     */
    private List<String> extractMultigaugeNames(final Object rawNames) {
        if (rawNames == null) {
            return List.of();
        }

        final ConverterBuilder cb = Converters.newConverterBuilder();
        cb.rule(new Rule<String, String[]>(v -> Arrays.stream(v.split(",")).toArray(String[]::new)) {
        });
        cb.errorHandler((o, e) -> new String[0]);
        return Arrays.asList(cb.build().convert(rawNames).to(String[].class));
    }

    /**
     * Found a new gauge service
     *
     * @param gauges     Multigauge service
     * @param properties Multigauge service properties
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void registerGauges(final IMetricsMultiGauge gauges, final Map<String, Object> properties) {

        final List<String> names = extractMultigaugeNames(properties.get(IMetricsMultiGauge.NAMES));
        if (names.isEmpty()) {
            logger.warn("Multigauge service registered without the {} property", IMetricsMultiGauge.NAMES);
            return;
        }

        for (String name : names) {
            registerGauge(name, () -> gauges.gauge(name));
        }
    }

    /**
     * A gauge service went away
     *
     * @param properties Multigauge service properties
     */
    void unregisterGauges(final Map<?, ?> properties) {
        final List<String> names = extractMultigaugeNames(properties.get(IMetricsMultiGauge.NAMES));
        for (String name : names) {
            unregisterGauge(name);
        }
    }

    /**
     * Registers a gauge to Metrics
     *
     * @param <T>           Gauge type
     * @param name          Gauge name
     * @param gaugeCallback Gauge method
     */
    private <T> void registerGauge(final String name, final Callable<T> gaugeCallback) {
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

    /**
     * Unregisters a gauge from Metrics
     *
     * @param name Gauge name
     */
    private void unregisterGauge(String name) {
        if (registry != null) {
            registry.remove(name);
        }
    }

    /**
     * Called back by the reporter when new DTOs are ready
     *
     * @param dtos Metrics DTOs
     */
    private void reporterCallback(final BulkGenericDto dtos) {
        for (IMetricsListener consumer : listener) {
            try {
                consumer.onMetricsReport(dtos);
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
    public IMetricTimer withTimers(String... names) {
        final List<IMetricTimer> timers = new ArrayList<>();
        for (String name : names) {
            if (isEnabled(name)) {
                timers.add(new MetricsTimer(registry, name));
            }
        }

        return new IMetricTimer() {
            @Override
            public String getName() {
                return "[" + String.join(", ", names) + "]";
            }

            @Override
            public void close() {
                for (IMetricTimer timer : timers) {
                    timer.close();
                }
            }
        };
    }

    @Override
    public IMetricCounter getCounter(String name) {
        return new Counter(name, registry, this::isEnabled);
    }

    @Override
    public IMetricsHistogram getHistogram(String name) {
        return new Histogram(name, registry, this::isEnabled);
    }
}
