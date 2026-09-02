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
 * In-memory history storage: reference implementation of the storage SPI.
 * Exported so tests and embedders can construct it programmatically; regular
 * deployments configure it via the {@code sensinact.history.inmemory} PID.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.sensinact.gateway.southbound.history.inmemory;
