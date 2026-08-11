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
package org.eclipse.sensinact.gateway.southbound.history.core;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateBucket;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQueryException;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.UnsupportedHistoryOperationException;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;

/**
 * {@link HistoryProvider} over a {@link HistoryStorage}: resolves and clamps
 * page limits, gates capability-dependent operations, and maps backend
 * failures to {@link HistoryQueryException} — backends receive pre-validated
 * queries only.
 */
public class HistoryProviderEngine implements HistoryProvider {

    private final String name;
    private final HistoryStorage storage;

    public HistoryProviderEngine(String name, HistoryStorage storage) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.storage = Objects.requireNonNull(storage, "storage must not be null");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<HistoryCapability> getCapabilities() {
        return storage.capabilities();
    }

    @Override
    public int getMaxPageSize() {
        return storage.maxPageSize();
    }

    @Override
    public Optional<TimedValue<?>> getValueAt(ResourcePath path, Instant at) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(at, "at must not be null");
        return guarded(() -> storage.valueAt(path, at));
    }

    @Override
    public Optional<TimedValue<?>> getFirstValue(ResourcePath path) {
        Objects.requireNonNull(path, "path must not be null");
        return guarded(() -> storage.firstValue(path));
    }

    @Override
    public Optional<TimedValue<?>> getLatestValue(ResourcePath path) {
        Objects.requireNonNull(path, "path must not be null");
        return guarded(() -> storage.latestValue(path));
    }

    @Override
    public long getValueCount(ResourcePath path, TimeRange range) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(range, "range must not be null");
        return guarded(() -> storage.count(path, range));
    }

    @Override
    public HistoryPage getValues(HistoryQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        if (query.valueFilter() != null && !getCapabilities().contains(HistoryCapability.VALUE_FILTERING)) {
            throw new UnsupportedHistoryOperationException(HistoryCapability.VALUE_FILTERING);
        }
        int limit = query.limit() == HistoryQuery.PROVIDER_DEFAULT_LIMIT ? storage.maxPageSize()
                : Math.min(query.limit(), storage.maxPageSize());
        HistoryQuery resolved = new HistoryQuery(query.path(), query.range(), query.order(), query.offset(), limit,
                query.valueFilter());
        return guarded(() -> storage.values(resolved));
    }

    @Override
    public List<AggregateBucket> aggregate(AggregationQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        if (!getCapabilities().contains(HistoryCapability.AGGREGATION)) {
            throw new UnsupportedHistoryOperationException(HistoryCapability.AGGREGATION);
        }
        return guarded(() -> storage.aggregate(query));
    }

    private <T> T guarded(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (HistoryQueryException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new HistoryQueryException("History backend '" + name + "' failed", e);
        }
    }
}
