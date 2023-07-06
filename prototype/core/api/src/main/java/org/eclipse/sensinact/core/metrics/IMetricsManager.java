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

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.eclipse.sensinact.core.push.dto.BulkGenericDto;

/**
 *
 */
public interface IMetricsManager {

    void enableMetrics();

    void disableMetrics();

    void enableMetrics(String... names);

    void disableMetrics(String... names);

    IMetricTimer withTimer(String name);

    <T> void registerGauge(String name, Callable<T> gaugeCallback);

    void unregisterGauge(String name);

    void unregisterGaugesByPrefix(String prefix);

    IMetricCounter getCounter(String name);

    IMetricsHistogram getHistogram(String string);

    int registerListener(Consumer<BulkGenericDto> listener);

    void unregisterListener(int id);
}
