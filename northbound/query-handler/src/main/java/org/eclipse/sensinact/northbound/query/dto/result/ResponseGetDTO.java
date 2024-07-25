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
package org.eclipse.sensinact.northbound.query.dto.result;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Resource/metadata GET response
 */
public class ResponseGetDTO implements SubResult {

    /**
     * Resource name
     */
    public String name;

    /**
     * Resource value time stamp
     */
    public long timestamp;

    /**
     * Resource value type
     */
    public String type;

    /**
     * Resource value
     */
    public Object value;

    /**
     * Resource metadata, if requested
     */
    @JsonInclude(Include.NON_NULL)
    public List<MetadataDTO> attributes;
}
