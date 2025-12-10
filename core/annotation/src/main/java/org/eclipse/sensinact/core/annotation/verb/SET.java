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

import org.eclipse.sensinact.core.annotation.dto.AnnotationConstants;
import org.eclipse.sensinact.core.annotation.verb.VerbAnnotationConstants.Marker;
import org.osgi.util.promise.Promise;

/**
 * Used to define a SET method for writable values
 *<p>
 * Can be repeated if a single method can set values for more than one
 * service/resource.
 * <p>
 * A SET method may return any of:
 * <ul>
 *   <li>A TimedValue - providing the updated data value and timestamp</li>
 *   <li>An Object - providing the updated data value with the timestamp {@link Instant#now}</li>
 *   <li>An Promise - containing a TimedValue or Object</li>
 * </ul>
 * <p>
 * Note that set methods may receive parameters annotated with {@link UriParam} which
 * will provide information about the resource being set. Other parameters
 * must be annotated with {@link SetParam}.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(SET.SETs.class)
public @interface SET {

    /**
     * The package URI of the model that this SET method applies to. If omitted, a
     * package URI is derived from its model name.
     */
    String modelPackageUri() default AnnotationConstants.NOT_SET;

    /**
     * The model that this SET method applies to
     *
     * @return
     */
    String model();

    /**
     * The service that this SET method applies to
     *
     * @return
     */
    String service();

    /**
     * The resource that this SET method applies to
     *
     * @return
     */
    String resource();

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
     * will be set based on the received type of the annotated method. Specifically:
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SETs {
        SET[] value();
    }

}
