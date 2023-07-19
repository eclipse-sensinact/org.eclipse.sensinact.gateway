/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.twin.impl;

import java.time.Instant;

import org.eclipse.sensinact.core.twin.TimedValue;

public class TimedValueImpl<T> implements TimedValue<T> {

    private final Instant timestamp;

    private final T value;

    public TimedValueImpl(final T value) {
        this(value, Instant.now());
    }

    public TimedValueImpl(final T value, Instant instant) {
        this.value = value;
        this.timestamp = instant;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("TimedValue(%s, %s)", getValue(), getTimestamp());
    }
}
