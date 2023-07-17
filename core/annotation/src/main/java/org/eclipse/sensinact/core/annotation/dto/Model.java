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
 * Defines the identity of the model for a given data value
 *
 * Either used:
 *
 * On a String field with no value to supply the model name.
 *
 * <pre>
 * &#64;Model
 * public String model;
 * </pre>
 *
 * or
 *
 * On the type
 *
 * <pre>
 * &#64;Model(&quot;exampleModel&quot;)
 * &#64;Provider(&quot;exampleProvider&quot;)
 * public class MyDto {
 *     &#64;Service(&quot;exampleService&quot;)
 *     &#64;Data
 *     public String value;
 * }
 * </pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface Model {
    /**
     * The name of the provider
     *
     * @return
     */
    String value() default AnnotationConstants.NOT_SET;
}
