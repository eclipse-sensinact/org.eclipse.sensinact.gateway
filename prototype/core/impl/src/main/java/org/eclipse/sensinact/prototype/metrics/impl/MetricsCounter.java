/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.metrics.impl;

import org.eclipse.sensinact.core.metrics.IMetricCounter;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

/**
 * Metrics-based counter
 */
public class MetricsCounter implements IMetricCounter {

    /**
     * Counter name
     */
    private final String name;

    /**
     * Metrics counter
     */
    private final Counter counter;

    /**
     * @param registry Metrics registry
     * @param name     Name of the counter
     */
    public MetricsCounter(final MetricRegistry registry, final String name) {
        this.name = name;
        this.counter = registry.counter(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long inc() {
        counter.inc();
        return counter.getCount();
    }

    @Override
    public long dec() {
        counter.dec();
        return counter.getCount();
    }
}
