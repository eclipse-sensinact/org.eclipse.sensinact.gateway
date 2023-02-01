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

/**
 * Helper to parse a filter
 */
public interface IFilterHandler {

    /**
     * Parses the given filter
     *
     * @param filterLanguage Filter language
     * @param filterQuery    Filter query string
     * @return Parsed filter, null if without effect
     * @throws FilterParserException Error parsing filter
     */
    ICriterion parseFilter(String filterLanguage, String filterQuery) throws FilterParserException;
}
