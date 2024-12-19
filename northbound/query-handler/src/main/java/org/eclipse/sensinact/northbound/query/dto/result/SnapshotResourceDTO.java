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

public class SnapshotResourceDTO {

    /**
     * Resource ID
     */
    public String name;

    /**
     * Value milliseconds timestamp
     */
    public long timestamp;

    /**
     * Value type
     */
    public String type;

    /**
     * Value value
     */
    public Object value;

    /**
     * Metadata, if requested
     */
    @JsonInclude(Include.NON_NULL)
    public List<MetadataDTO> attributes;
}
