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

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * Time-bucketed aggregation over a resource's history. Buckets are aligned to
 * the epoch; empty buckets are not returned. Capability-gated by
 * {@link HistoryCapability#AGGREGATION}.
 */
public record AggregationQuery(ResourcePath path, TimeRange range, Duration bucketSize,
        Set<AggregateFunction> functions) {

    public AggregationQuery {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(range, "range must not be null");
        Objects.requireNonNull(bucketSize, "bucketSize must not be null");
        Objects.requireNonNull(functions, "functions must not be null");
        if (bucketSize.isZero() || bucketSize.isNegative()) {
            throw new IllegalArgumentException("bucketSize must be positive");
        }
        if (functions.isEmpty()) {
            throw new IllegalArgumentException("at least one aggregate function is required");
        }
        functions = Set.copyOf(functions);
    }
}
