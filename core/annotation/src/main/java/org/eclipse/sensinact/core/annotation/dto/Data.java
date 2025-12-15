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
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT })
public @interface Data {
    /**
     * The type of the resource data. If not set then the type of the DTO field is
     * used.
     *
     * @return
     */
    Class<?> type() default Object.class;

    /**
     * The upper bound for the resource data. If not set then the resource bounds
     * will be set based on the type of the annotated field. Specifically:
     *
     * <ul>
     *   <li> An array type = -1</li>
     *   <li> <code>List</code>, <code>Set</code>, <code>Collection</code> = -1</li>
     *   <li> All other types = 1</li>
     * <ul>
     *
     * A bound of <code>-1</code> means no upper limit. A bound of <code>1</code> means
     * that the resource value is unary.
     * @return
     */
    int upperBound() default AnnotationConstants.NO_UPPER_BOUND_SET;

    /**
     * The resource action when the data field is null
     *
     * @return
     */
    NullAction onNull() default NullAction.IGNORE;

    /**
     * The action to take if the current value is the same as the new value.
     * The default is to always update, even if the value has not changed.
     *
     * @return
     */
    DuplicateAction onDuplicate() default DuplicateAction.UPDATE_ALWAYS;
}
