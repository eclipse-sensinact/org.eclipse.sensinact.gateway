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

import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.EQueryType;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySnapshotDTO.SnapshotLinkOption;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;

/**
 *
 */
public class QueryDescribeDTO extends AbstractQueryDTO {

    /**
     * Filter to apply
     */
    public String filter;

    public List<String> attrs;

    @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<SnapshotLinkOption> linkOptions;

    /**
     * Language of the filter to use
     */
    @JsonProperty("filter.language")
    public String filterLanguage;

    public QueryDescribeDTO() {
        super(EQueryType.DESCRIBE);
        attrs = List.of();
    }
}
