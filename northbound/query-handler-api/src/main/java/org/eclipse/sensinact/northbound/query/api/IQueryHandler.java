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
package org.eclipse.sensinact.northbound.query.api;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

/**
 * Handles a standard sensiNact query
 */
public interface IQueryHandler {

    /**
     * Handles the given query description in the scope of a user session.
     *
     * Exceptions are handled by the method and return a result DTO with the
     * description of the error.
     *
     * @param userSession Caller session
     * @param query       Query description
     * @return Query results
     */
    AbstractResultDTO handleQuery(SensiNactSession userSession, AbstractQueryDTO query);

    /**
     * Parses the given filter
     *
     * @param filter         Filter content (must not be null)
     * @param filterLanguage Filter language
     * @return Parsed filter, null if empty filter
     * @throws StatusException Error parsing filter: 501 for missing filter parser,
     *                         500 for parser exception
     */
    ICriterion parseFilter(String filter, String filterLanguage) throws StatusException;
}
