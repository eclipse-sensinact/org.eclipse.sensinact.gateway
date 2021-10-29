/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
