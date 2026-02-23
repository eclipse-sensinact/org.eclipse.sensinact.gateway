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

import static org.eclipse.sensinact.northbound.query.dto.query.QueryLinkDTO.LinkAction.ADD;

import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.EQueryType;

/**
 *
 */
public class QueryLinkDTO extends AbstractQueryDTO {

    /**
     * The action to take on the link
     */
    public LinkAction action = ADD;

    /**
     * The action to take on the link
     */
    public String child;

    public QueryLinkDTO() {
        super(EQueryType.LINK);
    }

    public static enum LinkAction {
        ADD, REMOVE;
    }
}
