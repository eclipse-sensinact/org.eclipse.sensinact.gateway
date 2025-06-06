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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the name of the service for a given data value
 *
 * Either used:
 *
 * On a String field with no value to supply the service name.
 *
 * <pre>
 * &#64;Service
 * public String service;
 * </pre>
 *
 * or
 *
 * On the type, or a {@link Data} field with a value containing the service name
 *
 * <pre>
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
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.RECORD_COMPONENT })
@Inherited
public @interface Service {
    /**
     * The name of the service
     *
     * @return
     */
    String value() default AnnotationConstants.NOT_SET;
}
