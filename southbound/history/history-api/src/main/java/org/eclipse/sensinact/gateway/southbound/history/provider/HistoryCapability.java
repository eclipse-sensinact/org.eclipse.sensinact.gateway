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

/**
 * Optional capabilities a history provider may support. Callers must check
 * {@link HistoryProvider#getCapabilities()} before using capability-gated
 * operations.
 */
public enum HistoryCapability {
    /** {@link HistoryProvider#aggregate(AggregationQuery)} is supported. */
    AGGREGATION,
    /** {@link HistoryQuery#valueFilter()} is executed by the backend. */
    VALUE_FILTERING,
    /** {@link HistoryPage#totalCount()} may be populated on range queries. */
    TOTAL_COUNT,
    /**
     * The backend can delete stored values: housekeeping policies apply.
     * Backends that cannot delete — e.g. an adapter for an external history
     * web service — simply do not declare it and are skipped by housekeeping.
     */
    PRUNING
}
