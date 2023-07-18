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
package org.eclipse.sensinact.core.session;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Description/snapshot of the resource
 */
public class ResourceDescription {

    /**
     * Provider name
     */
    public String provider;

    /**
     * Service name
     */
    public String service;

    /**
     * Resource name
     */
    public String resource;

    /**
     * Metadata
     */
    public Map<String, Object> metadata;

    /**
     * Current value
     */
    public Object value;

    /**
     * Time stamp of current value
     */
    public Instant timestamp;

    /**
     * Value type (fixed, updatable, ...)
     */
    public ValueType valueType;

    /**
     * Resource type (action, ...)
     */
    public ResourceType resourceType;

    /**
     * Resource content type
     */
    public Class<?> contentType;

    /**
     * Parameters of the ACT method if the resource is of type Action
     */
    @JsonInclude(Include.NON_NULL)
    public List<Entry<String, Class<?>>> actMethodArgumentsTypes;
}
