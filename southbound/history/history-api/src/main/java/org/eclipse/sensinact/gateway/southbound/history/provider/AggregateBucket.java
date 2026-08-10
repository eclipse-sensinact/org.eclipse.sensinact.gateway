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
import java.util.Map;
import java.util.Objects;

/**
 * One aggregation bucket. Numeric results are {@link java.math.BigDecimal};
 * {@link AggregateFunction#COUNT} is a {@link Long}; a numeric function over a
 * bucket without numeric records yields {@code null} (absent from the map).
 */
public record AggregateBucket(Instant bucketStart, Map<AggregateFunction, Object> results) {

    public AggregateBucket {
        Objects.requireNonNull(bucketStart, "bucketStart must not be null");
        Objects.requireNonNull(results, "results must not be null");
        results = Map.copyOf(results);
    }
}
