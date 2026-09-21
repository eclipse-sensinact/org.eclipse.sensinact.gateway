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
/**
 * Backend SPI of the sensiNact history provider: storage backends implement
 * {@link org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage},
 * ingest filters implement
 * {@link org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter};
 * the history-core engine wires both to the typed event bus and exposes each
 * backend as a
 * {@link org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider}.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.sensinact.gateway.southbound.history.storage;
