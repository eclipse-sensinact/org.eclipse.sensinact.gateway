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
package org.eclipse.sensinact.gateway.southbound.history.spikes.spi;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * SPI v0 snapshot for the storage backend evaluation: candidate adapters
 * implement this, the shared contract test and benchmark run against it.
 */
public interface HistoryStorage extends AutoCloseable {

    enum Capability {
        AGGREGATION, VALUE_FILTERING
    }

    Set<Capability> capabilities();

    int maxPageSize();

    void store(List<HistoricalRecord> records);

    /** Value at or before {@code at} (inclusive), empty when none exists. */
    Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at);

    Optional<TimedValue<?>> firstValue(ResourcePath path);

    Optional<TimedValue<?>> latestValue(ResourcePath path);

    long count(ResourcePath path, TimeRange range);

    HistoryPage values(HistoryQuery query);

    List<AggregationQuery.Bucket> aggregate(AggregationQuery query);

    long prune(PruneRequest request);

    @Override
    void close();
}
