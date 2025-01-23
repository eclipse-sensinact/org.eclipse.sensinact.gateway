/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.twin;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record DefaultTimedValue<T> (T value, Instant timestamp) implements TimedValue<T> {

    public static final DefaultTimedValue<?> EMPTY = new DefaultTimedValue<>();

    /**
     * A shortcut for creating an empty TimedValue with no value or timestamp
     * @param value
     */
    @JsonIgnore
    public DefaultTimedValue() {
        this(null, null);
    }

    /**
     * A shortcut for creating a value with the current time
     * @param value
     */
    @JsonIgnore
    public DefaultTimedValue(T value) {
        this(value, Instant.now());
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public T getValue() {
        return value;
    }
}
