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

import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;

/**
 * This example is a DTO defining two resources with the URIs
 * <code>push-example/simple/count</code> and
 * <code>override-example/simple-override/average</code>
 *
 * <ul>
 * <li>The provider and service names are defined as annotations, with fields
 * overriding class level</li>
 * <li>The resource name is inferred from the data field name unless an
 * annotation defines it.</li>
 * <li>The resource type is inferred from the data field type unless an
 * annotation defines it.</li>
 * </ul>
 *
 */
@Provider("push-example")
@Service("simple")
public class _02_SimpleMultiDTO {

    /**
     * Resource value (resource name will be the field name)
     */
    @Data
    public int count;

    /**
     * Other resource value with overridden characteristics
     */
    @Provider("override-example") // Override provider name
    @Service("simple-override") // Override service name
    @Resource("average") // Explicit resource name
    @Data(type = double.class) // Explicit resource type
    public Float remapped;

}
