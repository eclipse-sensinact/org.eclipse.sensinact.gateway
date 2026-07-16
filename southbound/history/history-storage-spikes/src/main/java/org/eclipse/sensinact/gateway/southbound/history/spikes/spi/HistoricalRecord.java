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
import java.util.Objects;

/**
 * One normalized update to be stored. {@code value} may be null (a resource
 * was cleared); its runtime type must round-trip according to {@code kind}.
 */
public record HistoricalRecord(String modelPackageUri, String model, ResourcePath path, Instant timestamp,
        ValueKind kind, Object value) {
    public HistoricalRecord {
        Objects.requireNonNull(path);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(kind);
    }
}
