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

import java.util.Objects;

/**
 * Composable range query: every optional dimension is an independent field,
 * combined into a single query by the backend (no template matrix).
 */
public record HistoryQuery(ResourcePath path, TimeRange range, SortOrder order, long offset, int limit,
        ValueFilter valueFilter) {

    public HistoryQuery {
        Objects.requireNonNull(path);
        Objects.requireNonNull(range);
        Objects.requireNonNull(order);
        if (offset < 0 || limit <= 0) {
            throw new IllegalArgumentException("offset must be >= 0 and limit > 0");
        }
    }

    public static HistoryQuery of(ResourcePath path, int limit) {
        return new HistoryQuery(path, TimeRange.ALL, SortOrder.ASCENDING, 0, limit, null);
    }

    public HistoryQuery withRange(TimeRange newRange) {
        return new HistoryQuery(path, newRange, order, offset, limit, valueFilter);
    }

    public HistoryQuery withOrder(SortOrder newOrder) {
        return new HistoryQuery(path, range, newOrder, offset, limit, valueFilter);
    }

    public HistoryQuery withOffset(long newOffset) {
        return new HistoryQuery(path, range, order, newOffset, limit, valueFilter);
    }

    public HistoryQuery withValueFilter(ValueFilter newFilter) {
        return new HistoryQuery(path, range, order, offset, limit, newFilter);
    }
}
