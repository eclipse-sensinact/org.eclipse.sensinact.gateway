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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.sensinact.core.annotation.dto.AnnotationConstants;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.annotation.verb.VerbAnnotationConstants.Marker;
import org.osgi.util.promise.Promise;

/**
 * Used to define a GET method for "pull based" querying of values
 * <p>
 * Can be repeated if a single method can return results for more than one
 * service/resource.
 * <p>
 * A GET method may return any of:
 * <ul>
 *   <li>A TimedValue - providing a data value and timestamp</li>
 *   <li>An Object - providing a data value with the timestamp {@link Instant#now}</li>
 *   <li>An Promise - containing a TimedValue or Object</li>
 * </ul>
 * <p>
 * Note that get methods may receive parameters annotated with {@link UriParam} which
 * will provide information about the resource being queried. Other parameters
 * must be annotated with {@link GetParam}.
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
     * The model package URI for the model that this GET method applies to, can be omitted if {@link #value()}
     * is {@link ReturnType#DTO} and the dto defines the model names and package URIs. If neither is supplied,
     * a package URI will be derived from the model name.
     *
     * @return
     */
    String modelPackageUri() default AnnotationConstants.NOT_SET;

    /**
     * The model that this GET method applies to, can be omitted if {@link #value()}
     * is {@link ReturnType#DTO} and the dto defines the model names
     *
     * @return
     */
    String model() default AnnotationConstants.NOT_SET;

    /**
     * The service that this GET method applies to, can be omitted if
     * {@link #value()} is {@link ReturnType#DTO} and the dto defines the service
     * name(s)
     *
     * @return
     */
    String service() default AnnotationConstants.NOT_SET;

    /**
     * The resource that this GET method applies to
     *
     * @return
     */
    String resource() default AnnotationConstants.NOT_SET;

    /**
     * The type of the resource data. If not set then the return type of the method
     * is used to determine the type of the resource. First the return
     * type is unwrapped from any {@link Promise} and TimedValue types.
     *
     * <ul>
     *   <li>If the return is an array type then the component type of the
     *   array will be used</li>
     *   <li>If the return type is a <code>List</code>, <code>Set</code> or
     *   <code>Collection</code> and has a bound type parameter then the generic
     *   type parameter will be used.</li>
     *   <li>If no suitable type information is available (for example the
     *   method returns <code>List<?></code> or <code>Promise<TimedValue<?>></code>
     *   <code>Object</code> will be used</code>
     *   <li>In all other cases the return type will be used</li>
     *
     * @return
     */
    Class<?> type() default Marker.class;

    /**
     * The lower bound for the resource data. If not set then the resource may be
     * empty.
     *
     * @return
     */
    int lowerBound() default 0;

    /**
     * The upper bound for the resource data. If not set then the resource bounds
     * will be set based on the type of the annotated method. Specifically:
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
    int upperBound() default VerbAnnotationConstants.NO_UPPER_BOUND_SET;

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
