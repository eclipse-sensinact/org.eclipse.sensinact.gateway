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
package org.eclipse.sensinact.prototype.annotation.verb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define a SET method for writable values
 *
 * Can be repeated if a single method can write values for more than one
 * service/resource.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(SET.SETs.class)
public @interface SET {

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
     * The type of the resource data. If not set then the received parameter type of
     * the method is used.
     *
     * @return
     */
    Class<?> type() default Object.class;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SETs {
        SET[] value();
    }

}
