/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.eclipse.sensinact.prototype.impl.snapshot;

import java.time.Instant;

import org.eclipse.sensinact.core.snapshot.Snapshot;

/**
 * Parent class of sensiNact snapshot elements
 */
public abstract class AbstractSnapshot implements Snapshot {

    /**
     * Name of the element
     */
    private final String name;

    /**
     * Time of the snapshot
     */
    private final Instant snapshotInstant;

    /**
     * @param name            Name of the element
     * @param snapshotInstant Instant of the snapshot
     */
    public AbstractSnapshot(final String name, final Instant snapshotInstant) {
        this.snapshotInstant = snapshotInstant;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Instant getSnapshotTime() {
        return snapshotInstant;
    }
}
