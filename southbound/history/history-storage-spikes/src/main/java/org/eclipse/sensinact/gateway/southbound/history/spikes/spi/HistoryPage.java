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

import java.util.List;

import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * One page of results, in the requested order. {@code hasMore} replaces the
 * legacy 501st empty-marker convention.
 */
public record HistoryPage(List<TimedValue<?>> values, long offset, boolean hasMore) {
}
