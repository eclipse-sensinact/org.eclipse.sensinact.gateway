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

import org.eclipse.sensinact.core.metrics.IMetricTimer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Metrics-based timer resource. Starts when created and stops when closed.
 * Aimed to be used in try-with-resource patterns.
 */
public class MetricsTimer implements IMetricTimer {

    /**
     * Timer name
     */
    private final String name;

    /**
     * Timer contexts
     */
    private final Timer.Context context;

    /**
     * @param registry Metrics registry
     * @param name     Timer name
     */
    public MetricsTimer(MetricRegistry registry, String name) {
        this.name = name;
        context = registry.timer(name).time();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        context.close();
    }
}
