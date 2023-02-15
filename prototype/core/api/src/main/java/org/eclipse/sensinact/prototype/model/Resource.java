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
package org.eclipse.sensinact.prototype.model;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.prototype.command.CommandScoped;

/**
 * A model for a resource
 */
public interface Resource extends Modelled, CommandScoped {

    /**
     * The type of the resource
     *
     * @return
     */
    Class<?> getType();

    /**
     * The type of the value
     *
     * @return
     */
    ValueType getValueType();

    /**
     * The type of the resource
     *
     * @return
     */
    ResourceType getResourceType();

    /**
     * The list of arguments for a {@link ResourceType#ACTION} resource
     *
     * @return
     * @throws IllegalStateException if this resource is not an action resource
     */
    List<Map.Entry<String, Class<?>>> getArguments();

    Service getService();

}
