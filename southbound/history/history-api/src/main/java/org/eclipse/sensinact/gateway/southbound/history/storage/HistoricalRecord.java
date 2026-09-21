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
package org.eclipse.sensinact.gateway.southbound.history.storage;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;

/**
 * One normalized update to be stored. {@code value} may be null (resource
 * cleared); otherwise its runtime type follows {@code kind}.
 *
 * {@code endTimestamp} and {@code annotations} are reserved for future use
 * (interval-valued records and per-record metadata such as SensorThings
 * {@code parameters}/{@code resultQuality}): the ingestion pipeline does not
 * populate them yet, but backends must persist them when present so the SPI
 * never needs a breaking change.
 */
public record HistoricalRecord(String modelPackageUri, String model, ResourcePath path, Instant timestamp,
        Instant endTimestamp, ValueKind kind, Object value, Map<String, Object> annotations) {

    public HistoricalRecord {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(kind, "kind must not be null");
        if (endTimestamp != null && endTimestamp.isBefore(timestamp)) {
            throw new IllegalArgumentException("endTimestamp must not be before timestamp");
        }
        annotations = annotations == null ? Map.of() : Map.copyOf(annotations);
    }

    public HistoricalRecord(String modelPackageUri, String model, ResourcePath path, Instant timestamp,
            ValueKind kind, Object value) {
        this(modelPackageUri, model, path, timestamp, null, kind, value, Map.of());
    }
}
