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
package org.eclipse.sensinact.gateway.southbound.history.provider;

/**
 * Per-bucket aggregate functions. Numeric functions apply to
 * {@code ValueKind.NUMBER} records only; {@link #COUNT} counts every record
 * in the bucket regardless of kind.
 */
public enum AggregateFunction {
    MIN, MAX, AVG, SUM, COUNT, FIRST, LAST
}
