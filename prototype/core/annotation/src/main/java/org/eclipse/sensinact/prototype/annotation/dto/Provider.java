/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.annotation.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the name of the provider for a given data value
 * 
 * Either used:
 * 
 * On a String field with no value to supply the provider name.
 * 
 * <pre>
 * &#64;Provider
 * public String provider;
 * </pre>
 * 
 * or
 * 
 * On the type, or a {@link Data} field with a value containing the provider
 * name
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
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface Provider {
    /**
     * The name of the provider
     * 
     * @return
     */
    String value() default AnnotationConstants.NOT_SET;
}