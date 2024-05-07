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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.model.core.provider.DynamicProvider;

/**
 * A special update dto type. This is for the export mode, where you can work
 * directly with known EMF Models
 */
public final class EMFGenericDto extends GenericDto {

    /** The {@link EClass} of the provider */
    @Model
    public EClass modelEClass;

    /**
     * The {@link EClass} of a service. This must be set, when updating a
     * {@link DynamicProvider}, where a service might not be available yet.
     */
    @Service
    public EClass serviceEClass;

    /**
     * The {@link EReference} to the service to set. Can be null if service is
     * available.
     */
    @Service
    public EReference serviceReference;
}
