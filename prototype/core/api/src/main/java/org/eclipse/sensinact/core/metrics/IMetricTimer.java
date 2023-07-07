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
 * A simple timer that holds the time taken by a task.
 *
 * Timers are started immediately when {@link IMetricsManager#withTimer(String)}
 * returns and run until their {@link #close()} method is called.
 *
 * Timers are {@link AutoCloseable} and can be used in a try-with-resources
 * pattern.
 */
public interface IMetricTimer extends INamedMetric, AutoCloseable {
}
