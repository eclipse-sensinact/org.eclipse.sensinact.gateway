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
package org.eclipse.sensinact.prototype.annotation.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a data field in a DTO which maps to a resource. The name of the
 * resource is determined by the following preference order
 * 
 * <ol>
 * <li>If present, the {@link Resource} annotation present on the field</li>
 * <li>If present, value of the dto field annotated with {@link Resource}</li>
 * <li>If present, the {@link Resource} annotation present on the dto type</li>
 * <li>The name of the field</li>
 * </ol>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Data {
    /**
     * The type of the resource data. If not set then the type of the DTO field is
     * used.
     * 
     * @return
     */
    Class<?> type() default Object.class;

    /**
     * The resource action when the data field is null
     * 
     * @return
     */
    NullAction onNull() default NullAction.IGNORE;
}
