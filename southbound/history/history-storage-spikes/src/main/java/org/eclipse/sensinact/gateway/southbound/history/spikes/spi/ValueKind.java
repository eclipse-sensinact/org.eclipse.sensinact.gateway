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

/**
 * Normalized kind of a stored value (SPI v0 snapshot for the storage
 * evaluation, see HISTORY_PROVIDER_REWORK_PLAN.md).
 */
public enum ValueKind {
    /** Long, Integer, Short, Byte, BigInteger, BigDecimal, Float, Double (incl. NaN/±Infinity) */
    NUMBER,
    BOOLEAN,
    STRING,
    /** any org.eclipse.sensinact.gateway.geojson.GeoJsonObject, lossless */
    GEOJSON,
    /** arbitrary Jackson-mappable structure (Map/List), lossless */
    OBJECT
}
