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
package org.eclipse.sensinact.gateway.southbound.history.core.housekeeping;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * One configured retention policy (internal service between the
 * {@code sensinact.history.housekeeping} factory and the scheduler). At least
 * one of {@code retention}/{@code keepCount} is required.
 */
public record HousekeepingPolicy(String name, List<String> targets, Duration retention, Long keepCount,
        Long maxDelete, Duration schedule) {

    public HousekeepingPolicy {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(schedule, "schedule must not be null");
        if (retention == null && keepCount == null) {
            throw new IllegalArgumentException(
                    "at least one of retention.period/keep.count is required");
        }
        if (schedule.isZero() || schedule.isNegative()) {
            throw new IllegalArgumentException("schedule.period must be positive");
        }
        targets = targets == null ? List.of() : List.copyOf(targets);
    }

    public boolean appliesTo(String providerName) {
        return targets.isEmpty() || targets.contains(providerName);
    }
}
