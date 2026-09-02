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
 * Consumer contract of the sensiNact history provider: an OSGi service
 * interface replacing the legacy ACT-resource-based
 * {@link org.eclipse.sensinact.gateway.southbound.history.api.HistoricalQueries}
 * contract. See {@code HISTORY_PROVIDER_REWORK_PLAN.md}.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.sensinact.gateway.southbound.history.provider;
