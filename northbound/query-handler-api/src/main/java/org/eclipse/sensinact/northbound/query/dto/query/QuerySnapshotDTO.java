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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;

/**
 *
 */
public class QuerySnapshotDTO extends AbstractQueryDTO {

    /**
     * If set, include metadata in the result
     */
    public boolean includeMetadata = false;

    @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<ResourceSelector> filter;

    @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<SnapshotLinkOption> linkOptions;

    public QuerySnapshotDTO() {
        super(EQueryType.GET_SNAPSHOT);
    }

    public enum SnapshotLinkOption {
        ID_ONLY, MODEL, ICON, LOCATION, DESCRIPTION, FRIENDLY_NAME, FULL;
    }
}
