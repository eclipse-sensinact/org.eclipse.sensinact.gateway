/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.filters.api;

import org.eclipse.sensinact.prototype.snapshot.ICriterion;

/**
 * Specification of a filter parser
 */
public interface IFilterParser {

    /**
     * Name of the service property declaring the supported filter language(s)
     */
    String SUPPORTED_FILTER_LANGUAGE = "sensinact.filters.supported";

    /**
     * Parses the given filter string
     *
     * @param query Filter string
     * @return Parsed filter (can be null if the filter has no effect)
     * @throws FilterParserException Error parsing filter
     */
    default ICriterion parseFilter(final String query) throws FilterParserException {
        return parseFilter(query, null);
    }

    /**
     * Parses the given filter string
     *
     * @param query         Filter string
     * @param queryLanguage Filter string language (null if unknown)
     * @return Parsed filter (can be null if the filter has no effect)
     * @throws FilterParserException Error parsing filter
     */
    ICriterion parseFilter(final String query, final String queryLanguage) throws FilterParserException;
}
