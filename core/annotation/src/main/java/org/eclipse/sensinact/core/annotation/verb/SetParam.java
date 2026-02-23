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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A parameter annotation used to define the type of a setter parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SetParam {

    /**
     * The kind of the action parameter
     */
    SetSegment value();

    /**
     * Possible arguments for SET handlers
     */
    public enum SetSegment {
        /**
         * Current value in the twin. May be a TimedValue or
         * a POJO. The actual resource value will be converted into
         * the parameter type if it does not match.
         */
        CACHED_VALUE,

        /**
         * New value given to the handler. May be a TimedValue or
         * a POJO. The supplied value will be converted into
         * the parameter type if it does not match.
         */
        NEW_VALUE,

        /**
         * Expected result type - the parameter must be a {@link Class}
         */
        RESULT_TYPE,
    }
}
