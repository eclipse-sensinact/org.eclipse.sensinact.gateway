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

import java.util.List;

import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.EQueryType;

/**
 *
 */
public class QuerySnapshotDTO extends AbstractQueryDTO {

    /**
     * If set, include metadata in the result
     */
    public boolean includeMetadata = false;

    public List<ResourceSelector> filter;

    public QuerySnapshotDTO() {
        super(EQueryType.GET_SNAPSHOT);
    }
}
