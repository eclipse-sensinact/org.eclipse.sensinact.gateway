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

import org.eclipse.sensinact.core.metrics.IMetricsMultiGauge;
import org.osgi.service.component.annotations.Component;

/**
 * {@link Runtime}-based system metrics
 */
@Component(immediate = true, property = { IMetricsMultiGauge.NAMES + "=" + JVMMetrics.MEM_FREE + ","
        + JVMMetrics.MEM_MAX + "," + JVMMetrics.MEM_TOTAL + "," + JVMMetrics.MEM_USED })
public class JVMMetrics implements IMetricsMultiGauge {

    /**
     * Prefix of all metrics names
     */
    private static final String PREFIX = "jvm.memory.heap.";

    /**
     * Total heap memory
     */
    static final String MEM_TOTAL = PREFIX + "total";

    /**
     * Free heap memory
     */
    static final String MEM_FREE = PREFIX + "free";

    /**
     * Used heap memory
     */
    static final String MEM_USED = PREFIX + "used";

    /**
     * Maximum heap memory
     */
    static final String MEM_MAX = PREFIX + "max";

    @Override
    public Object gauge(String name) {
        final Runtime rt = Runtime.getRuntime();

        switch (name) {
        case MEM_FREE:
            return rt.freeMemory();

        case MEM_MAX:
            return rt.maxMemory();

        case MEM_TOTAL:
            return rt.totalMemory();

        case MEM_USED:
            return rt.totalMemory() - rt.freeMemory();

        default:
            throw new RuntimeException("Unknown gauge name: " + name);
        }
    }
}
