/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.annotation.verb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

import org.eclipse.sensinact.core.annotation.dto.NullAction;

/**
 * Used to define a GET method for "pull based" querying of values
 *
 * Can be repeated if a single method can return results for more than one
 * service/resource.
 *
 * @see UriParam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(GET.GETs.class)
public @interface GET {

    /**
     * Whether this method returns a raw value, or a DTO containing multiple data
     * values
     *
     * @return
     */
    ReturnType value() default ReturnType.VALUE;

    /**
     * The model that this GET method applies to, can be omitted if {@link #value()}
     * is {@link ReturnType#DTO} and the dto defines the model names
     *
     * @return
     */
    String model() default "<<NOT_SET>>";

    /**
     * The service that this GET method applies to, can be omitted if
     * {@link #value()} is {@link ReturnType#DTO} and the dto defines the service
     * name(s)
     *
     * @return
     */
    String service() default "<<NOT_SET>>";

    /**
     * The resource that this GET method applies to
     *
     * @return
     */
    String resource() default "<<NOT_SET>>";

    /**
     * The type of the resource data. If not set then the return type of the method
     * is used.
     *
     * @return
     */
    Class<?> type() default Object.class;

    /**
     * Duration of the value cache. If the value is requested using a CACHED get and
     * the cache duration is exceeded, the annotated method will be called.
     *
     * The default duration is in {@link ChronoUnit#MILLIS}.
     */
    long cacheDuration() default 500;

    /**
     * Unit of the value cache duration. Defaults to {@link ChronoUnit#MILLIS}.
     */
    ChronoUnit cacheDurationUnit() default ChronoUnit.MILLIS;

    NullAction onNull() default NullAction.IGNORE;

    public enum ReturnType {
        /**
         * The value returned is the data value
         */
        VALUE,
        /**
         * The value returned is a DTO which should be processed
         */
        DTO
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface GETs {
        GET[] value();
    }
}
