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
package org.eclipse.sensinact.core.twin;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = DefaultTimedValue.class)
public interface TimedValue<T> {

    Instant getTimestamp();

    T getValue();

    /**
     * @return true if this {@link TimedValue} has no timestamp
     * and is therefore an empty marker value
     */
    @JsonIgnore
    default boolean isEmpty() {
        return getTimestamp() == null;
    }
}
