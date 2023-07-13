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

import org.eclipse.sensinact.core.metrics.IMetricTimer;

/**
 * Dummy timer that does nothing
 */
public class DummyTimer implements IMetricTimer {

    private final String name;

    /**
     * @param name Timer name
     */
    public DummyTimer(final String name) {
        this.name = name;
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public String getName() {
        return name;
    }
}
