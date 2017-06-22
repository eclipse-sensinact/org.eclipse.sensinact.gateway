/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.component.data;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.manager.component.DataProvider;

import java.util.UUID;

/**
 * This class stores a data from a {@link DataProvider}
 *
 * @author Rémi Druilhe
 */
public class Data implements DataItf {

    private final UUID uuid;
    private final String sourceUri;
    private final Class<?> type;
    private final Object value;
    private final long timestamp;

    public Data(UUID uuid, String sourceUri, Class<?> type, Object value, long timestamp) {
        this.uuid = uuid;
        this.sourceUri = sourceUri;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    /**
     * Get the UUID of the {@link Data}
     * @return the UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the source URI of this data
     * @return the source URI
     */
    public String getSourceUri() {
        return sourceUri;
    }

    /**
     * Get the value of the {@link Data}
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Get the Java type of the {@link Data}
     * @return the Java type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Get the timestamp of the data
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
