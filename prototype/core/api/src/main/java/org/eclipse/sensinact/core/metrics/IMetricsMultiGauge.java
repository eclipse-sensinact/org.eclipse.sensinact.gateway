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
 * Service providing multiple gauges. Called each time a metrics report is
 * generated.
 */
public interface IMetricsMultiGauge {

    /**
     * Name of the service property to hold the names of the gauges
     */
    String NAMES = "sensinact.metrics.multigauge.names";

    /**
     * Returns the value of the gauge with the given name
     *
     * @param name Name of the gauge, one of those defined in the {@link #NAMES}
     *             property
     * @return Value of the gauge with the given name
     */
    Object gauge(String name);
}
