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
 * A parameter annotation used to define the name of a getter parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GetParam {

    /**
     * The kind of the getter parameter
     */
    GetSegment value();

    /**
     * Possible arguments for GET handlers
     */
    public enum GetSegment {
        /**
         * Current value in the twin (TimedValue)
         */
        CACHED_VALUE,

        /**
         * Expected result type (Class)
         */
        RESULT_TYPE,
    }
}
