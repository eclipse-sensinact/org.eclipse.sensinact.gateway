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

import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link Runtime}-based system metrics
 */
@Component
public class JVMMetrics {

    @Reference
    private IMetricsManager metrics;

    @Activate
    void activate() {
        // Make sure no other gauges are here
        metrics.unregisterGaugesByPrefix("jvm.memory.heap.");

        // JVM stats
        final Runtime rt = Runtime.getRuntime();
        metrics.registerGauge("jvm.memory.heap.total", () -> rt.totalMemory());
        metrics.registerGauge("jvm.memory.heap.free", () -> rt.freeMemory());
        metrics.registerGauge("jvm.memory.heap.usage", () -> rt.totalMemory() - rt.freeMemory());
        metrics.registerGauge("jvm.memory.heap.max", () -> rt.maxMemory());
    }

    @Deactivate
    void deactivate() {
        metrics.unregisterGaugesByPrefix("jvm.memory.heap.");
    }
}
