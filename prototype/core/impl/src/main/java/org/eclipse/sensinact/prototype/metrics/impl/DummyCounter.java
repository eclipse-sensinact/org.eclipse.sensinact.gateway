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

import org.eclipse.sensinact.core.metrics.IMetricCounter;

/**
 * Dummy counter that works locally
 */
public class DummyCounter implements IMetricCounter {

    private final String name;

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
    public void inc() {
        // Do nothing
    }

    @Override
    public void dec() {
        // Do nothing
    }
}