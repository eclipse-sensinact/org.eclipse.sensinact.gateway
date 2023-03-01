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
package org.eclipse.sensinact.northbound.query.dto.query;

import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.EOperation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Subscription query
 */
public class QuerySubscribeDTO extends AbstractQueryDTO {

    /**
     * Resource filter
     */
    public String filter;

    /**
     * Filter language
     */
    @JsonInclude(Include.NON_NULL)
    public String filterLanguage;

    public QuerySubscribeDTO() {
        super(EOperation.SUBSCRIBE);
    }
}
