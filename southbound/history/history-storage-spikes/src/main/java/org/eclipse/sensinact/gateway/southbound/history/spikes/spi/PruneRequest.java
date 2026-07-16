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
 * Housekeeping request. {@code scope} null means all resources. A record is
 * deleted when it is older than {@code olderThan} (if set) OR beyond the
 * newest {@code keepLatestPerResource} records of its resource (if set). At
 * least one bound must be set.
 */
public record PruneRequest(ResourcePath scope, Instant olderThan, Long keepLatestPerResource) {
    public PruneRequest {
        if (olderThan == null && keepLatestPerResource == null) {
            throw new IllegalArgumentException("at least one of olderThan/keepLatestPerResource is required");
        }
        if (keepLatestPerResource != null && keepLatestPerResource < 0) {
            throw new IllegalArgumentException("keepLatestPerResource must be >= 0");
        }
    }
}
