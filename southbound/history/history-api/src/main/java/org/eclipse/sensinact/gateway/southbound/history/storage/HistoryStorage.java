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
package org.eclipse.sensinact.gateway.southbound.history.storage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateBucket;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Backend SPI: implemented by storage backends, registered as an OSGi service
 * with the properties below. The history-core engine subscribes to the typed
 * event bus, normalizes and filters updates, calls {@link #store(List)}, and
 * exposes the backend to consumers as a
 * {@link org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider}
 * (which also owns limit clamping and capability gating — backends may assume
 * pre-validated queries).
 */
@ConsumerType
public interface HistoryStorage {

    /** Service property (String, required): the history provider name. */
    String PROP_NAME = HistoryProvider.PROP_NAME;

    /** Service property (String[], optional): JSON ResourceSelector includes. */
    String PROP_INCLUDE = "sensinact.history.include.resources";

    /** Service property (String[], optional): JSON ResourceSelector excludes. */
    String PROP_EXCLUDE = "sensinact.history.exclude.resources";

    Set<HistoryCapability> capabilities();

    int maxPageSize();

    /** Ingestion; called off the gateway thread, list allows batching. */
    void store(List<HistoricalRecord> records);

    /** Value at or before {@code at} (inclusive). */
    Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at);

    Optional<TimedValue<?>> firstValue(ResourcePath path);

    Optional<TimedValue<?>> latestValue(ResourcePath path);

    default long count(ResourcePath path, TimeRange range) {
        return count(path, range, null);
    }

    /**
     * Number of values in the range matching the filter. A non-null
     * {@code valueFilter} is only passed when {@link #capabilities()}
     * contains VALUE_FILTERING.
     */
    long count(ResourcePath path, TimeRange range, ValueFilter valueFilter);

    HistoryPage values(HistoryQuery query);

    /** Only called when {@link #capabilities()} contains AGGREGATION. */
    List<AggregateBucket> aggregate(AggregationQuery query);

    /** @return the number of deleted records */
    long prune(PruneRequest request);
}
