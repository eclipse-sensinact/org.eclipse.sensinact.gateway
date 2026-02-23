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
package org.eclipse.sensinact.core.annotation.dto;

public final class AnnotationConstants {

    private AnnotationConstants() {
    };

    public static final String NOT_SET = "<<NOT_SET>>";

    /**
     * This marker value is used to indicate that no upper bound
     * has been set in the annotation, and that inference should
     * be used to determine whether this resource is multi-valued.
     */
    public static final int NO_UPPER_BOUND_SET = -3;

}
