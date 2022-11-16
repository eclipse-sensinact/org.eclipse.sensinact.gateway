package org.eclipse.sensinact.prototype.command;

import java.time.Instant;

public interface TimedValue<T> {

    Instant getTimestamp();

    T getValue();
}
