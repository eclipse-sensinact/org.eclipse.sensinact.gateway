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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * Implementation of a reporter that will callback a method with a bulk DTO
 * update
 */
public class CallbackReporter extends ScheduledReporter {

    /**
     * Metrics provider model name
     */
    private final String modelName;

    /**
     * Metrics provider name
     */
    private final String providerName;

    /**
     * The method to callback
     */
    private final Consumer<BulkGenericDto> callback;

    /**
     * @param updateCallback Method to call back with the set of DTOs
     * @param registry       Metrics registry
     * @param providerName   Name of the provider
     */
    protected CallbackReporter(Consumer<BulkGenericDto> updateCallback, MetricRegistry registry, String providerName) {
        this(updateCallback, registry, providerName, providerName, MetricFilter.ALL);
    }

    /**
     * @param updateCallback Method to call back with the set of DTOs
     * @param registry       Metrics registry
     * @param providerName   Name of the provider
     * @param modelName      Name of the provider model
     */
    protected CallbackReporter(Consumer<BulkGenericDto> updateCallback, MetricRegistry registry, String providerName,
            String modelName) {
        this(updateCallback, registry, providerName, modelName, MetricFilter.ALL);
    }

    /**
     * @param updateCallback Method to call back with the set of DTOs
     * @param registry       Metrics registry
     * @param providerName   Name of the provider
     * @param modelName      Name of the provider model
     * @param filter         Metrics filter (null for all)
     */
    protected CallbackReporter(Consumer<BulkGenericDto> updateCallback, MetricRegistry registry, String providerName,
            String modelName, MetricFilter filter) {
        super(registry, providerName, filter == null ? MetricFilter.ALL : filter, TimeUnit.SECONDS,
                TimeUnit.MILLISECONDS);
        this.providerName = providerName;
        this.modelName = modelName != null ? modelName : providerName;
        this.callback = updateCallback;
    }

    /**
     * Ensures that the given resource name is valid
     *
     * @param resource Expected resource name
     * @return Normalized name
     */
    private String normalizeName(final String resource) {
        return resource.replaceAll("\\.", "-");
    }

    /**
     * Prepares a simple metric resource DTO
     *
     * @param time     Update time
     * @param resource Resource name
     * @param value    Resource value
     * @return The associated DTO
     */
    private GenericDto makeMetricsDto(final Instant time, final String resource, final Object value) {

        final GenericDto dto = new GenericDto();
        dto.timestamp = time;
        dto.model = modelName;
        dto.provider = providerName;

        if (resource.contains(".")) {
            final int dotPosition = resource.indexOf('.');
            dto.service = normalizeName(resource.substring(0, dotPosition));
            dto.resource = normalizeName(resource.substring(dotPosition + 1));
        } else {
            dto.service = "metrics";
            dto.resource = normalizeName(resource);
        }

        dto.value = value;
        dto.type = value.getClass();
        return dto;
    }

    /**
     * Generates the DTO for a counter
     *
     * @param time     Update time
     * @param rcPrefix Resource prefix
     * @param counting Counting metric
     * @return A single DTO for a resource suffixed with "-count"
     */
    private GenericDto makeCountDto(final Instant time, final String rcPrefix, final Counting counting) {
        return makeMetricsDto(time, rcPrefix + "-count", counting.getCount());
    }

    /**
     * Generates DTOs for a metered metric
     *
     * @param time     Update time
     * @param rcPrefix Resource prefix
     * @param metered  Metered metric
     * @return The DTOs for the given metric statistics
     */
    private List<GenericDto> makeMeteredDtos(final Instant time, final String rcPrefix, final Metered metered) {
        final List<GenericDto> dtos = new ArrayList<>();
        dtos.add(makeCountDto(time, rcPrefix, metered));
        dtos.add(makeMetricsDto(time, rcPrefix + "-mean-rate", metered.getMeanRate()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-1min-rate", metered.getOneMinuteRate()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-5min-rate", metered.getFiveMinuteRate()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-15min-rate", metered.getFifteenMinuteRate()));
        return dtos;
    }

    /**
     * Generates DTOs for a metrics snapshot
     *
     * @param time     Update time
     * @param rcPrefix Resource prefix
     * @param snapshot Metrics snapshot
     * @return The DTOs for the given snapshot statistics
     */
    private List<GenericDto> makeSnapshotDtos(final Instant time, final String rcPrefix, final Snapshot snapshot) {
        final List<GenericDto> dtos = new ArrayList<>();
        dtos.add(makeMetricsDto(time, rcPrefix + "-min", snapshot.getMin()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-max", snapshot.getMax()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-mean", snapshot.getMean()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-stddev", snapshot.getStdDev()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-p50", snapshot.getMedian()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-p75", snapshot.get75thPercentile()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-p95", snapshot.get95thPercentile()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-p98", snapshot.get98thPercentile()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-p99", snapshot.get99thPercentile()));
        dtos.add(makeMetricsDto(time, rcPrefix + "-p99_9", snapshot.get999thPercentile()));
        return dtos;
    }

    /**
     * Adds the DTOs entries as a single map at the end of the DTOs list for the
     * resource itself
     *
     * @param time     Update time
     * @param resource Resource name
     * @param dtos     List of DTOs for that resource
     */
    private void addGlobalStats(final Instant time, final String resource, final List<GenericDto> dtos) {
        // Make a map from all those statistics
        final Map<String, Object> stats = new HashMap<>();
        for (GenericDto dto : dtos) {
            final String key = dto.resource.substring(dto.resource.lastIndexOf("-") + 1);
            stats.put(key, dto.value);
        }
        dtos.add(makeMetricsDto(time, resource, stats));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        // Same timestamp for all values
        final Instant updateTime = Instant.now();

        // Overall list of DTOs
        final List<GenericDto> dtos = new ArrayList<>();

        for (Entry<String, Gauge> entry : gauges.entrySet()) {
            dtos.add(makeMetricsDto(updateTime, entry.getKey(), entry.getValue().getValue()));
        }

        for (Entry<String, Counter> entry : counters.entrySet()) {
            dtos.add(makeMetricsDto(updateTime, entry.getKey(), entry.getValue().getCount()));
        }

        for (Entry<String, Meter> entry : meters.entrySet()) {
            final String name = entry.getKey();
            final List<GenericDto> meterDtos = new ArrayList<>();
            meterDtos.addAll(makeMeteredDtos(updateTime, name, entry.getValue()));
            addGlobalStats(updateTime, name, meterDtos);
        }

        for (Entry<String, Histogram> entry : histograms.entrySet()) {
            final String name = entry.getKey();
            final Histogram histogram = entry.getValue();

            final List<GenericDto> histogramDtos = new ArrayList<>();
            histogramDtos.add(makeCountDto(updateTime, name, histogram));
            histogramDtos.addAll(makeSnapshotDtos(updateTime, name, histogram.getSnapshot()));
            addGlobalStats(updateTime, name, histogramDtos);
            dtos.addAll(histogramDtos);
        }

        for (Entry<String, Timer> entry : timers.entrySet()) {
            final String name = entry.getKey();
            final Timer timer = entry.getValue();

            final List<GenericDto> timerDtos = new ArrayList<>();
            timerDtos.addAll(makeMeteredDtos(updateTime, name, timer));
            timerDtos.addAll(makeSnapshotDtos(updateTime, name, timer.getSnapshot()));
            addGlobalStats(updateTime, name, timerDtos);
            dtos.addAll(timerDtos);
        }

        // Put everything in a bulk update DTO
        final BulkGenericDto bulk = new BulkGenericDto();
        bulk.dtos = dtos;
        // Notify about the update
        callback.accept(bulk);
    }
}
