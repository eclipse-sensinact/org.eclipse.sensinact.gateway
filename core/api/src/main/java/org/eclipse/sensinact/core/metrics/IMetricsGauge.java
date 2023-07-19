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
 * Service providing a gauge. Called each time a metrics report is generated.
 */
public interface IMetricsGauge {

    /**
     * Name of the service property to hold the gauge name
     */
    String NAME = "sensinact.metrics.gauge.name";

    /**
     * Returns the value of the gauge
     */
    Object gauge();
}
