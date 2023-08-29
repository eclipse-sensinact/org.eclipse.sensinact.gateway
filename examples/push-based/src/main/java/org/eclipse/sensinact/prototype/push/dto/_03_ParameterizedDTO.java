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
import org.eclipse.sensinact.core.annotation.dto.Timestamp;

/**
 * This example is a DTO defining a resource with the URI
 * <code>foo/bar/foobar</code>
 *
 * <ul>
 * <li>The provider, service and resource names are defined as annotated
 * fields</li>
 * <li>The timestamp is set using a field</li>
 * </ul>
 *
 */
public class _03_ParameterizedDTO {

    /**
     * Provider name, with a default value
     */
    @Provider
    public String provider = "foo";

    /**
     * Resource name, with a default value
     */
    @Service
    public String service = "bar";

    /**
     * Resource name, with a default value
     */
    @Resource
    public String resource = "foobar";

    /**
     * Resource value
     */
    @Data
    public int count;

    /**
     * Value timestamp
     */
    @Timestamp
    public long timestamp;

}
