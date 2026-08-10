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

/**
 * Normalized kind of a stored value. Normalization happens once in
 * history-core, so every backend receives identically typed input and must
 * round-trip it losslessly (value AND Java type).
 */
public enum ValueKind {
    /**
     * Boxed integral/floating point types, BigInteger, or BigDecimal;
     * NaN/±Infinity arrive as Double.
     */
    NUMBER,
    BOOLEAN,
    STRING,
    /** Any {@code org.eclipse.sensinact.gateway.geojson.GeoJsonObject} subtype. */
    GEOJSON,
    /**
     * Arbitrary structured value, delivered as its canonical JSON
     * representation plus the original type name (replaces the legacy lossy
     * toString()).
     */
    OBJECT
}
