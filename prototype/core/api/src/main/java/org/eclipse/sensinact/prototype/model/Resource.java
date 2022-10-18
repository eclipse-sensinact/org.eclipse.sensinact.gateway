/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.model;

import java.util.List;

/**
 * A model for a resource
 */
public interface Resource extends Modelled {

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
    List<Class<?>> getArguments();

    Service getService();

}
