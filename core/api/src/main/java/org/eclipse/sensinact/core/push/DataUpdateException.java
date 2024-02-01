/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.push;

import java.util.Objects;

public class DataUpdateException extends Exception {

    private static final long serialVersionUID = -3948871450647637647L;
    private final String modelPackageUri;
    private final String model;
    private final String provider;
    private final String service;
    private final String resource;

    private final Object originalDto;

    public DataUpdateException(String modelPackageUri, String model, String provider, String service, String resource,
            Object originalDto, Throwable cause) {
        this(modelPackageUri, model, provider, service, resource, originalDto,
                cause == null ? getDefaultMessage(provider, service, resource) : cause.getMessage(), cause);
    }

    private static String getDefaultMessage(String provider, String service, String resource) {
        return String.format("Data update failed for %s/%s/%s", provider, service, resource);
    }

    protected DataUpdateException(String modelPackageUri, String model, String provider, String service, String resource,
            Object originalDto, String message, Throwable cause) {
        super(message, cause);
        Objects.requireNonNull(originalDto);
        this.modelPackageUri = modelPackageUri;
        this.model = model;
        this.provider = provider;
        this.service = service;
        this.resource = resource;
        this.originalDto = originalDto;
    }

    /**
     * @return The discovered modelPackageUri, or null
     */
    public String getModelPackageUri() {
        return modelPackageUri;
    }

    /**
     * @return The discovered model, or null
     */
    public String getModel() {
        return model;
    }

    /**
     * @return The discovered provider, or null
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @return The discovered service, or null
     */
    public String getService() {
        return service;
    }

    /**
     * @return The discovered resource, or null
     */
    public String getResource() {
        return resource;
    }

    /**
     * @return The DTO that was being mapped
     */
    public Object getOriginalDto() {
        return originalDto;
    }

}
