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

import java.util.Objects;

/**
 * Paged range query. Every optional dimension is an independent field —
 * backends compose a single query from the populated fields instead of
 * selecting among pre-written statement variants.
 *
 * A {@code limit} of {@link #PROVIDER_DEFAULT_LIMIT} requests the provider's
 * {@link HistoryProvider#getMaxPageSize() maximum page size}; explicit limits
 * must be positive and are clamped to that maximum.
 */
public record HistoryQuery(ResourcePath path, TimeRange range, SortOrder order, long offset, int limit,
        ValueFilter valueFilter) {

    /** Sentinel: use the provider's maximum page size. */
    public static final int PROVIDER_DEFAULT_LIMIT = 0;

    public HistoryQuery {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(range, "range must not be null");
        Objects.requireNonNull(order, "order must not be null");
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be positive or PROVIDER_DEFAULT_LIMIT");
        }
    }

    public static Builder builder(ResourcePath path) {
        return new Builder(path);
    }

    public static final class Builder {

        private final ResourcePath path;
        private TimeRange range = TimeRange.ALL;
        private SortOrder order = SortOrder.ASCENDING;
        private long offset = 0;
        private int limit = PROVIDER_DEFAULT_LIMIT;
        private ValueFilter valueFilter;

        private Builder(ResourcePath path) {
            this.path = Objects.requireNonNull(path, "path must not be null");
        }

        public Builder range(TimeRange range) {
            this.range = Objects.requireNonNull(range, "range must not be null");
            return this;
        }

        public Builder order(SortOrder order) {
            this.order = Objects.requireNonNull(order, "order must not be null");
            return this;
        }

        public Builder offset(long offset) {
            this.offset = offset;
            return this;
        }

        public Builder limit(int limit) {
            if (limit <= 0) {
                throw new IllegalArgumentException("limit must be positive");
            }
            this.limit = limit;
            return this;
        }

        public Builder valueFilter(ValueFilter valueFilter) {
            this.valueFilter = valueFilter;
            return this;
        }

        public HistoryQuery build() {
            return new HistoryQuery(path, range, order, offset, limit, valueFilter);
        }
    }
}
