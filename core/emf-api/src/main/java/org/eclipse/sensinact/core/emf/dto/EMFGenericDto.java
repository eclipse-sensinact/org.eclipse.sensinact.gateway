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
package org.eclipse.sensinact.core.emf.dto;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.push.dto.BaseValueDto;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;

/**
 * A special update dto type. This is for the export mode, where you can work
 * directly with known EMF Models
 */
public final class EMFGenericDto extends BaseValueDto {

    /** The {@link EClass} of the provider */
    public EClass modelEClass;

    /**
     * The name of the {@link EClass} of a service. This must be set, when updating
     * a {@link DynamicProvider}, where a service might not be available yet, it is
     * an alternative to the serviceEClass and will be overwritten by the
     * serviceEClass is set..
     */
    public String serviceEClassName;

    /**
     * The {@link EClass} of a service. This must be set, when updating a
     * {@link DynamicProvider}, where a service might not be available yet.
     */
    public EClass serviceEClass;

    /**
     * The {@link EReference} to the service to set. Can be null if service is
     * available.
     */
    public EReference serviceReference;

    public Class<?> type;

    public Object value;

    /**
     * The timestamp for the data. If null then Instant.now will be used.
     */
    public Instant timestamp;

    /**
     * Action to apply if value is null
     */
    public NullAction nullAction = NullAction.IGNORE;
}
