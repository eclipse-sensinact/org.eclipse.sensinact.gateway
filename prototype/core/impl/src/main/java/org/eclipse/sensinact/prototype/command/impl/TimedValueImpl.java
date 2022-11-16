package org.eclipse.sensinact.prototype.command.impl;

import java.time.Instant;

import org.eclipse.sensinact.prototype.command.TimedValue;

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

}
