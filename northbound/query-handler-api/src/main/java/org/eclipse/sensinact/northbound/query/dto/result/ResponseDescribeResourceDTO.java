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

import org.eclipse.sensinact.core.model.ResourceType;

/**
 * Description of a resource
 */
public class ResponseDescribeResourceDTO implements SubResult {

    /**
     * Resource ID
     */
    public String name;

    /**
     * Resource type
     */
    public ResourceType type;

    /**
     * Resource metadata
     */
    public List<MetadataDTO> attributes;

    /**
     * Resource access methods
     */
    public List<AccessMethodDTO> accessMethods;
}
