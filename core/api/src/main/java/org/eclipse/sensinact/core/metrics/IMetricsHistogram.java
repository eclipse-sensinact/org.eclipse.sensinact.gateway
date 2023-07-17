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
package org.eclipse.sensinact.core.metrics;

/**
 * Represents a metrics histogram: values history is kept to compute statistics
 * on the metrics
 */
public interface IMetricsHistogram extends INamedMetric {

    /**
     * Updates the value of the metric
     *
     * @param value New value of the metric
     */
    void update(long value);
}
