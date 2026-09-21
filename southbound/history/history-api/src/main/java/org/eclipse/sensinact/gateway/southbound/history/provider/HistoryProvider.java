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
package org.eclipse.sensinact.gateway.southbound.history.provider;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Consumer contract for querying a sensiNact history store. Registered as an
 * OSGi service, one per configured history provider, distinguished by the
 * {@link #PROP_NAME} service property.
 *
 * All operations are synchronous; values are type-faithful (numbers with full
 * stored precision, GeoJSON as the stored {@code GeoJsonObject} subtype).
 * Failures surface as unchecked {@link HistoryQueryException}s.
 */
@ProviderType
public interface HistoryProvider {

    /**
     * Service property carrying the provider name (also the name of the
     * synthetic twin provider exposing the legacy ACT facade).
     */
    String PROP_NAME = "sensinact.history.provider.name";

    String getName();

    Set<HistoryCapability> getCapabilities();

    /**
     * Value at or before {@code at} (inclusive); empty when nothing was
     * stored at or before that time.
     */
    Optional<TimedValue<?>> getValueAt(ResourcePath path, Instant at);

    /** Earliest stored value. */
    Optional<TimedValue<?>> getFirstValue(ResourcePath path);

    /** Newest stored value. */
    Optional<TimedValue<?>> getLatestValue(ResourcePath path);

    /**
     * The oldest values in chronological order, skipping the {@code skip}
     * oldest ones. Convenience for {@link #getValues(HistoryQuery)} with
     * ascending order.
     */
    default List<TimedValue<?>> getFirstValues(ResourcePath path, int limit, long skip) {
        return getValues(HistoryQuery.builder(path).order(SortOrder.ASCENDING).offset(skip).limit(limit).build())
                .values();
    }

    /**
     * The newest values, newest first, skipping the {@code skip} newest
     * ones. Convenience for {@link #getValues(HistoryQuery)} with descending
     * order; reverse the list for chronological display.
     */
    default List<TimedValue<?>> getLastValues(ResourcePath path, int limit, long skip) {
        return getValues(HistoryQuery.builder(path).order(SortOrder.DESCENDING).offset(skip).limit(limit).build())
                .values();
    }

    default long getValueCount(ResourcePath path, TimeRange range) {
        return getValueCount(path, range, null);
    }

    /**
     * Number of values in the range matching the filter. A non-null
     * {@code valueFilter} requires {@link HistoryCapability#VALUE_FILTERING}.
     */
    long getValueCount(ResourcePath path, TimeRange range, ValueFilter valueFilter);

    /**
     * Paged range query. The page size is {@code query.limit()} clamped to
     * {@link #getMaxPageSize()}; {@link HistoryQuery#PROVIDER_DEFAULT_LIMIT}
     * requests the maximum. Never returns marker values.
     */
    HistoryPage getValues(HistoryQuery query);

    /** Largest limit a single {@link #getValues(HistoryQuery)} honors. */
    int getMaxPageSize();

    /**
     * Aggregation, capability-gated by {@link HistoryCapability#AGGREGATION}.
     *
     * @throws UnsupportedHistoryOperationException without the capability
     */
    List<AggregateBucket> aggregate(AggregationQuery query);

    /**
     * Convenience: transparently pages through all results of {@code query}
     * (starting at its offset), up to {@code maxTotal} values.
     */
    default Stream<TimedValue<?>> streamValues(HistoryQuery query, long maxTotal) {
        Iterator<TimedValue<?>> paging = new Iterator<>() {

            private HistoryQuery nextQuery = query;
            private Iterator<TimedValue<?>> currentPage = List.<TimedValue<?>>of().iterator();
            private boolean morePages = true;
            private long delivered = 0;

            @Override
            public boolean hasNext() {
                while (!currentPage.hasNext() && morePages && delivered < maxTotal) {
                    HistoryPage page = getValues(nextQuery);
                    currentPage = page.values().iterator();
                    morePages = page.hasMore();
                    nextQuery = HistoryQuery.builder(nextQuery.path()).range(nextQuery.range())
                            .order(nextQuery.order()).offset(page.offset() + page.values().size())
                            .valueFilter(nextQuery.valueFilter()).build();
                    if (page.values().isEmpty()) {
                        break;
                    }
                }
                return currentPage.hasNext() && delivered < maxTotal;
            }

            @Override
            public TimedValue<?> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                delivered++;
                return currentPage.next();
            }
        };
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(paging, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
