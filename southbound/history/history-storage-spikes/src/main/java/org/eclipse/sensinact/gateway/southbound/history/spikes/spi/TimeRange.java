/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.spikes.spi;

import java.time.Instant;

/**
 * Time interval with explicit bound inclusivity. Null bounds are unbounded.
 */
public record TimeRange(Instant from, boolean fromInclusive, Instant to, boolean toInclusive) {

    public static final TimeRange ALL = new TimeRange(null, true, null, true);

    public static TimeRange closed(Instant from, Instant to) {
        return new TimeRange(from, true, to, true);
    }

    public static TimeRange atOrBefore(Instant to) {
        return new TimeRange(null, true, to, true);
    }

    public boolean contains(Instant t) {
        if (from != null && (fromInclusive ? t.isBefore(from) : !t.isAfter(from))) {
            return false;
        }
        if (to != null && (toInclusive ? t.isAfter(to) : !t.isBefore(to))) {
            return false;
        }
        return true;
    }
}
