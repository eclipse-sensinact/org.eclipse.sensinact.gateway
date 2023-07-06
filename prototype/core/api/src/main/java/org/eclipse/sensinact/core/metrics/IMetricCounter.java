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
 * A simple counter
 */
public interface IMetricCounter extends INamedMetric {

    /**
     * Increments the counter by 1 then returns its value
     *
     * @return The new value of the counter
     */
    long inc();

    /**
     * Decrements the counter by 1 then returns its value
     *
     * @return The new value of the counter
     */
    long dec();
}
