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

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.sensinact.core.metrics.IMetricCounter;

/**
 * Dummy counter that works locally
 */
public class DummyCounter implements IMetricCounter {

    private final String name;
    private final AtomicLong value = new AtomicLong();

    /**
     * @param name Name of the counter
     */
    public DummyCounter(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long inc() {
        return value.incrementAndGet();
    }

    @Override
    public long dec() {
        return value.decrementAndGet();
    }
}
