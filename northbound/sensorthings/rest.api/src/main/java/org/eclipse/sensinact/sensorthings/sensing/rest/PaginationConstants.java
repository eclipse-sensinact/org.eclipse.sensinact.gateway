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
package org.eclipse.sensinact.sensorthings.sensing.rest;

/**
 * Request context properties shared between the query option filters and
 * resource implementations that can apply pagination themselves, e.g. by
 * pushing it down into a history query.
 */
public interface PaginationConstants {

    /**
     * Request context property holding the effective $top value as an
     * {@link Integer} — the requested value capped by the resource method's
     * pagination limit, or that limit when no $top was requested
     */
    String TOP_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.top";

    /**
     * Request context property holding the $skip value as an {@link Integer}
     */
    String SKIP_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.skip";

    /**
     * Request context property holding whether the client requested the
     * entity count ($count=true) as a {@link Boolean}. Resource methods may
     * skip computing expensive counts when it is absent or false.
     */
    String COUNT_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.count";

    /**
     * Request context property a resource method sets to {@link Boolean#TRUE}
     * after applying $orderby, $skip and $top itself. The response-side query
     * option filters then leave the result list untouched: the method already
     * produced the final page, count and nextLink.
     */
    String PAGINATION_APPLIED = "org.eclipse.sensinact.sensorthings.sensing.rest.pagination.applied";
}
