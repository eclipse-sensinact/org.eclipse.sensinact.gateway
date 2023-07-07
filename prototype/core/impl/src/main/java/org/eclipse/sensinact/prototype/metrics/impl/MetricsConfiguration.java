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

/**
 * Metrics configuration
 */
public @interface MetricsConfiguration {

    /**
     * Default reporters update rate is {@value #DEFAULT_RATE} seconds
     */
    public static final int DEFAULT_RATE = 10;

    /**
     * Flag to activate metrics (false by default)
     */
    boolean enabled() default false;

    /**
     * Flag to activate the metrics provider (can influence metrics)
     */
    boolean provider_enabled() default true;

    /**
     * Metrics provider name
     */
    String provider_name() default "sensiNact-metrics";

    /**
     * Metrics provider model name
     */
    String provider_model() default "sensiNact-metrics";

    /**
     * Flag to activate console output
     */
    boolean console_enabled() default false;

    /**
     * Metrics update rate in seconds ({@value #DEFAULT_RATE} seconds by default)
     */
    int metrics_rate() default DEFAULT_RATE;

    /**
     * Explicitly enabled metrics. Others will be ignored. All metrics are enabled
     * if not set.
     */
    String[] metrics_enabled() default {};
}
