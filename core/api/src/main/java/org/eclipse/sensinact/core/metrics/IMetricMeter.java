/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
 * A simple meter that can be notified of events and can
 * be used to track the total number, and rate of, events
 */
public interface IMetricMeter extends INamedMetric {

    /**
     * Mark that an event has occurred
     */
    void mark();

    /**
     * Mark that a batch of <code>n</code> n events has occurred
     * @param n
     */
    void mark(long n);
}
