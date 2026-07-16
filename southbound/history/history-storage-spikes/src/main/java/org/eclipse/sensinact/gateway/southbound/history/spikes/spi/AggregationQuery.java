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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Time-bucketed aggregation over numeric values; non-numeric records are
 * ignored by the aggregation but still counted by {@link Function#COUNT}.
 */
public record AggregationQuery(ResourcePath path, TimeRange range, Duration bucketSize, Set<Function> functions) {

    public enum Function {
        MIN, MAX, AVG, SUM, COUNT
    }

    public record Bucket(Instant bucketStart, Map<Function, Object> results) {
    }

    public AggregationQuery {
        Objects.requireNonNull(path);
        Objects.requireNonNull(range);
        Objects.requireNonNull(bucketSize);
        if (bucketSize.isZero() || bucketSize.isNegative() || functions == null || functions.isEmpty()) {
            throw new IllegalArgumentException("bucketSize must be positive and functions non-empty");
        }
    }
}
