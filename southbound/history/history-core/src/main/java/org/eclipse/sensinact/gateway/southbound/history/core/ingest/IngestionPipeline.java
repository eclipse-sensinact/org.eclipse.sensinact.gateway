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
package org.eclipse.sensinact.gateway.southbound.history.core.ingest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.core.ValueNormalizer;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ingestion decision chain of one history backend. When ingest filters
 * targeting this backend exist, a record is stored iff at least one filter's
 * include matches AND that filter's change condition admits it, and no
 * filter's exclude matches (the ConfigAdmin filters win). Without applicable
 * filters, the backend's own include/exclude service properties apply
 * unchanged (legacy-compatible fallback).
 *
 * The pipeline tracks the last stored value per resource — the comparison
 * basis handed to {@link HistoryIngestFilter#shouldStore} — in memory; the
 * first update after a restart is therefore always stored.
 *
 * Accepted records are handed to a {@link HistoryRecordingStep}: the built-in
 * one writes them to the storage at once, a replacement can be set with
 * {@link #replaceRecordingStep(HistoryRecordingStep)}. A record counts as
 * stored when the step returns normally.
 */
public class IngestionPipeline {

    private static final Logger logger = LoggerFactory.getLogger(IngestionPipeline.class);

    private final String providerName;
    private final HistoryStorage storage;
    private final ICriterion backendInclude;
    private final ICriterion backendExclude;

    private volatile List<HistoryIngestFilter> filters = List.of();
    // the step in use, locked while it handles a record: retiring a replaced
    // step waits for that record, so the step receives nothing once retired
    private final AtomicReference<StepHolder> current;
    private final Map<ResourcePath, TimedValue<?>> lastStored = new ConcurrentHashMap<>();

    public IngestionPipeline(String providerName, HistoryStorage storage, ICriterion backendInclude,
            ICriterion backendExclude) {
        this.providerName = providerName;
        this.storage = storage;
        this.backendInclude = backendInclude;
        this.backendExclude = backendExclude;
        this.current = new AtomicReference<>(new StepHolder(new DirectRecordingStep(storage)));
    }

    /**
     * Replaces the step recording the accepted records; {@code null} restores
     * the built-in step, which writes to the storage at once. Does not wait:
     * the records that follow go to the new step.
     *
     * @return the previous step, which the caller retires and then closes
     */
    public RetiredStep replaceRecordingStep(HistoryRecordingStep newStep) {
        return new RetiredStep(
                current.getAndSet(new StepHolder(newStep == null ? new DirectRecordingStep(storage) : newStep)));
    }

    /** A step taken out of the pipeline. */
    public static final class RetiredStep {

        private final StepHolder holder;

        private RetiredStep(StepHolder holder) {
            this.holder = holder;
        }

        /**
         * Waits for the record the step may still be handling; the step
         * receives no record afterwards.
         *
         * @return the step, ready to be closed
         */
        public HistoryRecordingStep retire() {
            synchronized (holder) {
                holder.retired = true;
            }
            return holder.step;
        }
    }

    private static final class StepHolder {

        final HistoryRecordingStep step;
        boolean retired;

        StepHolder(HistoryRecordingStep step) {
            this.step = step;
        }
    }

    /** Replaces the applicable filters (already scoped to this backend). */
    public void setFilters(List<HistoryIngestFilter> applicableFilters) {
        this.filters = List.copyOf(applicableFilters);
    }

    /**
     * Escaped typed-event topics this backend needs: the union of the
     * applicable filters' include topics, or the backend's own when no filter
     * applies.
     */
    public Set<String> topics() {
        List<HistoryIngestFilter> currentFilters = filters;
        Set<String> topics = new LinkedHashSet<>();
        if (currentFilters.isEmpty()) {
            addTopics(topics, backendInclude);
        } else {
            currentFilters.forEach(filter -> addTopics(topics, filter.include()));
        }
        return topics;
    }

    private static void addTopics(Set<String> topics, ICriterion criterion) {
        List<String> dataTopics = criterion == null ? null : criterion.dataTopics();
        if (dataTopics == null) {
            topics.add("DATA/*");
        } else {
            dataTopics.stream().filter(Objects::nonNull).forEach(topics::add);
        }
    }

    public void handle(ResourceDataNotification event) {
        HistoricalRecord record = null;
        List<HistoryIngestFilter> currentFilters = filters;

        if (currentFilters.isEmpty()) {
            if (!includes(backendInclude, event) || excludes(backendExclude, event)) {
                return;
            }
            record = ValueNormalizer.normalize(event);
        } else {
            for (HistoryIngestFilter filter : currentFilters) {
                if (excludes(filter.exclude(), event)) {
                    return;
                }
            }
            for (HistoryIngestFilter filter : currentFilters) {
                if (includes(filter.include(), event)) {
                    HistoricalRecord candidate = ValueNormalizer.normalize(event);
                    if (filter.shouldStore(candidate, lastStoredFor(candidate.path()))) {
                        record = candidate;
                        break;
                    }
                }
            }
            if (record == null) {
                return;
            }
        }

        while (true) {
            StepHolder holder = current.get();
            synchronized (holder) {
                if (holder.retired) {
                    // replaced while this record waited: the new step is current
                    continue;
                }
                try {
                    holder.step.record(record);
                    lastStored.put(record.path(), new DefaultTimedValue<>(record.value(), record.timestamp()));
                } catch (RuntimeException e) {
                    logger.warn("History backend {} failed to store an update for {}", providerName, record.path(), e);
                }
                return;
            }
        }
    }

    private Optional<TimedValue<?>> lastStoredFor(ResourcePath path) {
        return Optional.ofNullable(lastStored.get(path));
    }

    /** Missing include criterion or predicate admits everything. */
    private static boolean includes(ICriterion criterion, ResourceDataNotification event) {
        if (criterion == null) {
            return true;
        }
        Predicate<ResourceDataNotification> predicate = criterion.dataEventFilter();
        return predicate == null || predicate.test(event);
    }

    /** Missing exclude criterion or predicate excludes nothing. */
    private static boolean excludes(ICriterion criterion, ResourceDataNotification event) {
        if (criterion == null) {
            return false;
        }
        Predicate<ResourceDataNotification> predicate = criterion.dataEventFilter();
        return predicate != null && predicate.test(event);
    }
}
