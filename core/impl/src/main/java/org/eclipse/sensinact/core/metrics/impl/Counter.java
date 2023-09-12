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

package org.eclipse.sensinact.core.metrics.impl;

import java.util.function.Function;

import org.eclipse.sensinact.core.metrics.IMetricCounter;

import com.codahale.metrics.MetricRegistry;

/**
 * A proxy implementation to set a counter
 */
public class Counter implements IMetricCounter {

    /**
     * Metric name
     */
    private final String name;

    /**
     * Method indicating if the metrics service is active
     */
    private final Function<String, Boolean> isActive;

    /**
     * Metrics registry
     */
    private final MetricRegistry registry;

    /**
     * @param name     Counter name
     * @param registry Metrics registry
     * @param isActive Metrics activation test method
     */
    public Counter(final String name, final MetricRegistry registry, final Function<String, Boolean> isActive) {
        this.isActive = isActive;
        this.name = name;
        this.registry = registry;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void inc() {
        if (isActive.apply(name)) {
            registry.counter(name).inc();
        }
    }

    @Override
    public void dec() {
        if (isActive.apply(name)) {
            registry.counter(name).dec();
        }
    }
}
