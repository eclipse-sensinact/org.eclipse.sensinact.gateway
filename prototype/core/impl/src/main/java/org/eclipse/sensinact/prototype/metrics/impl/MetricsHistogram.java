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

import org.eclipse.sensinact.core.metrics.IMetricsHistogram;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

/**
 *
 */
public class MetricsHistogram implements IMetricsHistogram {

    /**
     * Histogram name
     */
    private final String name;

    /**
     * Metrics histogram
     */
    private final Histogram histogram;

    /**
     * @param registry Metrics registry
     * @param name     Name of the counter
     */
    public MetricsHistogram(final MetricRegistry registry, final String name) {
        this.name = name;
        this.histogram = registry.histogram(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(long value) {
        histogram.update(value);
    }
}
