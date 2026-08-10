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

import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * One page of results in the requested order. {@code hasMore} replaces the
 * legacy 500-row cap with its magic empty-marker convention.
 * {@code totalCount} is populated only when the provider has
 * {@link HistoryCapability#TOTAL_COUNT} and computed it cheaply; consumers
 * needing an exact count use
 * {@link HistoryProvider#getValueCount(ResourcePath, TimeRange)}.
 */
public record HistoryPage(List<TimedValue<?>> values, long offset, boolean hasMore, OptionalLong totalCount) {

    public HistoryPage {
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(totalCount, "totalCount must not be null");
        values = List.copyOf(values);
    }

    public static HistoryPage of(List<TimedValue<?>> values, long offset, boolean hasMore) {
        return new HistoryPage(values, offset, hasMore, OptionalLong.empty());
    }
}
