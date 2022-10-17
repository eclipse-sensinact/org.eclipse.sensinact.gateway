/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component.data;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;

/**
 * This class represents a static value.
 *
 * @author Rémi Druilhe
 */
public class ConstantData implements DataItf {
    private final Object value;
    private final Class<?> type;
    private final long timestamp;

    public ConstantData(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @see DataItf#getSourceUri()
     */
    public String getSourceUri() {
        return null;
    }

    /**
     * @see DataItf#getValue()
     */
    public Object getValue() {
        return value;
    }

    /**
     * @see DataItf#getType()
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @see DataItf#getTimestamp()
     */
    public long getTimestamp() {
        return timestamp;
    }
}
