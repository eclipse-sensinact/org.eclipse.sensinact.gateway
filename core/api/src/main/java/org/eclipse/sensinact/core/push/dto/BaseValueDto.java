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
package org.eclipse.sensinact.core.push.dto;

import java.util.Map;

import org.eclipse.sensinact.core.annotation.dto.MapAction;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.ModelPackageUri;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;

/**
 * A base dto for performing updates, suitable for subclassing by device modules
 */
public abstract class BaseValueDto {

    /**
     * The model package URI of the model to which this value belongs. If null then a model package URI will be
     * created if necessary
     */
    @ModelPackageUri
    public String modelPackageUri;

    /**
     * The model to which this value belongs. If null then a model name will be
     * created if necessary
     */
    @Model
    public String model;

    /**
     * The provider to which this value belongs. Must be set.
     */
    @Provider
    public String provider;

    /**
     * The service to which this value belongs. Must be set.
     */
    @Service
    public String service;

    /**
     * The resource to which this value belongs. Must be set.
     */
    @Resource
    public String resource;

    @Metadata(onMap = { MapAction.USE_KEYS_AS_FIELDS, MapAction.REMOVE_NULL_VALUES })
    public Map<String, Object> metadata;

}
