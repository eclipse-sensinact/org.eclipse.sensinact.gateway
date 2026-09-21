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
import java.util.function.Predicate;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.core.ValueNormalizer;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
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
 */
public class IngestionPipeline {

    private static final Logger logger = LoggerFactory.getLogger(IngestionPipeline.class);

    private final String providerName;
    private final HistoryStorage storage;
    private final ICriterion backendInclude;
    private final ICriterion backendExclude;

    private volatile List<HistoryIngestFilter> filters = List.of();
    private final Map<ResourcePath, TimedValue<?>> lastStored = new ConcurrentHashMap<>();

    public IngestionPipeline(String providerName, HistoryStorage storage, ICriterion backendInclude,
            ICriterion backendExclude) {
        this.providerName = providerName;
        this.storage = storage;
        this.backendInclude = backendInclude;
        this.backendExclude = backendExclude;
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

        try {
            storage.store(List.of(record));
            lastStored.put(record.path(), new DefaultTimedValue<>(record.value(), record.timestamp()));
        } catch (RuntimeException e) {
            logger.warn("History backend {} failed to store an update for {}", providerName, record.path(), e);
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
