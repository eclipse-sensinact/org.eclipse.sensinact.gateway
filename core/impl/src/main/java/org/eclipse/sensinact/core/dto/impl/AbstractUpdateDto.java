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
package org.eclipse.sensinact.core.dto.impl;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.annotation.dto.DuplicateAction;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.model.core.provider.Provider;

public abstract class AbstractUpdateDto {

    /**
     * The package URI of the model to use, if null then a unique package URI will
     * be derived from the model name
     */
    public String modelPackageUri;

    /**
     * The model to use, if null then a unique model may be created
     */
    public String model;

    /**
     * The provider name for this update. Must be set
     */
    public String provider;

    /**
     * The service name for this update. Must be set
     */
    public String service;

    /**
     * The resource name for this update. Must be set
     */
    public String resource;

    /**
     * The timestamp for this update. If not set then the current time is used.
     */
    public Instant timestamp;

    /**
     * The original object which this update is derived from
     */
    public Object originalDto;

    /**
     * The provider {@link EClass}, optional
     */
    public EClass modelEClass;

    /**
     * The name of the services {@link EClass}, optional
     */
    public String serviceEClassName;

    /**
     * The services {@link EClass}, optional
     */
    public EClass serviceEClass;

    /**
     * The services {@link EReference} on the {@link Provider} model, optional
     */
    public EReference serviceReference;

    /**
     * The action to take on a null update
     */
    public NullAction actionOnNull;

    /**
     * The action to take when the new value is a duplicate of the old value
     */
    public DuplicateAction actionOnDuplicate;
}
