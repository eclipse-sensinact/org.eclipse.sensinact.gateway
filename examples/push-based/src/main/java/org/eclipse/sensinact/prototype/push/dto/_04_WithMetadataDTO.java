/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.push.dto;

import java.util.Map;

import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.MapAction;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;

/**
 * This example is a minimal DTO defining a resource with the URI
 * <code>push-example/simple/count</code> that defines metadata fields
 *
 * <ul>
 * <li>The provider and service names are defined as annotations at class
 * level</li>
 * <li>The resource name is inferred from the data field name.</li>
 * <li>The resource type is inferred from the data field type</li>
 * </ul>
 *
 */
@Provider("push-example")
@Service("simple")
public class _04_WithMetadataDTO {

    /**
     * Name of the resource: it must be explicitly set when using the
     * {@link Metadata} annotation
     */
    @Resource
    public String resource;

    /**
     * Resource value
     */
    @Data
    public int count;

    /**
     * Metadata value: key name will be the field name (<code>name</code>)
     */
    @Metadata
    public String name;

    /**
     * Metadata extracted from the map content
     */
    @Metadata(onMap = MapAction.USE_KEYS_AS_FIELDS)
    public Map<String, Object> values;

}
