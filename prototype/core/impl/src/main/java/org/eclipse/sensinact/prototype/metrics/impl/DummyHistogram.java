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

import org.eclipse.sensinact.core.metrics.IMetricsHistogram;

/**
 * Dummy histogram that does nothing
 */
public class DummyHistogram implements IMetricsHistogram {

    private final String name;

    /**
     * @param name Name of the histogram
     */
    public DummyHistogram(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(long value) {
        // Do nothing
    }

}
